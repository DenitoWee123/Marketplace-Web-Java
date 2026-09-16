package bg.uni.isn.localmarketplace.controller;

import bg.uni.isn.localmarketplace.dto.input.order.PlaceOrderDTO;
import bg.uni.isn.localmarketplace.dto.input.order.UpdateOrderStatusDTO;
import bg.uni.isn.localmarketplace.dto.input.payment.CreatePaymentDTO;
import bg.uni.isn.localmarketplace.dto.output.order.OrderDetailsDTO;
import bg.uni.isn.localmarketplace.response.ValidationErrorResponse;
import bg.uni.isn.localmarketplace.service.contract.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order management", description = "Endpoints for placing and managing orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place an order from cart",
        description = "Creates an order from the current user's cart. Snapshots prices, decrements stock, sets status PENDING_PAYMENT, and clears the cart.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order placed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or empty cart",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<OrderDetailsDTO> placeOrder(
        @Valid @RequestBody PlaceOrderDTO dto,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.placeOrderFromCart(principal.getName(), dto));
    }

    @GetMapping
    @Operation(summary = "List orders",
        description = "Admins receive all orders; authenticated non-admin users receive only their own.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<Page<OrderDetailsDTO>> getOrders(
        @PageableDefault(size = 20, sort = "id") Pageable pageable,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok(orderService.getOrders(principal.getName(), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID",
        description = "Returns the order with the given ID. Accessible by the order owner or an admin.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Requester is not the order owner or an admin",
            content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<OrderDetailsDTO> getOrderById(
        @Parameter(description = "Order ID", required = true) @PathVariable Long id,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok(orderService.getOrder(id, principal.getName()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status",
        description = "Owner/admin can cancel PENDING_PAYMENT orders and restore stock. Admins can move PROCESSING to SHIPPED and SHIPPED to DELIVERED. Payment uses the pay endpoint. Paid orders cannot be cancelled because refunds are not implemented.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "403", description = "Not permitted to modify this order"),
        @ApiResponse(responseCode = "409", description = "Invalid status transition or concurrent update",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<OrderDetailsDTO> updateStatus(
        @Parameter(description = "Order ID", required = true) @PathVariable Long id,
        @Valid @RequestBody UpdateOrderStatusDTO dto,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok(orderService.updateStatus(id, dto.status(), principal.getName()));
    }

    @PatchMapping("/{id}/pay")
    @Operation(summary = "Pay for an order",
        description = "Simulates payment for a PENDING_PAYMENT order. Records the payment and transitions the order status to PROCESSING. Accessible by the order owner or an admin.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment recorded, order moved to PROCESSING"),
        @ApiResponse(responseCode = "400", description = "Invalid payment data supplied",
            content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "403", description = "Requester is not the order owner or an admin",
            content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "409", description = "Order is not in PENDING_PAYMENT status",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<OrderDetailsDTO> payOrder(
        @Parameter(description = "Order ID", required = true) @PathVariable Long id,
        @Valid @RequestBody CreatePaymentDTO dto,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok(orderService.payOrder(id, principal.getName(), dto));
    }
}
