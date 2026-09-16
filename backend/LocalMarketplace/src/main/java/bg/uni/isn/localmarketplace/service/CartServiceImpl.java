package bg.uni.isn.localmarketplace.service;

import bg.uni.isn.localmarketplace.domain.Product;
import bg.uni.isn.localmarketplace.domain.User;
import bg.uni.isn.localmarketplace.domain.cart.Cart;
import bg.uni.isn.localmarketplace.domain.cart.CartItem;
import bg.uni.isn.localmarketplace.dto.input.cart.AddCartItemDTO;
import bg.uni.isn.localmarketplace.dto.input.cart.UpdateCartItemDTO;
import bg.uni.isn.localmarketplace.dto.output.cart.CartDetailsDTO;
import bg.uni.isn.localmarketplace.exception.cart.CartItemNotFoundException;
import bg.uni.isn.localmarketplace.exception.product.InsufficientStockException;
import bg.uni.isn.localmarketplace.exception.product.ProductDoesNotExistException;
import bg.uni.isn.localmarketplace.exception.user.OwnershipMismatchException;
import bg.uni.isn.localmarketplace.exception.user.UserNotFoundException;
import bg.uni.isn.localmarketplace.repository.ProductRepository;
import bg.uni.isn.localmarketplace.repository.UserRepository;
import bg.uni.isn.localmarketplace.repository.cart.CartItemRepository;
import bg.uni.isn.localmarketplace.repository.cart.CartRepository;
import bg.uni.isn.localmarketplace.service.contract.CartService;
import bg.uni.isn.localmarketplace.utils.ValidationConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
                           UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public CartDetailsDTO getCart(String username) {
        User user = getUser(username);
        Cart cart = getOrCreateCart(user);
        return CartDetailsDTO.from(cart);
    }

    @Override
    public CartDetailsDTO addItem(String username, AddCartItemDTO dto) {
        User user = getUser(username);
        Cart cart = getOrCreateCart(user);
        Product product = getProduct(dto.productId());

        Optional<CartItem> existing = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(product.getId()))
            .findFirst();

        if (existing.isPresent()) {
            incrementExistingItem(existing.get(), dto.quantity(), product);
        } else {
            addNewItemToCart(cart, product, dto.quantity());
        }

        return CartDetailsDTO.from(cart);
    }

    @Override
    public CartDetailsDTO updateItemQuantity(String username, Long itemId, UpdateCartItemDTO dto) {
        CartItem item = getOwnedCartItem(username, itemId);
        validateStock(item.getProduct(), dto.quantity());
        item.setQuantity(dto.quantity());
        return CartDetailsDTO.from(item.getCart());
    }

    @Override
    public void removeItem(String username, Long itemId) {
        CartItem item = getOwnedCartItem(username, itemId);
        item.getCart().getItems().remove(item);
    }

    @Override
    public void clearCart(String username) {
        getUser(username);
        cartRepository.findByUser_Username(username).ifPresent(cart -> cart.getItems().clear());
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser_Username(user.getUsername())
            .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    private CartItem getOwnedCartItem(String username, Long itemId) {
        getUser(username);
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new CartItemNotFoundException("Cart item with id " + itemId + " does not exist"));
        if (!item.getCart().getUser().getUsername().equals(username)) {
            throw new OwnershipMismatchException(ValidationConstants.Cart.ITEM_NOT_OWNED);
        }
        return item;
    }

    private void incrementExistingItem(CartItem item, int additionalQty, Product product) {
        int newQty = Math.addExact(item.getQuantity(), additionalQty);
        validateStock(product, newQty);
        item.setQuantity(newQty);
    }

    private void addNewItemToCart(Cart cart, Product product, int quantity) {
        validateStock(product, quantity);
        CartItem saved = cartItemRepository.save(new CartItem(cart, product, quantity));
        cart.getItems().add(saved);
    }

    private void validateStock(Product product, int requestedQty) {
        if (requestedQty < 1 || requestedQty > product.getQuantity()) {
            throw new InsufficientStockException(ValidationConstants.Cart.INSUFFICIENT_STOCK);
        }
    }

    private User getUser(String username) {
        return userRepository.findForUpdate(username)
            .orElseThrow(() -> new UserNotFoundException("User with username " + username + " does not exist"));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductDoesNotExistException("Product with id " + productId + " does not exist"));
    }
}
