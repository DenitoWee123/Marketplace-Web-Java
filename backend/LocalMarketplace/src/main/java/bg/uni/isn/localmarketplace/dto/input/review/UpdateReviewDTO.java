package bg.uni.isn.localmarketplace.dto.input.review;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating a review")
public record UpdateReviewDTO(
    @Size(max = 500, message = ValidationConstants.Review.LENGTH_TEXT)
    @Schema(description = "The text of the review", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String text,

    @Min(value = 0, message = ValidationConstants.Review.MIN_RATING)
    @Max(value = 5, message = ValidationConstants.Review.MAX_RATING)
    @Schema(description = "The rating of the product", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", maximum = "5")
    int rating
) {
}
