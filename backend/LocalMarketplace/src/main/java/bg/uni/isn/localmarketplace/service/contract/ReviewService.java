package bg.uni.isn.localmarketplace.service.contract;

import bg.uni.isn.localmarketplace.dto.input.review.CreateReviewDTO;
import bg.uni.isn.localmarketplace.dto.input.review.UpdateReviewDTO;
import bg.uni.isn.localmarketplace.dto.output.review.ReviewDetailsDTO;

import bg.uni.isn.localmarketplace.exception.product.ProductDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.review.ReviewDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /**
     * Returns information about a review
     *
     * @param id the id of the review
     * @return {@link ReviewDetailsDTO}
     * @throws ReviewDoesNotExistException if no review with the id exists
     */
    ReviewDetailsDTO getReview(Long id);

    /**
     * Retrieves a paginated and optionally sorted page of all review of a product converted to DTOs.
     *
     * @param pageable pagination and sorting details for the reviews
     * @return a page of product responses
     * @throws ProductDoesNotExistException if the product does not exist
     */
    Page<ReviewDetailsDTO> getAllReviewsOfAProduct(Long productId, Pageable pageable);

    /**
     * Creates a new review
     *
     * @param dto             the data for the new review
     * @param currentUsername the username of the reviewer
     * @return {@link ReviewDetailsDTO}
     * @throws ProductDoesNotExistException if the product does not exist
//   * @throws UserNotFoundException        if the user is not found
     */
    ReviewDetailsDTO createReview(CreateReviewDTO dto, String currentUsername);

    /**
     * Update a review
     *
     * @param id              the id of the review
     * @param dto             the data for the updated review
     * @param currentUsername the username of the reviewer
     * @return {@link ReviewDetailsDTO}
     * @throws ReviewDoesNotExistException if the review does not exist
     * @throws UserNotFoundException       if the user is not found
     * @throws OwnershipMismatchException  if a user different from the author of the review tries modifying the review
     */
    ReviewDetailsDTO updateReview(Long id, UpdateReviewDTO dto, String currentUsername);

    /**
     * Deletes a review
     *
     * @param id              the id of the review
     * @param currentUsername the username of the reviewer
     * @throws ReviewDoesNotExistException if the review does not exist
     * @throws OwnershipMismatchException  if a user different from the author of the review tries deleting the review
     */
    void deleteReview(Long id, String currentUsername);
}
