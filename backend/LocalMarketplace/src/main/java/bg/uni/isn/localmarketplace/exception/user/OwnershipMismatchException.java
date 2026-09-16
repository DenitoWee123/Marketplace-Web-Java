package bg.uni.isn.localmarketplace.exception.user;

public class OwnershipMismatchException extends RuntimeException {
    public OwnershipMismatchException(String message) {
        super(message);
    }

    public OwnershipMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
