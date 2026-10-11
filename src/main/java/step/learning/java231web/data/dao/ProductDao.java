package step.learning.java231web.data.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.services.db.IDbService;

@Singleton
public class ProductDao {
    private final IDbService dbService;
    private final Logger logger;

    @Inject
    public ProductDao(IDbService dbService) {
        this.dbService = dbService;
        this.logger = Logger.getLogger(ProductDao.class.getName());
    }

    public ProductDao(IDbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger != null ? logger : Logger.getLogger(ProductDao.class.getName());
    }

    public boolean addProduct(Product product) throws SQLException {
        if (dbService == null || product == null) {
            return false;
        }
        String sql = "INSERT INTO products "
                + "(product_id, product_section_id, product_title, product_description, product_slug, "
                + "product_price, product_stock, product_order, product_image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, product.getId() != null ? product.getId() : UUID.randomUUID().toString());
            prep.setString(2, product.getSectionId());
            prep.setString(3, product.getTitle());
            prep.setString(4, product.getDescription());
            prep.setString(5, product.getSlug());
            prep.setDouble(6, product.getPrice());
            prep.setInt(7, product.getStock());
            prep.setInt(8, product.getOrder());
            prep.setString(9, product.getImageUrl());
            return prep.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, sql, ex);
            throw ex;
        }
    }

    public List<Product> getProductsBySection(String sectionId, RestPagination pagination) throws SQLException {
        List<Product> list = new ArrayList<>();
        if (dbService == null) {
            return list;
        }
        int limit = pagination != null && pagination.getPageSize() > 0 ? pagination.getPageSize() : 10;
        int page = pagination != null && pagination.getPage() > 0 ? pagination.getPage() : 1;
        int offset = limit * (page - 1);

        String sql = "SELECT * FROM products "
                + "WHERE product_section_id = ? AND product_delete_dt IS NULL "
                + "ORDER BY product_order LIMIT ? OFFSET ?";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, sectionId);
            prep.setInt(2, limit);
            prep.setInt(3, offset);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    Product product = Product.fromResultSet(rs);
                    if (product != null) {
                        product.setImageUrl("" + product.getImageUrl());
                        list.add(product);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, sql, ex);
            throw ex;
        }
        return list;
    }
}
