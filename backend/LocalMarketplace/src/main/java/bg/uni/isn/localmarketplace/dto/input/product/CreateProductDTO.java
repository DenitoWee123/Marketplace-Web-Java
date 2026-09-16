package bg.uni.isn.localmarketplace.dto.input.product;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import bg.uni.isn.localmarketplace.vo.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a new product")
public record CreateProductDTO(
    @NotNull(message = ValidationConstants.Product.NULL_TYPE)
    @Schema(description = "The type of the product", requiredMode = Schema.RequiredMode.REQUIRED)
    ProductType productType,

    @NotBlank(message = ValidationConstants.Product.BLANK_NAME)
    @Size(max = 100, message = ValidationConstants.Product.LENGTH_NAME)
    @Schema(description = "Name of the product. Maximum 100 characters.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    String name,

    @Size(max = 100, message = ValidationConstants.Product.LENGTH_DESCRIPTION)
    @Schema(description = "Description of the product. Maximum 100 characters.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = 100)
    String description,

    @Min(value = 0, message = ValidationConstants.Product.MIN_PRICE)
    @Schema(description = "The price of the product. Minimum - 0 {price}", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    long price,

    @Min(value = 1, message = ValidationConstants.Product.MIN_QUANTITY)
    @Schema(description = "Quantity of the product. Minimum 1", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1")
    int quantity
) {
}
