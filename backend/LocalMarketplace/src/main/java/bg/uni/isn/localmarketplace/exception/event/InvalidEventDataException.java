package bg.uni.isn.localmarketplace.exception.event;

public class InvalidEventDataException extends RuntimeException {

    public InvalidEventDataException(String message) {
        super(message);
    }

    public InvalidEventDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
