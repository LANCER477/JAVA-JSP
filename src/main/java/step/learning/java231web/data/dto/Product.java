package step.learning.java231web.data.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Product {
    private String id;
    private String sectionId;
    private String title;
    private String description;
    private String slug;
    private transient double price;
    private transient int stock;
    private transient int order;
    private String imageUrl;
    private Date deletedAt;
    private Integer orderInPrice;
    private List<ProductVersion> versions = new ArrayList<>();

    public Product() {
    }

    public static Product fromResultSet(ResultSet rs) {
        try {
            String productId = rs.getString("product_id");
            if (productId == null) {
                return null;
            }
            Product product = new Product();
            product.setId(productId);
            product.setSectionId(rs.getString("product_section_id"));
            product.setTitle(rs.getString("product_title"));
            product.setDescription(rs.getString("product_description"));
            product.setSlug(rs.getString("product_slug"));
            product.setPrice(rs.getDouble("product_price"));
            product.setStock(rs.getInt("product_stock"));
            int orderVal = rs.getInt("product_order");
            product.setOrder(orderVal);
            product.setOrderInPrice(orderVal);
            product.setImageUrl(rs.getString("product_image"));
            Timestamp ts = rs.getTimestamp("product_delete_dt");
            if (ts != null) {
                product.setDeletedAt(new Date(ts.getTime()));
            }
            return product;
        } catch (SQLException ex) {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public Product setId(String id) {
        this.id = id;
        return this;
    }

    public String getSectionId() {
        return sectionId;
    }

    public Product setSectionId(String sectionId) {
        this.sectionId = sectionId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Product setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Product setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSlug() {
        return slug;
    }

    public Product setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public Product setPrice(double price) {
        this.price = price;
        return this;
    }

    public int getStock() {
        return stock;
    }

    public Product setStock(int stock) {
        this.stock = stock;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public Product setOrder(int order) {
        this.order = order;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Product setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public Product setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public Integer getOrderInPrice() {
        return orderInPrice != null ? orderInPrice : order;
    }

    public Product setOrderInPrice(Integer orderInPrice) {
        this.orderInPrice = orderInPrice;
        if (orderInPrice != null) {
            this.order = orderInPrice;
        }
        return this;
    }

    public List<ProductVersion> getVersions() {
        return versions;
    }

    public Product setVersions(List<ProductVersion> versions) {
        this.versions = versions != null ? versions : new ArrayList<>();
        return this;
    }
}
