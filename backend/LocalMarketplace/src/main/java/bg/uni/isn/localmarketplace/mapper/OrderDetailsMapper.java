package bg.uni.isn.localmarketplace.mapper;

import bg.uni.isn.localmarketplace.domain.order.Order;
import bg.uni.isn.localmarketplace.vo.CurrencyType;
import bg.uni.isn.localmarketplace.vo.OrderStatus;
import bg.uni.isn.localmarketplace.vo.PaymentMethod;
import java.util.List;
import bg.uni.isn.localmarketplace.dto.output.order.*;

public final class OrderDetailsMapper {
    private OrderDetailsMapper() {}
    public static OrderDetailsDTO toDto(Order order) {
        List<OrderItemDTO> items = order.getOrderItems().stream()
            .map(OrderItemDTO::from)
            .toList();
        return new OrderDetailsDTO(
            order.getId(),
            order.getUser().getUsername(),
            order.getCurrency(),
            order.getTotalAmount(),
            order.getStatus(),
            items
        );
    }
}
