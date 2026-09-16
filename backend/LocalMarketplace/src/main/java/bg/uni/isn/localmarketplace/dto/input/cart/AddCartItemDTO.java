package bg.uni.isn.localmarketplace.dto.input.cart;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for adding an item to the cart")
public record AddCartItemDTO(
    @NotNull(message = ValidationConstants.Cart.NULL_PRODUCT_ID)
    @Schema(description = "ID of the product to add", requiredMode = Schema.RequiredMode.REQUIRED)
    Long productId,

    @Min(value = 1, message = ValidationConstants.Cart.MIN_QUANTITY)
    @Schema(description = "Quantity to add. Minimum 1.", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1")
    int quantity
) {
}
