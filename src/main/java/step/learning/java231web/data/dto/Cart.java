package step.learning.java231web.data.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Cart {
    private String id;
    private String userId;
    private String openAt;
    private String closeAt;
    private Integer status; // null for active cart, 0/1 for closed
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

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            try {
                Timestamp tsOpen = null;
                try { tsOpen = rs.getTimestamp("cart_open_at"); } catch (SQLException ignored) {}
                if (tsOpen == null) {
                    try { tsOpen = rs.getTimestamp("cart_create_dt"); } catch (SQLException ignored) {}
                }
                if (tsOpen != null) {
                    cart.setOpenAt(sdf.format(tsOpen));
                } else {
                    cart.setOpenAt(sdf.format(new Date()));
                }
            } catch (Exception ignored) {
                cart.setOpenAt(sdf.format(new Date()));
            }

            try {
                Timestamp tsClose = null;
                try { tsClose = rs.getTimestamp("cart_close_at"); } catch (SQLException ignored) {}
                if (tsClose == null) {
                    try { tsClose = rs.getTimestamp("cart_close_dt"); } catch (SQLException ignored) {}
                }
                if (tsClose != null) {
                    cart.setCloseAt(sdf.format(tsClose));
                }
            } catch (Exception ignored) {}

            try {
                Object statusObj = rs.getObject("cart_status");
                if (statusObj != null && rs.getInt("cart_status") != 0) {
                    cart.setStatus(rs.getInt("cart_status"));
                } else {
                    cart.setStatus(null);
                }
            } catch (SQLException ignored) {
                cart.setStatus(null);
            }

            try {
                cart.setPrice(rs.getDouble("cart_price"));
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

    public String getOpenAt() {
        return openAt;
    }

    public Cart setOpenAt(String openAt) {
        this.openAt = openAt;
        return this;
    }

    public Date getCreatedAt() {
        if (openAt == null) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(openAt);
        } catch (Exception e) {
            return null;
        }
    }

    public Cart setCreatedAt(Date createdAt) {
        if (createdAt != null) {
            this.openAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createdAt);
        } else {
            this.openAt = null;
        }
        return this;
    }

    public String getCloseAt() {
        return closeAt;
    }

    public Cart setCloseAt(String closeAt) {
        this.closeAt = closeAt;
        return this;
    }

    public Date getClosedAt() {
        if (closeAt == null) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(closeAt);
        } catch (Exception e) {
            return null;
        }
    }

    public Cart setClosedAt(Date closedAt) {
        if (closedAt != null) {
            this.closeAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(closedAt);
        } else {
            this.closeAt = null;
        }
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public Cart setStatus(Integer status) {
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
