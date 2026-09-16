package bg.uni.isn.localmarketplace.service;

import bg.uni.isn.localmarketplace.domain.Product;
import bg.uni.isn.localmarketplace.domain.Review;
import bg.uni.isn.localmarketplace.domain.User;
import bg.uni.isn.localmarketplace.dto.input.review.CreateReviewDTO;
import bg.uni.isn.localmarketplace.dto.input.review.UpdateReviewDTO;
import bg.uni.isn.localmarketplace.dto.output.review.ReviewDetailsDTO;
import bg.uni.isn.localmarketplace.exception.product.ProductDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.review.ReviewDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.repository.ProductRepository;
import bg.uni.isn.localmarketplace.repository.ReviewRepository;
import bg.uni.isn.localmarketplace.repository.UserRepository;
import bg.uni.isn.localmarketplace.service.contract.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, UserRepository userRepository,
                             ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }


    @Override
    public ReviewDetailsDTO getReview(Long id) {
        return ReviewDetailsDTO.from(getReviewById(id));
    }

    @Override
    public Page<ReviewDetailsDTO> getAllReviewsOfAProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable).map(ReviewDetailsDTO::from);
    }

    @Override
    public ReviewDetailsDTO createReview(CreateReviewDTO dto, String currentUsername) {
        Product product = getProductById(dto.productId());

        // No need to check whether the user exists because currentUsername is taken from the active user
        Optional<User> user = userRepository.findById(currentUsername);

        Review newReview = new Review(user.get(), product, dto.text(), dto.rating());
        reviewRepository.save(newReview);

        return ReviewDetailsDTO.from(newReview);
    }

    @Override
    public ReviewDetailsDTO updateReview(Long id, UpdateReviewDTO dto, String currentUsername) {
        Review toUpdate = getReviewById(id);
        // No need to check whether the user exists because currentUsername is taken from the active user
        Optional<User> user = userRepository.findById(currentUsername);

        // Is it ok??? What if an admin wants to do it
        if (!toUpdate.getUser().getUsername().equals(currentUsername)) {
            throw new OwnershipMismatchException("The current user can't alter another's author review!");
        }

        toUpdate.setText(dto.text());
        toUpdate.setRating(dto.rating());

        return ReviewDetailsDTO.from(toUpdate);
    }

    @Override
    public void deleteReview(Long id, String currentUsername) {
        Review toDelete = getReviewById(id);

        // Is it ok??? What if an admin wants to do it
        if (!toDelete.getUser().getUsername().equals(currentUsername)) {
            throw new OwnershipMismatchException("The current user can't alter another's author review!");
        }

        reviewRepository.delete(toDelete);
    }

    private Review getReviewById(Long id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isEmpty()) {
            throw new ReviewDoesNotExistException("Review with id " + id + " does not exist");
        }
        return review.get();
    }

    private Product getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new ProductDoesNotExistException("Product with id " + id + " does not exist");
        }
        return product.get();
    }
}
