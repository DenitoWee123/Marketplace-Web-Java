package bg.uni.isn.localmarketplace.mapper;

import bg.uni.isn.localmarketplace.domain.cart.Cart;
import java.util.List;
import bg.uni.isn.localmarketplace.dto.output.cart.*;

public final class CartDetailsMapper {
    private CartDetailsMapper() {}
    public static CartDetailsDTO toDto(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
            .map(CartItemDTO::from)
            .toList();
        long total = itemDTOs.stream().mapToLong(CartItemDTO::subtotal).sum();
        return new CartDetailsDTO(cart.getId(), itemDTOs, total);
    }
}
