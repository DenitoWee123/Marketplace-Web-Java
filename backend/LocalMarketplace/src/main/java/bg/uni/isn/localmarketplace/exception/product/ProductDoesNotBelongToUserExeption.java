package bg.uni.isn.localmarketplace.exception.product;

public class ProductDoesNotBelongToUserExeption extends RuntimeException {
    public ProductDoesNotBelongToUserExeption(String message) {
        super(message);
    }

    public ProductDoesNotBelongToUserExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
