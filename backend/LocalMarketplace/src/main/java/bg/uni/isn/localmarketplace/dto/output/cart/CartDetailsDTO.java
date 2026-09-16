package bg.uni.isn.localmarketplace.dto.output.cart;

import bg.uni.isn.localmarketplace.domain.cart.Cart;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Details of the current user's shopping cart")
public record CartDetailsDTO(
    @Schema(description = "Cart ID")
    Long id,

    @Schema(description = "Items in the cart")
    List<CartItemDTO> items,

    @Schema(description = "Total price of all items in the cart")
    long total
) {

    public static CartDetailsDTO from(Cart cart) {
        return bg.uni.isn.localmarketplace.mapper.CartDetailsMapper.toDto(cart);
    }
}
