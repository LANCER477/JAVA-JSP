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
    private List<CartItem> items = new ArrayList<>();

    public Cart() {
    }

    public static Cart fromResultSet(ResultSet rs) {
        try {
            String cartId = rs.getString("cart_id");
            if (cartId == null) {
                return null;
            }
            Cart cart = new Cart();
            cart.setId(cartId);
            cart.setUserId(rs.getString("user_id"));
            Timestamp tsCreated = rs.getTimestamp("cart_create_dt");
            if (tsCreated != null) {
                cart.setCreatedAt(new Date(tsCreated.getTime()));
            }
            Timestamp tsClosed = rs.getTimestamp("cart_close_dt");
            if (tsClosed != null) {
                cart.setClosedAt(new Date(tsClosed.getTime()));
            }
            cart.setStatus(rs.getInt("cart_status"));
            return cart;
        } catch (SQLException ex) {
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

    public List<CartItem> getItems() {
        return items;
    }

    public Cart setItems(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        return this;
    }
}