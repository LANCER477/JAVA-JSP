package step.learning.java231web.data.dto;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class ProductVersion {
    private UUID id;
    private UUID productId;
    private String titleAddon;
    private String descriptionAddon;
    private String slugAddon;
    private String imageUrl;
    private Date deletedAt;
    private long orderInPrice;
    private double price;
    private long stock;

    private Product product;

    public ProductVersion() {
    }

    public static ProductVersion fromResultSet(ResultSet rs) throws SQLException {
        Timestamp t = null;
        try {
            t = rs.getTimestamp("product_version_deleted_at");
        } catch (SQLException ignored) {}
        if (t == null) {
            try {
                t = rs.getTimestamp("product_version_delete_dt");
            } catch (SQLException ignored) {}
        }

        String idStr = null;
        try {
            idStr = rs.getString("product_version_id");
        } catch (SQLException ignored) {}
        if (idStr == null) {
            try {
                idStr = rs.getString("id");
            } catch (SQLException ignored) {}
        }

        String prodIdStr = null;
        try {
            prodIdStr = rs.getString("product_id");
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

        String desc = null;
        try {
            desc = rs.getString("product_version_description_addon");
        } catch (SQLException ignored) {}
        if (desc == null) {
            try {
                desc = rs.getString("product_version_description");
            } catch (SQLException ignored) {}
        }

        String slug = null;
        try {
            slug = rs.getString("product_version_slug_addon");
        } catch (SQLException ignored) {}
        if (slug == null) {
            try {
                slug = rs.getString("product_version_slug");
            } catch (SQLException ignored) {}
        }

        String image = null;
        try {
            image = rs.getString("product_version_image_url");
        } catch (SQLException ignored) {}
        if (image == null) {
            try {
                image = rs.getString("product_version_image");
            } catch (SQLException ignored) {}
        }

        long order = 1;
        try {
            order = rs.getLong("product_version_order");
        } catch (SQLException ignored) {}
        if (order == 0) {
            try {
                order = rs.getLong("product_version_order_in_price");
            } catch (SQLException ignored) {}
        }

        double price = 0;
        try {
            price = rs.getDouble("product_version_price");
        } catch (SQLException ignored) {}

        long stock = 0;
        try {
            stock = rs.getLong("product_version_stock");
        } catch (SQLException ignored) {}

        ProductVersion pv = new ProductVersion();
        if (idStr != null) {
            pv.setId(idStr);
        }
        if (prodIdStr != null) {
            pv.setProductId(prodIdStr);
        }
        pv.setTitleAddon(title)
                .setDescriptionAddon(desc)
                .setSlugAddon(slug)
                .setImageUrl(image)
                .setDeletedAt(t == null ? null : new Date(t.getTime()))
                .setOrderInPrice(order)
                .setPrice(price)
                .setStock(stock);

        try {
            if (rs.getString("product_title") != null) {
                pv.setProduct(Product.fromResultSet(rs));
            }
        } catch (SQLException ignored) {}

        return pv;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public UUID getId() {
        return id;
    }

    public ProductVersion setId(UUID id) {
        this.id = id;
        return this;
    }

    public ProductVersion setId(String id) {
        if (id == null) {
            this.id = null;
        } else {
            try {
                this.id = UUID.fromString(id);
            } catch (IllegalArgumentException ex) {
                this.id = UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8));
            }
        }
        return this;
    }

    public String getIdAsString() {
        return id != null ? id.toString() : null;
    }

    public UUID getProductId() {
        return productId;
    }

    public ProductVersion setProductId(UUID productId) {
        this.productId = productId;
        return this;
    }

    public ProductVersion setProductId(String productId) {
        if (productId == null) {
            this.productId = null;
        } else {
            try {
                this.productId = UUID.fromString(productId);
            } catch (IllegalArgumentException ex) {
                this.productId = UUID.nameUUIDFromBytes(productId.getBytes(StandardCharsets.UTF_8));
            }
        }
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

    public long getOrderInPrice() {
        return orderInPrice;
    }

    public ProductVersion setOrderInPrice(long orderInPrice) {
        this.orderInPrice = orderInPrice;
        return this;
    }

    public long getOrder() {
        return orderInPrice;
    }

    public ProductVersion setOrder(long order) {
        this.orderInPrice = order;
        return this;
    }

    public ProductVersion setOrder(int order) {
        this.orderInPrice = order;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public ProductVersion setPrice(double price) {
        this.price = price;
        return this;
    }

    public long getStock() {
        return stock;
    }

    public ProductVersion setStock(long stock) {
        this.stock = stock;
        return this;
    }

    public ProductVersion setStock(int stock) {
        this.stock = stock;
        return this;
    }
}
