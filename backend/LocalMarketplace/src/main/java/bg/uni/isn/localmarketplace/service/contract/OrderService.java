package bg.uni.isn.localmarketplace.service.contract;

import bg.uni.isn.localmarketplace.dto.input.payment.CreatePaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import bg.uni.isn.localmarketplace.dto.input.order.PlaceOrderDTO;
import bg.uni.isn.localmarketplace.dto.output.order.OrderDetailsDTO;
import bg.uni.isn.localmarketplace.exception.cart.EmptyCartException;
import bg.uni.isn.localmarketplace.exception.product.InsufficientStockException;
import bg.uni.isn.localmarketplace.exception.order.InvalidOrderStatusException;
import bg.uni.isn.localmarketplace.exception.order.OrderDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import bg.uni.isn.localmarketplace.vo.OrderStatus;

public interface OrderService {

    /**
     * Places an order from the current user's cart. Snapshots product prices into OrderItems,
     * computes totalAmount, sets status PENDING_PAYMENT, decrements product stock, and clears the cart.
     *
     * @param username the username of the customer placing the order
     * @param dto      currency
     * @return created {@link OrderDetailsDTO}
     * @throws UserNotFoundException      if the user does not exist
     * @throws EmptyCartException         if the cart is absent or contains no items
     * @throws InsufficientStockException if any cart item quantity exceeds available product stock
     */
    OrderDetailsDTO placeOrderFromCart(String username, PlaceOrderDTO dto);

    /**
     * Retrieves orders for the given user. Admins receive all orders; non-admins receive only their own.
     *
     * @param username the requesting user's username
     * @param pageable pagination parameters
     * @return paginated list of orders
     * @throws UserNotFoundException if the user does not exist
     */
    Page<OrderDetailsDTO> getOrders(String username, Pageable pageable);

    /**
     * Retrieves a single order by ID. Accessible by the order owner or an admin.
     *
     * @param id       the order ID
     * @param username the requesting user's username
     * @return {@link OrderDetailsDTO}
     * @throws OrderDoesNotExistException if the order does not exist
     * @throws OwnershipMismatchException if the requester is neither the owner nor an admin
     */
    OrderDetailsDTO getOrder(Long id, String username);

    /**
     * Updates the status of an order. When newStatus is CANCELLED, product stock is restored first.
     * Cannot transition out of a terminal state (CANCELLED or DELIVERED).
     *
     * @param id        the order ID
     * @param newStatus the target status
     * @param requester the requesting user's username (vendor/admin role check pending Security — see §10)
     * @return updated {@link OrderDetailsDTO}
     * @throws OrderDoesNotExistException  if the order does not exist
     * @throws InvalidOrderStatusException if the order is already in a terminal state
     */
    OrderDetailsDTO updateStatus(Long id, OrderStatus newStatus, String requester);

    /**
     * Simulates payment for an order. Records a Payment using the method/currency/amount already on the
     * order, then transitions the order status from PENDING_PAYMENT to PROCESSING.
     * Accessible by the order owner or an admin.
     *
     * @param id        the order ID
     * @param requester the requesting user's username
     * @param dto the data for creating the payment
     * @return updated {@link OrderDetailsDTO} with status PROCESSING
     * @throws OrderDoesNotExistException  if the order does not exist
     * @throws OwnershipMismatchException  if the requester is neither the owner nor an admin
     * @throws InvalidOrderStatusException if the order is not in PENDING_PAYMENT status
     */
    OrderDetailsDTO payOrder(Long id, String requester, CreatePaymentDTO dto);
}
