package bg.uni.isn.localmarketplace.dto.input.order;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import bg.uni.isn.localmarketplace.vo.CurrencyType;
import bg.uni.isn.localmarketplace.vo.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for placing an order from the current cart")
public record PlaceOrderDTO(
//    @NotNull(message = ValidationConstants.Order.NULL_PAYMENT_METHOD)
//    @Schema(description = "Payment method to use", requiredMode = Schema.RequiredMode.REQUIRED)
//    PaymentMethod paymentMethod,

    @NotNull(message = ValidationConstants.Order.NULL_CURRENCY)
    @Schema(description = "Currency for the order", requiredMode = Schema.RequiredMode.REQUIRED)
    CurrencyType currency
) {
}
