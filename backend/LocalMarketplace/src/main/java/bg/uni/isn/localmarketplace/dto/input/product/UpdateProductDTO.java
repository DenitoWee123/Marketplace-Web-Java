package bg.uni.isn.localmarketplace.dto.input.product;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateProductDTO(
    @Size(max = 100, message = ValidationConstants.Product.LENGTH_NAME)
    @Schema(description = "Name of the product. Maximum 100 characters.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 100)
    String name,

    @Size(max = 100, message = ValidationConstants.Product.LENGTH_DESCRIPTION)
    @Schema(description = "Description of the product. Maximum 100 characters.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 100)
    String description,

    @Min(value = 0, message = ValidationConstants.Product.MIN_PRICE)
    @Schema(description = "The price of the product. Minimum - 0 {price}", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    long price,

    @Min(value = 0, message = ValidationConstants.Product.MIN_UPDATE_QUANTITY)
    @Schema(description = "Quantity of the product. Minimum 0", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    int quantity
) {
}