package bg.uni.isn.localmarketplace.dto.output.review;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body containing detailed information about the writer of a review.")
public record OutputReviewWriterDTO(
    @Schema(description = "The username of the reviewer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String name
) {
}
