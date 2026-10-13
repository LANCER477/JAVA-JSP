package step.learning.java231web.data.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ProductVersion {
    private String id;
    private String productId;
    private String title;
    private String description;
    private String slug;
    private double price;
    private int stock;
    private int order;
    private String imageUrl;
    private Date deletedAt;

    public ProductVersion() {
    }

    public static ProductVersion fromResultSet(ResultSet rs) {
        try {
            String id = null;
            try {
                id = rs.getString("product_version_id");
            } catch (SQLException ignored) {
            }
            if (id == null) {
                try {
                    id = rs.getString("id");
                } catch (SQLException ignored) {
                }
            }
            if (id == null) {
                return null;
            }

            ProductVersion version = new ProductVersion();
            version.setId(id);
            try {
                version.setProductId(rs.getString("product_id"));
            } catch (SQLException ignored) {}
            try {
                version.setTitle(rs.getString("product_version_title"));
            } catch (SQLException ignored) {}
            try {
                version.setDescription(rs.getString("product_version_description"));
            } catch (SQLException ignored) {}
            try {
                version.setSlug(rs.getString("product_version_slug"));
            } catch (SQLException ignored) {}
            try {
                version.setPrice(rs.getDouble("product_version_price"));
            } catch (SQLException ignored) {}
            try {
                version.setStock(rs.getInt("product_version_stock"));
            } catch (SQLException ignored) {}
            try {
                version.setOrder(rs.getInt("product_version_order"));
            } catch (SQLException ignored) {}
            try {
                version.setImageUrl(rs.getString("product_version_image"));
            } catch (SQLException ignored) {}
            try {
                Timestamp ts = rs.getTimestamp("product_version_delete_dt");
                if (ts != null) {
                    version.setDeletedAt(new Date(ts.getTime()));
                }
            } catch (SQLException ignored) {}

            return version;
        } catch (Exception ex) {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public ProductVersion setId(String id) {
        this.id = id;
        return this;
    }

    public String getProductId() {
        return productId;
    }

    public ProductVersion setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ProductVersion setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ProductVersion setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSlug() {
        return slug;
    }

    public ProductVersion setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public ProductVersion setPrice(double price) {
        this.price = price;
        return this;
    }

    public int getStock() {
        return stock;
    }

    public ProductVersion setStock(int stock) {
        this.stock = stock;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public ProductVersion setOrder(int order) {
        this.order = order;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ProductVersion setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public ProductVersion setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }
}
