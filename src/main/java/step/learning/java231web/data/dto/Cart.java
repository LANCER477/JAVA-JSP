package step.learning.java231web.data.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Cart {
    private String id;
    private String userId;
    private Date createdAt;
    private Date closedAt;
    private int status; // 0 - active, 1 - closed
    private double price;
    private List<CartItem> cartItems = new ArrayList<>();

    public Cart() {
    }

    public static Cart fromResultSet(ResultSet rs) {
        try {
            String cartId = null;
            try {
                cartId = rs.getString("cart_id");
            } catch (SQLException ignored) {}
            if (cartId == null) {
                try {
                    cartId = rs.getString("id");
                } catch (SQLException ignored) {}
            }
            if (cartId == null) {
                return null;
            }
            Cart cart = new Cart();
            cart.setId(cartId);
            try {
                cart.setUserId(rs.getString("user_id"));
            } catch (SQLException ignored) {}
            try {
                Timestamp tsCreated = rs.getTimestamp("cart_create_dt");
                if (tsCreated != null) {
                    cart.setCreatedAt(new Date(tsCreated.getTime()));
                }
            } catch (SQLException ignored) {}
            try {
                Timestamp tsClosed = rs.getTimestamp("cart_close_dt");
                if (tsClosed != null) {
                    cart.setClosedAt(new Date(tsClosed.getTime()));
                }
            } catch (SQLException ignored) {}
            try {
                cart.setStatus(rs.getInt("cart_status"));
            } catch (SQLException ignored) {}
            return cart;
        } catch (Exception ex) {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public Cart setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public Cart setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Cart setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public Cart setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Cart setStatus(int status) {
        this.status = status;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public Cart setPrice(double price) {
        this.price = price;
        return this;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public Cart setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        return this;
    }

    public List<CartItem> getItems() {
        return cartItems;
    }

    public Cart setItems(List<CartItem> items) {
        return setCartItems(items);
    }
}
