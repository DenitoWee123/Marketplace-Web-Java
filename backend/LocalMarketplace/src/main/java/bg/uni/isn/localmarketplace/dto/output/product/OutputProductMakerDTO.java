package bg.uni.isn.localmarketplace.dto.output.product;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body containing basic information about the maker of a product.")
public record OutputProductMakerDTO(
    @Schema(description = "Username of the user.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    String username,

    @Schema(description = "Email of the user.", requiredMode = Schema.RequiredMode.REQUIRED)
    String email
) {
}
