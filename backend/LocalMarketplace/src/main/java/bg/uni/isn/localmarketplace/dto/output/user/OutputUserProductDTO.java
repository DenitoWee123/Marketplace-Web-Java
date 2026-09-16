package bg.uni.isn.localmarketplace.dto.output.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body containing basic product information of a user.")
public record OutputUserProductDTO(
    @Schema(description = "Description of the product", requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(description = "Price of the product", requiredMode = Schema.RequiredMode.REQUIRED)
    long price,

    @Schema(description = "Quantity of the product", requiredMode = Schema.RequiredMode.REQUIRED)
    int quantity
) {
}
