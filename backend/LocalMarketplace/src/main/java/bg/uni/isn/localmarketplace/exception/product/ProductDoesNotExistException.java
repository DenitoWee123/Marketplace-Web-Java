package bg.uni.isn.localmarketplace.exception.product;

public class ProductDoesNotExistException extends RuntimeException {
    public ProductDoesNotExistException(String message) {
        super(message);
    }
    public ProductDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
