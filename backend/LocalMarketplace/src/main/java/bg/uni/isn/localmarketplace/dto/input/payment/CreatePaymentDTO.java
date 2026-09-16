package bg.uni.isn.localmarketplace.dto.input.payment;

import bg.uni.isn.localmarketplace.vo.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a new payment")
public record CreatePaymentDTO(
    @Schema(description = "The payment method.", requiredMode = Schema.RequiredMode.REQUIRED)
    @jakarta.validation.constraints.NotNull
    PaymentMethod paymentMethod
) {
}