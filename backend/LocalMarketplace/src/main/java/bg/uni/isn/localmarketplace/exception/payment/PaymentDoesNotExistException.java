package bg.uni.isn.localmarketplace.exception.payment;

public class PaymentDoesNotExistException extends RuntimeException {
    public PaymentDoesNotExistException(String message) {
        super(message);
    }

    public PaymentDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
