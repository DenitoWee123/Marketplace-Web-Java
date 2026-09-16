package bg.uni.isn.localmarketplace.dto.output.order;

import bg.uni.isn.localmarketplace.domain.order.OrderItem;
import bg.uni.isn.localmarketplace.dto.output.product.ProductDetailsDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of a single item in an order")
public record OrderItemDTO(
    @Schema(description = "Order item ID")
    Long id,

    @Schema(description = "Product details at time of order")
    ProductDetailsDTO product,

    @Schema(description = "Quantity ordered")
    int quantity,

    @Schema(description = "Price snapshot at time of order")
    long price
) {

    public static OrderItemDTO from(OrderItem item) {
        return bg.uni.isn.localmarketplace.mapper.OrderItemMapper.toDto(item);
    }
}
