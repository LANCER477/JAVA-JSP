package step.learning.java231web.data.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class CartItem {
    private String id;
    private String cartId;
    private String productId;
    private String productVersionId;
    private int quantity = 1;
    private double price;
    private Date addDt;

    private Product product;
    private ProductVersion productVersion;

    public CartItem() {
    }

    public static CartItem fromResultSet(ResultSet rs) {
        try {
            String itemId = null;
            try {
                itemId = rs.getString("cart_item_id");
            } catch (SQLException ignored) {}
            if (itemId == null) {
                try {
                    itemId = rs.getString("id");
                } catch (SQLException ignored) {}
            }
            if (itemId == null) {
                return null;
            }
            CartItem item = new CartItem();
            item.setId(itemId);
            try {
                item.setCartId(rs.getString("cart_id"));
            } catch (SQLException ignored) {}
            try {
                item.setProductId(rs.getString("product_id"));
            } catch (SQLException ignored) {}
            try {
                item.setProductVersionId(rs.getString("product_version_id"));
            } catch (SQLException ignored) {}

            int q = 1;
            try {
                q = rs.getInt("cnt");
            } catch (SQLException ignored) {
                try {
                    q = rs.getInt("quantity");
                } catch (SQLException ignored2) {}
            }
            item.setQuantity(q);

            try {
                item.setPrice(rs.getDouble("price"));
            } catch (SQLException ignored) {}

            try {
                Timestamp ts = rs.getTimestamp("add_dt");
                if (ts != null) {
                    item.setAddDt(new Date(ts.getTime()));
                }
            } catch (SQLException ignored) {}

            return item;
        } catch (Exception ex) {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public CartItem setId(String id) {
        this.id = id;
        return this;
    }

    public String getCartId() {
        return cartId;
    }

    public CartItem setCartId(String cartId) {
        this.cartId = cartId;
        return this;
    }

    public String getProductId() {
        return productId;
    }

    public CartItem setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getProductVersionId() {
        return productVersionId;
    }

    public CartItem setProductVersionId(String productVersionId) {
        this.productVersionId = productVersionId;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public CartItem setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public int getCount() {
        return quantity;
    }

    public CartItem setCount(int count) {
        this.quantity = count;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public CartItem setPrice(double price) {
        this.price = price;
        return this;
    }

    public Date getAddDt() {
        return addDt;
    }

    public CartItem setAddDt(Date addDt) {
        this.addDt = addDt;
        return this;
    }

    public Product getProduct() {
        return product;
    }

    public CartItem setProduct(Product product) {
        this.product = product;
        return this;
    }

    public ProductVersion getProductVersion() {
        return productVersion;
    }

    public CartItem setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
        return this;
    }
}
