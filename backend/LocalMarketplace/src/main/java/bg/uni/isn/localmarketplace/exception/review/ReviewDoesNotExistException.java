package bg.uni.isn.localmarketplace.exception.review;

public class ReviewDoesNotExistException extends RuntimeException {
    public ReviewDoesNotExistException(String message) {
        super(message);
    }

    public ReviewDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
