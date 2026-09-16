package bg.uni.isn.localmarketplace.mapper;

import bg.uni.isn.localmarketplace.domain.order.OrderItem;
import bg.uni.isn.localmarketplace.dto.output.product.ProductDetailsDTO;
import bg.uni.isn.localmarketplace.dto.output.order.*;

public final class OrderItemMapper {
    private OrderItemMapper() {}
    public static OrderItemDTO toDto(OrderItem item) {
        return new OrderItemDTO(
            item.getId(),
            ProductDetailsDTO.from(item.getProduct()),
            item.getQuantity(),
            item.getPrice()
        );
    }
}
