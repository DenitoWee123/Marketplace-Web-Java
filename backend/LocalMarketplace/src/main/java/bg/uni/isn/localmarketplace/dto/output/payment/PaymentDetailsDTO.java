package bg.uni.isn.localmarketplace.dto.output.payment;

import bg.uni.isn.localmarketplace.domain.Payment;
import bg.uni.isn.localmarketplace.domain.order.Order;
import bg.uni.isn.localmarketplace.vo.CurrencyType;
import bg.uni.isn.localmarketplace.vo.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body containing detailed information about a payment.")
public record PaymentDetailsDTO(
    @Schema(description = "Id of the payment.", requiredMode = Schema.RequiredMode.REQUIRED)
    Long id,

    @Schema(description = "Description of the order.", requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentOrderDTO orderDTO,

    @Schema(description = "The amount to be paid.", requiredMode = Schema.RequiredMode.REQUIRED)
    long amount,

    @Schema(description = "The currency fot the payment.", requiredMode = Schema.RequiredMode.AUTO)
    CurrencyType currencyType,

    @Schema(description = "The payment method.", requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentMethod paymentMethod
) {

    public static PaymentDetailsDTO from(Payment payment) {
        return bg.uni.isn.localmarketplace.mapper.PaymentDetailsMapper.toDto(payment);
    }
}
