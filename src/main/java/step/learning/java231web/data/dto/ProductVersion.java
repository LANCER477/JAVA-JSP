package step.learning.java231web.data.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ProductVersion {
    private String id;
    private String productId;
    private String titleAddon;
    private String descriptionAddon;
    private String slugAddon;
    private String imageUrl;
    private Date deletedAt;
    private int orderInPrice;
    private double price;
    private int stock;

    public ProductVersion() {
    }

    public static ProductVersion fromResultSet(ResultSet rs) {
        try {
            String id = null;
            try {
                id = rs.getString("product_version_id");
            } catch (SQLException ignored) {}
            if (id == null) {
                try {
                    id = rs.getString("id");
                } catch (SQLException ignored) {}
            }
            if (id == null) {
                return null;
            }

            ProductVersion version = new ProductVersion();
            version.setId(id);
            try {
                version.setProductId(rs.getString("product_id"));
            } catch (SQLException ignored) {}

            String title = null;
            try {
                title = rs.getString("product_version_title_addon");
            } catch (SQLException ignored) {}
            if (title == null) {
                try {
                    title = rs.getString("product_version_title");
                } catch (SQLException ignored) {}
            }
            version.setTitleAddon(title);

            String desc = null;
            try {
                desc = rs.getString("product_version_description_addon");
            } catch (SQLException ignored) {}
            if (desc == null) {
                try {
                    desc = rs.getString("product_version_description");
                } catch (SQLException ignored) {}
            }
            version.setDescriptionAddon(desc);

            String slug = null;
            try {
                slug = rs.getString("product_version_slug_addon");
            } catch (SQLException ignored) {}
            if (slug == null) {
                try {
                    slug = rs.getString("product_version_slug");
                } catch (SQLException ignored) {}
            }
            version.setSlugAddon(slug);

            try {
                version.setPrice(rs.getDouble("product_version_price"));
            } catch (SQLException ignored) {}
            try {
                version.setStock(rs.getInt("product_version_stock"));
            } catch (SQLException ignored) {}

            int order = 1;
            try {
                order = rs.getInt("product_version_order_in_price");
            } catch (SQLException ignored) {}
            if (order == 0) {
                try {
                    order = rs.getInt("product_version_order");
                } catch (SQLException ignored) {}
            }
            version.setOrderInPrice(order);

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

    public String getTitleAddon() {
        return titleAddon;
    }

    public ProductVersion setTitleAddon(String titleAddon) {
        this.titleAddon = titleAddon;
        return this;
    }

    public String getTitle() {
        return titleAddon;
    }

    public ProductVersion setTitle(String title) {
        this.titleAddon = title;
        return this;
    }

    public String getDescriptionAddon() {
        return descriptionAddon;
    }

    public ProductVersion setDescriptionAddon(String descriptionAddon) {
        this.descriptionAddon = descriptionAddon;
        return this;
    }

    public String getDescription() {
        return descriptionAddon;
    }

    public ProductVersion setDescription(String description) {
        this.descriptionAddon = description;
        return this;
    }

    public String getSlugAddon() {
        return slugAddon;
    }

    public ProductVersion setSlugAddon(String slugAddon) {
        this.slugAddon = slugAddon;
        return this;
    }

    public String getSlug() {
        return slugAddon;
    }

    public ProductVersion setSlug(String slug) {
        this.slugAddon = slug;
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

    public int getOrderInPrice() {
        return orderInPrice;
    }

    public ProductVersion setOrderInPrice(int orderInPrice) {
        this.orderInPrice = orderInPrice;
        return this;
    }

    public int getOrder() {
        return orderInPrice;
    }

    public ProductVersion setOrder(int order) {
        this.orderInPrice = order;
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
