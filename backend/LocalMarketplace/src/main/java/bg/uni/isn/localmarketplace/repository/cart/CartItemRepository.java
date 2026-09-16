package bg.uni.isn.localmarketplace.repository.cart;

import bg.uni.isn.localmarketplace.domain.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
