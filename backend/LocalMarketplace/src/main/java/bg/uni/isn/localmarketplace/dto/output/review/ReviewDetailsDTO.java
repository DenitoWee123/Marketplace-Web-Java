package bg.uni.isn.localmarketplace.dto.output.review;

import bg.uni.isn.localmarketplace.domain.Review;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body containing detailed information about a review of a product.")
public record ReviewDetailsDTO(
    @Schema(description = "Id of the review", requiredMode = Schema.RequiredMode.REQUIRED)
    Long id,

    @Schema(description = "The text of the review", requiredMode = Schema.RequiredMode.REQUIRED)
    String text,

    @Schema(description = "The rating of the product", requiredMode = Schema.RequiredMode.REQUIRED)
    int rating,

    @Schema(description = "The writer of the review", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    OutputReviewWriterDTO reviewWriterDTO
) {

    public static ReviewDetailsDTO from(Review review) {
        return bg.uni.isn.localmarketplace.mapper.ReviewDetailsMapper.toDto(review);
    }
}
