package bg.uni.isn.localmarketplace.dto.output.product;

import bg.uni.isn.localmarketplace.domain.Product;
import bg.uni.isn.localmarketplace.vo.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body containing detailed information about a product.")
public record ProductDetailsDTO(
    @Schema(description = "Unique identifier of the product", requiredMode = Schema.RequiredMode.REQUIRED)
    Long id,

    @Schema(description = "Type of the product", requiredMode = Schema.RequiredMode.REQUIRED)
    ProductType productType,

    @Schema(description = "Name of the product", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    String name,

    @Schema(description = "Description of the product", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    String description,

    @Schema(description = "Path to product image (JPG file)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String productPicturePath,

    @Schema(description = "Price of the product", requiredMode = Schema.RequiredMode.REQUIRED)
    long price,

    @Schema(description = "Quantity of the product", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    int quantity,

    @Schema(description = "Maker of the product", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    OutputProductMakerDTO maker,

    @Schema(description = "Tags attached to the product")
    java.util.List<TagDTO> tags
) {

    public static ProductDetailsDTO from(Product product) {
        return bg.uni.isn.localmarketplace.mapper.ProductDetailsMapper.toDto(product);
    }
}
