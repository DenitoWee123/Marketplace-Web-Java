package bg.uni.isn.localmarketplace.service;

import java.util.ArrayList;
import java.util.List;

import bg.uni.isn.localmarketplace.dto.input.payment.CreatePaymentDTO;
import bg.uni.isn.localmarketplace.dto.output.payment.PaymentDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bg.uni.isn.localmarketplace.domain.Payment;
import bg.uni.isn.localmarketplace.domain.Product;
import bg.uni.isn.localmarketplace.domain.User;
import bg.uni.isn.localmarketplace.domain.cart.Cart;
import bg.uni.isn.localmarketplace.domain.cart.CartItem;
import bg.uni.isn.localmarketplace.domain.order.Order;
import bg.uni.isn.localmarketplace.domain.order.OrderItem;
import bg.uni.isn.localmarketplace.dto.input.order.PlaceOrderDTO;
import bg.uni.isn.localmarketplace.dto.output.order.OrderDetailsDTO;
import bg.uni.isn.localmarketplace.exception.cart.EmptyCartException;
import bg.uni.isn.localmarketplace.exception.product.InsufficientStockException;
import bg.uni.isn.localmarketplace.exception.order.InvalidOrderStatusException;
import bg.uni.isn.localmarketplace.exception.order.OrderDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import bg.uni.isn.localmarketplace.repository.PaymentRepository;
import bg.uni.isn.localmarketplace.repository.UserRepository;
import bg.uni.isn.localmarketplace.repository.cart.CartRepository;
import bg.uni.isn.localmarketplace.repository.order.OrderRepository;
import bg.uni.isn.localmarketplace.service.contract.OrderService;
import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import bg.uni.isn.localmarketplace.vo.OrderStatus;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository,
                            UserRepository userRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public OrderDetailsDTO placeOrderFromCart(String username, PlaceOrderDTO dto) {
        User user = userRepository.findForUpdate(username)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        Cart cart = getNonEmptyCart(username);

        List<CartItem> cartItems = cart.getItems();
        validateStockAvailability(cartItems);

        Order order = new Order(user, dto.currency(), 0L,
            OrderStatus.PENDING_PAYMENT, new ArrayList<>());
        order.setTotalAmount(buildOrderItems(order, cartItems));
        orderRepository.save(order);

        decrementStock(cartItems);
        clearCart(cart);
        // Detect version conflicts before returning. The entire transaction rolls back.
        orderRepository.flush();

        return OrderDetailsDTO.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDetailsDTO> getOrders(String username, Pageable pageable) {
        User user = getUser(username);
        if (user.isAdmin()) {
            return orderRepository.findAll(pageable).map(OrderDetailsDTO::from);
        }
        return orderRepository.findByUser_Username(username, pageable).map(OrderDetailsDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailsDTO getOrder(Long id, String username) {
        Order order = findOrder(id);
        assertOwnerOrAdmin(order, username);
        return OrderDetailsDTO.from(order);
    }

    @Override
    public OrderDetailsDTO payOrder(Long id, String requester, CreatePaymentDTO dto) {
        Order order = findOrderForUpdate(id);
        assertOwnerOrAdmin(order, requester);

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStatusException(
                ValidationConstants.Payment.ORDER_NOT_IN_PENDING_PAYMENT
                    + " Current status: " + order.getStatus());
        }

        paymentRepository.save(new Payment(order, order.getTotalAmount(), order.getCurrency(), dto.paymentMethod()));
        order.setStatus(OrderStatus.PROCESSING);

        return OrderDetailsDTO.from(order);
    }

    @Override
    public OrderDetailsDTO updateStatus(Long id, OrderStatus newStatus, String requester) {
        Order order = findOrderForUpdate(id);
        User actor = getUser(requester);
        if (!actor.isAdmin()) {
            assertOwnerOrAdmin(order, requester);
            if (newStatus != OrderStatus.CANCELLED) {
                throw new OwnershipMismatchException("Only admins can advance fulfillment status");
            }
        }
        OrderStatus current = order.getStatus();
        boolean allowed = switch (current) {
            case PENDING_PAYMENT -> newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!allowed) {
            throw new InvalidOrderStatusException("Cannot transition from " + current + " to " + newStatus
                + ". Use the payment endpoint to pay; only unpaid orders can be cancelled.");
        }
        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order.getOrderItems());
        }
        order.setStatus(newStatus);
        orderRepository.flush();
        return OrderDetailsDTO.from(order);
    }

    private Cart getNonEmptyCart(String username) {
        Cart cart = cartRepository.findByUser_Username(username)
            .orElseThrow(() -> new EmptyCartException(ValidationConstants.Order.EMPTY_CART));
        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException(ValidationConstants.Order.EMPTY_CART);
        }
        return cart;
    }

    private void validateStockAvailability(List<CartItem> cartItems) {
        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            if (ci.getQuantity() < 1 || ci.getQuantity() > product.getQuantity()) {
                throw new InsufficientStockException(
                    ValidationConstants.Order.INSUFFICIENT_STOCK +
                        " Product " + product.getId() +
                        ": requested " + ci.getQuantity() + ", available " + product.getQuantity());
            }
        }
    }

    private long buildOrderItems(Order order, List<CartItem> cartItems) {
        long total = 0L;
        for (CartItem ci : cartItems) {
            long price = ci.getProduct().getPrice();
            order.getOrderItems().add(new OrderItem(order, ci.getProduct(), ci.getQuantity(), price));
            total = Math.addExact(total, Math.multiplyExact(price, ci.getQuantity()));
        }
        return total;
    }

    private void decrementStock(List<CartItem> cartItems) {
        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            product.setQuantity(product.getQuantity() - ci.getQuantity());
        }
    }

    private void restoreStock(List<OrderItem> orderItems) {
        for (OrderItem oi : orderItems) {
            Product product = oi.getProduct();
            product.setQuantity(Math.addExact(product.getQuantity(), oi.getQuantity()));
        }
    }

    private void clearCart(Cart cart) {
        cart.getItems().clear();
    }

    private void assertOwnerOrAdmin(Order order, String username) {
        User user = getUser(username);
        if (!user.isAdmin() && !order.getUser().getUsername().equals(username)) {
            throw new OwnershipMismatchException(
                "Order " + order.getId() + " does not belong to user " + username);
        }
    }

    private Order findOrderForUpdate(Long id) {
        return orderRepository.findForUpdate(id)
            .orElseThrow(() -> new OrderDoesNotExistException("Order with id " + id + " does not exist"));
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new OrderDoesNotExistException("Order with id " + id + " does not exist"));
    }

    private User getUser(String username) {
        return userRepository.findById(username)
            .orElseThrow(() -> new UserNotFoundException("User with username " + username + " does not exist"));
    }
}
