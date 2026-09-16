package bg.uni.isn.localmarketplace;

import bg.uni.isn.localmarketplace.domain.*;
import bg.uni.isn.localmarketplace.dto.input.cart.AddCartItemDTO;
import bg.uni.isn.localmarketplace.dto.input.order.PlaceOrderDTO;
import bg.uni.isn.localmarketplace.dto.input.payment.CreatePaymentDTO;
import bg.uni.isn.localmarketplace.dto.input.product.*;
import bg.uni.isn.localmarketplace.exception.cart.EmptyCartException;
import bg.uni.isn.localmarketplace.exception.order.InvalidOrderStatusException;
import bg.uni.isn.localmarketplace.exception.product.InsufficientStockException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.repository.*;
import bg.uni.isn.localmarketplace.repository.order.OrderRepository;
import bg.uni.isn.localmarketplace.security.JwtService;
import bg.uni.isn.localmarketplace.service.contract.*;
import bg.uni.isn.localmarketplace.vo.*;
import jakarta.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MarketplaceIntegrationTests {
    @Autowired UserRepository users;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;
    @Autowired CartService carts;
    @Autowired OrderService orderService;
    @Autowired TagService tags;
    @Autowired ProductService productService;
    @Autowired PlatformTransactionManager transactions;
    @Autowired EntityManager em;
    @Autowired JwtService jwt;
    @Autowired MockMvc mvc;

    String buyer, otherBuyer, vendor, otherVendor, admin;
    Long productId;

    @BeforeEach
    void fixtures() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        buyer = "buyer-" + suffix;
        otherBuyer = "other-" + suffix;
        vendor = "vendor-" + suffix;
        otherVendor = "vendor2-" + suffix;
        admin = "admin-" + suffix;
        tx(() -> {
            for (String name : List.of(buyer, otherBuyer, vendor, otherVendor, admin)) {
                UserType role = name.equals(admin) ? UserType.ADMIN
                    : (name.equals(vendor) || name.equals(otherVendor)) ? UserType.VENDOR : UserType.CUSTOMER;
                users.save(new User(name, "Test", "User", "unused-password", name + "@example.test", null, role));
            }
            productId = products.save(new Product(ProductType.ART, "Original", "Handmade", 1200,
                users.findById(vendor).orElseThrow(), 1)).getId();
            return null;
        });
    }

    @Test
    void checkoutSnapshotsPriceAndClearsCart() {
        add(buyer);
        var result = checkout(buyer);
        assertEquals(1200, result.totalAmount());
        assertEquals(OrderStatus.PENDING_PAYMENT, result.status());
        assertEquals(0, stock());
        assertTrue(carts.getCart(buyer).items().isEmpty());
        tx(() -> { products.findById(productId).orElseThrow().setPrice(2000); return null; });
        assertEquals(1200, orderService.getOrder(result.id(), buyer).orderItems().getFirst().price());
    }

    @Test
    void insufficientStockRollsBackEntireMultiProductOrder() {
        Long second = tx(() -> products.save(new Product(ProductType.ART, "Second", "", 200,
            users.findById(vendor).orElseThrow(), 1)).getId());
        add(buyer);
        carts.addItem(buyer, new AddCartItemDTO(second, 1));
        tx(() -> { products.findById(second).orElseThrow().setQuantity(0); return null; });
        assertThrows(InsufficientStockException.class, () -> checkout(buyer));
        assertEquals(1, stock());
        assertEquals(0, countOrders(buyer));
        assertEquals(2, carts.getCart(buyer).items().size());
    }

    @Test
    void emptyCartCannotBeCheckedOut() {
        assertThrows(EmptyCartException.class, () -> checkout(buyer));
        assertEquals(0, countOrders(buyer));
    }

    @Test
    @Timeout(30)
    void concurrentLastUnitHasExactlyOneWinnerAndRollsBackLoser() throws Exception {
        add(buyer);
        add(otherBuyer);
        CyclicBarrier loaded = new CyclicBarrier(2);
        // Force both independent persistence contexts to observe the same stock version.
        // Service calls join these transactions; flush must reject exactly one stale writer.
        List<String> outcomes = race(
            () -> staleCheckout(buyer, loaded), () -> staleCheckout(otherBuyer, loaded));
        assertEquals(1, Collections.frequency(outcomes, "ok"));
        assertEquals(1, Collections.frequency(outcomes, "optimistic-conflict"));
        assertEquals(0, stock());
        assertEquals(1, countOrders(buyer) + countOrders(otherBuyer));
        assertEquals(1, carts.getCart(buyer).items().size() + carts.getCart(otherBuyer).items().size());
    }

    @Test
    @Timeout(30)
    void duplicateCheckoutDoesNotCreateTwoOrders() throws Exception {
        tx(() -> { products.findById(productId).orElseThrow().setQuantity(2); return null; });
        add(buyer);
        List<String> outcomes = race(() -> checkout(buyer), () -> checkout(buyer));
        assertEquals(1, Collections.frequency(outcomes, "ok"));
        assertEquals(1, Collections.frequency(outcomes, "empty-cart"));
        assertEquals(1, countOrders(buyer));
        assertEquals(1, stock());
    }

    @Test
    void ownerCanCancelUnpaidOrderOnlyOnce() {
        add(buyer);
        Long id = checkout(buyer).id();
        assertEquals(OrderStatus.CANCELLED, orderService.updateStatus(id, OrderStatus.CANCELLED, buyer).status());
        assertEquals(1, stock());
        assertThrows(InvalidOrderStatusException.class,
            () -> orderService.updateStatus(id, OrderStatus.CANCELLED, buyer));
        assertEquals(1, stock());
    }

    @Test
    @Timeout(30)
    void concurrentCancellationRestoresStockExactlyOnce() throws Exception {
        add(buyer);
        Long id = checkout(buyer).id();
        var outcomes = race(() -> orderService.updateStatus(id, OrderStatus.CANCELLED, buyer),
            () -> orderService.updateStatus(id, OrderStatus.CANCELLED, buyer));
        assertEquals(1, Collections.frequency(outcomes, "ok"));
        assertEquals(1, Collections.frequency(outcomes, "invalid-status"));
        assertEquals(1, stock());
    }

    @Test
    void unrelatedUserCannotReadPayOrCancelOrder() {
        add(buyer);
        Long id = checkout(buyer).id();
        assertThrows(OwnershipMismatchException.class, () -> orderService.getOrder(id, otherBuyer));
        assertThrows(OwnershipMismatchException.class, () -> pay(id, otherBuyer));
        assertThrows(OwnershipMismatchException.class,
            () -> orderService.updateStatus(id, OrderStatus.CANCELLED, otherBuyer));
        assertEquals(OrderStatus.PENDING_PAYMENT, orderService.getOrder(id, buyer).status());
        assertEquals(0, stock());
    }

    @Test
    void paymentCannotBeBypassedAndOnlyAdminCanFulfill() {
        add(buyer);
        Long id = checkout(buyer).id();
        for (OrderStatus status : List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED)) {
            assertThrows(InvalidOrderStatusException.class, () -> orderService.updateStatus(id, status, admin));
        }
        pay(id, buyer);
        assertThrows(OwnershipMismatchException.class,
            () -> orderService.updateStatus(id, OrderStatus.SHIPPED, buyer));
        assertThrows(OwnershipMismatchException.class,
            () -> orderService.updateStatus(id, OrderStatus.SHIPPED, vendor));
        assertThrows(InvalidOrderStatusException.class,
            () -> orderService.updateStatus(id, OrderStatus.DELIVERED, admin));
        assertEquals(OrderStatus.SHIPPED, orderService.updateStatus(id, OrderStatus.SHIPPED, admin).status());
        assertEquals(OrderStatus.DELIVERED, orderService.updateStatus(id, OrderStatus.DELIVERED, admin).status());
        assertThrows(InvalidOrderStatusException.class,
            () -> orderService.updateStatus(id, OrderStatus.CANCELLED, admin));
    }

    @Test
    void paidOrderCannotBeCancelledWithoutRefundSupport() {
        add(buyer);
        Long id = checkout(buyer).id();
        pay(id, buyer);
        assertThrows(InvalidOrderStatusException.class,
            () -> orderService.updateStatus(id, OrderStatus.CANCELLED, buyer));
        assertEquals(0, stock());
    }

    @Test
    @Timeout(30)
    void concurrentPaymentRecordsExactlyOnePayment() throws Exception {
        add(buyer);
        Long id = checkout(buyer).id();
        var outcomes = race(() -> pay(id, buyer), () -> pay(id, buyer));
        assertEquals(1, Collections.frequency(outcomes, "ok"));
        assertEquals(1, Collections.frequency(outcomes, "invalid-status"));
        assertEquals(1, tx(() -> orders.findById(id).orElseThrow().getPayments().size()));
        assertEquals(OrderStatus.PROCESSING, orderService.getOrder(id, buyer).status());
    }

    @Test
    @Timeout(30)
    void simultaneousPaymentAndCancellationCannotBothSucceed() throws Exception {
        add(buyer);
        Long id = checkout(buyer).id();
        var outcomes = race(() -> pay(id, buyer),
            () -> orderService.updateStatus(id, OrderStatus.CANCELLED, buyer));
        assertEquals(1, Collections.frequency(outcomes, "ok"));
        assertEquals(1, Collections.frequency(outcomes, "invalid-status"));
        boolean paid = orderService.getOrder(id, buyer).status() == OrderStatus.PROCESSING;
        assertEquals(paid ? 0 : 1, stock());
        assertEquals(paid ? 1 : 0, tx(() -> orders.findById(id).orElseThrow().getPayments().size()));
    }

    @Test
    void manyToManyPersistsSharedTagsAndDoesNotDeleteThemWhenUnlinked() {
        var tag = tags.create(new CreateTagDTO(" Handmade-" + buyer + " "), vendor);
        var secondTag = tags.create(new CreateTagDTO("Gift-" + buyer), vendor);
        Long second = tx(() -> products.save(new Product(ProductType.ART, "Second", "", 100,
            users.findById(vendor).orElseThrow(), 2)).getId());
        tags.replaceProductTags(productId, new UpdateProductTagsDTO(Set.of(tag.id(), secondTag.id())), vendor);
        tags.replaceProductTags(second, new UpdateProductTagsDTO(Set.of(tag.id())), vendor);
        assertEquals(2, productService.getProduct(productId).tags().size());
        assertEquals(List.of(tag), productService.getProduct(second).tags());
        assertEquals("handmade-" + buyer, tag.name());
        tags.replaceProductTags(productId, new UpdateProductTagsDTO(Set.of()), vendor);
        assertTrue(productService.getProduct(productId).tags().isEmpty());
        assertEquals(List.of(tag), productService.getProduct(second).tags());
    }

    @Test
    void productFilterQueryWorks() {
        var result = products.findProductsWithFilters(List.of(ProductType.ART), vendor, true, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals(productId, result.getContent().getFirst().getId());
        assertTrue(products.findProductsWithFilters(List.of(ProductType.TOYS), vendor, true, PageRequest.of(0, 10)).isEmpty());
    }

    @Test
    void httpRejectsForeignOrderChangesWith403AndIllegalTransitionsWith409() throws Exception {
        add(buyer);
        Long id = checkout(buyer).id();
        mvc.perform(patch("/api/orders/{id}/status", id).header("Authorization", token(otherBuyer))
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(patch("/api/orders/{id}/status", id).header("Authorization", token(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DELIVERED\"}"))
            .andExpect(status().isConflict());
        mvc.perform(patch("/api/orders/{id}/pay", id).header("Authorization", token(buyer))
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/orders/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void tagHttpEndpointsValidateInputRolesOwnershipAndDuplicateNames() throws Exception {
        mvc.perform(get("/api/tags")).andExpect(status().isOk());
        mvc.perform(post("/api/tags").header("Authorization", token(buyer))
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"sample\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/tags").header("Authorization", token(vendor))
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/tags").header("Authorization", token(vendor))
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + buyer + "\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value(buyer));
        mvc.perform(post("/api/tags").header("Authorization", token(vendor))
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" " + buyer.toUpperCase(Locale.ROOT) + " \"}"))
            .andExpect(status().isConflict());
        var tag = tags.create(new CreateTagDTO("tag-" + buyer), vendor);
        String payload = "{\"tagIds\":[" + tag.id() + "]}";
        mvc.perform(put("/api/products/{id}/tags", productId).header("Authorization", token(otherVendor))
            .contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isForbidden());
        mvc.perform(put("/api/products/{id}/tags", productId).header("Authorization", token(vendor))
            .contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tags[0].id").value(tag.id().intValue()));
        mvc.perform(put("/api/products/{id}/tags", productId).header("Authorization", token(vendor))
            .contentType(MediaType.APPLICATION_JSON).content("{\"tagIds\":[9223372036854775807]}"))
            .andExpect(status().isNotFound());
        assertEquals(List.of(tag), productService.getProduct(productId).tags());
        mvc.perform(put("/api/products/{id}/tags", productId).header("Authorization", token(vendor))
            .contentType(MediaType.APPLICATION_JSON).content("{\"tagIds\":[null]}"))
            .andExpect(status().isBadRequest());
        mvc.perform(put("/api/products/{id}/tags", productId).header("Authorization", token(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"tagIds\":[]}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tags").isEmpty());
    }

    @Test
    void swaggerDescribesNewEndpoints() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/tags']").exists())
            .andExpect(jsonPath("$.paths['/api/products/{id}/tags']").exists());
    }

    private <T> T tx(Supplier<T> body) {
        return new TransactionTemplate(transactions).execute(status -> body.get());
    }
    private void add(String username) { carts.addItem(username, new AddCartItemDTO(productId, 1)); }
    private bg.uni.isn.localmarketplace.dto.output.order.OrderDetailsDTO checkout(String username) {
        return orderService.placeOrderFromCart(username, new PlaceOrderDTO(CurrencyType.BGN));
    }
    private Object pay(Long id, String username) {
        return orderService.payOrder(id, username, new CreatePaymentDTO(PaymentMethod.CARD));
    }
    private int stock() { return products.findById(productId).orElseThrow().getQuantity(); }
    private long countOrders(String username) {
        return orders.findByUser_Username(username, PageRequest.of(0, 10)).getTotalElements();
    }
    private String token(String username) { return "Bearer " + jwt.generateToken(username); }
    private Object staleCheckout(String username, CyclicBarrier barrier) {
        return tx(() -> {
            em.find(Product.class, productId).getQuantity();
            try { barrier.await(10, TimeUnit.SECONDS); }
            catch (Exception e) { throw new IllegalStateException(e); }
            return checkout(username);
        });
    }
    private List<String> race(Supplier<?> first, Supplier<?> second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (Supplier<?> action : List.of(first, second)) {
                futures.add(pool.submit(() -> {
                    if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Start timeout");
                    try { action.get(); return "ok"; }
                    catch (OptimisticLockingFailureException e) { return "optimistic-conflict"; }
                    catch (EmptyCartException e) { return "empty-cart"; }
                    catch (InvalidOrderStatusException e) { return "invalid-status"; }
                }));
            }
            start.countDown();
            return List.of(futures.get(0).get(15, TimeUnit.SECONDS), futures.get(1).get(15, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) throw new IllegalStateException("Worker did not finish");
        }
    }
}
