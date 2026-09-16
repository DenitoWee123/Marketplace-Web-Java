package bg.uni.isn.localmarketplace.domain.cart;

import bg.uni.isn.localmarketplace.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    protected CartItem() {

    }

    public CartItem(Cart cart, Product product, int quantity) {
        if (cart == null) {
            throw new IllegalArgumentException("cart cannot be null");
        } else if (product == null) {
            throw new IllegalArgumentException("product cannot be null");
        } else if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public CartItem(Long id, Cart cart, Product product, int quantity) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        } else if (product == null) {
            throw new IllegalArgumentException("product cannot be null");
        } else if (quantity < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        this.id = id;
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CartItem cartItem)) return false;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
