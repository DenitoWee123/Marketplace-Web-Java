package bg.uni.isn.localmarketplace.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import bg.uni.isn.localmarketplace.exception.user.UserAlreadyVendorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import bg.uni.isn.localmarketplace.exception.NotFoundException;
import bg.uni.isn.localmarketplace.exception.auth.InvalidCredentialsException;
import bg.uni.isn.localmarketplace.exception.cart.CartItemNotFoundException;
import bg.uni.isn.localmarketplace.exception.cart.EmptyCartException;
import bg.uni.isn.localmarketplace.exception.event.EventDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.event.InvalidEventDataException;
import bg.uni.isn.localmarketplace.exception.order.InvalidOrderStatusException;
import bg.uni.isn.localmarketplace.exception.order.OrderDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.payment.InvalidPaymentException;
import bg.uni.isn.localmarketplace.exception.payment.PaymentDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.product.InsufficientStockException;
import bg.uni.isn.localmarketplace.exception.product.ProductDoesNotBelongToUserExeption;
import bg.uni.isn.localmarketplace.exception.product.ProductDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.review.ReviewDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.user.EmailAlreadyExistsException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.exception.user.UserAlreadyExistsException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import bg.uni.isn.localmarketplace.response.ErrorResponse;
import bg.uni.isn.localmarketplace.response.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({org.springframework.dao.OptimisticLockingFailureException.class,
        org.springframework.dao.PessimisticLockingFailureException.class,
        jakarta.persistence.OptimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleConcurrentChange(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "Data changed concurrently. Reload and retry the operation.", request);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrityConflict(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "The operation conflicts with existing data.", request);
    }

    @ExceptionHandler({IllegalArgumentException.class, ArithmeticException.class})
    public ResponseEntity<ErrorResponse> handleInvalidArgument(RuntimeException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid input or numeric value out of range.", request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(InvalidCredentialsException ex,
                                                            HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }


    @ExceptionHandler({
        NotFoundException.class,
        UserNotFoundException.class,
        CartItemNotFoundException.class,
        ProductDoesNotExistException.class,
        ReviewDoesNotExistException.class,
        OrderDoesNotExistException.class,
        EventDoesNotExistException.class,
        PaymentDoesNotExistException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }


    @ExceptionHandler({
        org.springframework.security.access.AccessDeniedException.class,
        OwnershipMismatchException.class,
        ProductDoesNotBelongToUserExeption.class
    })
    public ResponseEntity<ErrorResponse> handleForbidden(RuntimeException ex, HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }


    @ExceptionHandler({
        EmailAlreadyExistsException.class,
        UserAlreadyExistsException.class,
        InsufficientStockException.class,
        InvalidOrderStatusException.class,
        UserAlreadyVendorException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }


    @ExceptionHandler({
        EmptyCartException.class,
        InvalidEventDataException.class,
        InvalidPaymentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                fe -> fe.getField(),
                fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                (existing, replacement) -> existing
            ));
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
        HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return buildError(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }


    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message != null ? message : status.getReasonPhrase(),
            request.getRequestURI()
        ));
    }
}
