package bg.uni.isn.localmarketplace.controller;

import bg.uni.isn.localmarketplace.dto.input.cart.AddCartItemDTO;
import bg.uni.isn.localmarketplace.dto.input.cart.UpdateCartItemDTO;
import bg.uni.isn.localmarketplace.dto.output.cart.CartDetailsDTO;
import bg.uni.isn.localmarketplace.response.ValidationErrorResponse;
import bg.uni.isn.localmarketplace.service.contract.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users/me/cart")
@Tag(name = "Cart management", description = "Endpoints for managing the current user's shopping cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Get current user's cart",
        description = "Returns the shopping cart for the currently authenticated user, creating it if it does not exist yet.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<CartDetailsDTO> getCart(@Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getName()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart",
        description = "Adds a product to the cart. If the product is already present, its quantity is incremented.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Item added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<CartDetailsDTO> addItem(
        @Valid @RequestBody AddCartItemDTO dto,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(principal.getName(), dto));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity",
        description = "Replaces the quantity of an existing cart item. Must be ≥ 1 and ≤ product stock.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid quantity",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cart item not found",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<CartDetailsDTO> updateItem(
        @Parameter(description = "Cart item ID", required = true) @PathVariable Long itemId,
        @Valid @RequestBody UpdateCartItemDTO dto,
        @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok(cartService.updateItemQuantity(principal.getName(), itemId, dto));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes a single item from the current user's cart.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Item removed successfully"),
        @ApiResponse(responseCode = "404", description = "Cart item not found",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<Void> removeItem(
        @Parameter(description = "Cart item ID", required = true) @PathVariable Long itemId,
        @Parameter(hidden = true) Principal principal) {
        cartService.removeItem(principal.getName(), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the current user's cart.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cart cleared successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema()))
    })
    public ResponseEntity<Void> clearCart(@Parameter(hidden = true) Principal principal) {
        cartService.clearCart(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
