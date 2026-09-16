package bg.uni.isn.localmarketplace.dto.output.order;

import bg.uni.isn.localmarketplace.domain.order.Order;
import bg.uni.isn.localmarketplace.vo.CurrencyType;
import bg.uni.isn.localmarketplace.vo.OrderStatus;
import bg.uni.isn.localmarketplace.vo.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Details of an order")
public record OrderDetailsDTO(
    @Schema(description = "Order ID")
    Long id,

    @Schema(description = "Username of the customer who placed the order")
    String username,

    @Schema(description = "Currency of the order")
    CurrencyType currency,

    @Schema(description = "Total amount of the order")
    long totalAmount,

    @Schema(description = "Current status of the order")
    OrderStatus status,

    @Schema(description = "Items in the order")
    List<OrderItemDTO> orderItems
) {

    public static OrderDetailsDTO from(Order order) {
        return bg.uni.isn.localmarketplace.mapper.OrderDetailsMapper.toDto(order);
    }
}
