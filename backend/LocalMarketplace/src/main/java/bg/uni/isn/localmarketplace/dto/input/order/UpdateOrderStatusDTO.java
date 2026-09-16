package bg.uni.isn.localmarketplace.dto.input.order;

import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import bg.uni.isn.localmarketplace.vo.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for updating the status of an order")
public record UpdateOrderStatusDTO(
    @NotNull(message = ValidationConstants.Order.NULL_STATUS)
    @Schema(description = "New order status", requiredMode = Schema.RequiredMode.REQUIRED)
    OrderStatus status
) {
}
