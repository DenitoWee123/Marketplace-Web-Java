package bg.uni.isn.localmarketplace.mapper;

import bg.uni.isn.localmarketplace.domain.Review;
import bg.uni.isn.localmarketplace.dto.output.review.*;

public final class ReviewDetailsMapper {
    private ReviewDetailsMapper() {}
    public static ReviewDetailsDTO toDto(Review review) {
        return new ReviewDetailsDTO(review.getId(), review.getText(), review.getRating(),
            new OutputReviewWriterDTO(review.getUser().getUsername()));
    }
}
