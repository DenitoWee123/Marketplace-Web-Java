package bg.uni.isn.localmarketplace.repository.order;

import bg.uni.isn.localmarketplace.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
