package bg.uni.isn.localmarketplace.mapper;

import bg.uni.isn.localmarketplace.domain.cart.CartItem;
import bg.uni.isn.localmarketplace.dto.output.product.ProductDetailsDTO;
import bg.uni.isn.localmarketplace.dto.output.cart.*;

public final class CartItemMapper {
    private CartItemMapper() {}
    public static CartItemDTO toDto(CartItem item) {
        return new CartItemDTO(
            item.getId(),
            ProductDetailsDTO.from(item.getProduct()),
            item.getQuantity(),
            item.getProduct().getPrice() * item.getQuantity()
        );
    }
}
