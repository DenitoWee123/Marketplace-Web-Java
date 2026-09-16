package bg.uni.isn.localmarketplace.service.contract;

import bg.uni.isn.localmarketplace.dto.input.cart.AddCartItemDTO;
import bg.uni.isn.localmarketplace.dto.input.cart.UpdateCartItemDTO;
import bg.uni.isn.localmarketplace.dto.output.cart.CartDetailsDTO;
import bg.uni.isn.localmarketplace.exception.cart.CartItemNotFoundException;
import bg.uni.isn.localmarketplace.exception.product.InsufficientStockException;
import bg.uni.isn.localmarketplace.exception.product.ProductDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;

public interface CartService {

    /**
     * Retrieves the cart for the given user, creating it lazily if it does not exist yet.
     *
     * @param username the username of the cart owner
     * @return {@link CartDetailsDTO}
     * @throws UserNotFoundException if the user does not exist
     */
    CartDetailsDTO getCart(String username);

    /**
     * Adds a product to the user's cart. If the product is already present, its quantity is incremented.
     *
     * @param username the username of the cart owner
     * @param dto      product id and quantity to add
     * @return updated {@link CartDetailsDTO}
     * @throws UserNotFoundException        if the user does not exist
     * @throws ProductDoesNotExistException if the product does not exist
     * @throws InsufficientStockException   if the requested quantity exceeds available stock
     */
    CartDetailsDTO addItem(String username, AddCartItemDTO dto);

    /**
     * Replaces the quantity of an existing cart item.
     *
     * @param username the username of the cart owner
     * @param itemId   the cart item id
     * @param dto      the new quantity
     * @return updated {@link CartDetailsDTO}
     * @throws CartItemNotFoundException  if the item does not exist
     * @throws OwnershipMismatchException if the item does not belong to the requesting user
     * @throws InsufficientStockException if the requested quantity exceeds available stock
     */
    CartDetailsDTO updateItemQuantity(String username, Long itemId, UpdateCartItemDTO dto);

    /**
     * Removes a single item from the user's cart.
     *
     * @param username the username of the cart owner
     * @param itemId   the cart item id to remove
     * @throws CartItemNotFoundException  if the item does not exist
     * @throws OwnershipMismatchException if the item does not belong to the requesting user
     */
    void removeItem(String username, Long itemId);

    /**
     * Removes all items from the user's cart.
     *
     * @param username the username of the cart owner
     */
    void clearCart(String username);
}
