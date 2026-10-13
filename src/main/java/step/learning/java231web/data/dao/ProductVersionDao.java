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
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.services.db.IDbService;

@Singleton
public class ProductVersionDao {
    private final IDbService dbService;
    private final Logger logger;
    private boolean isInstalled = false;

    @Inject
    public ProductVersionDao(IDbService dbService) {
        this.dbService = dbService;
        this.logger = Logger.getLogger(ProductVersionDao.class.getName());
    }

    public ProductVersionDao(IDbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger != null ? logger : Logger.getLogger(ProductVersionDao.class.getName());
    }

    public synchronized void ensureInstalled() {
        if (isInstalled || dbService == null) {
            return;
        }
        try {
            createTable();
            isInstalled = true;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Auto install error for product_versions", ex);
        }
    }

    public void createTable() throws SQLException {
        if (dbService == null) {
            return;
        }
        String sql = "CREATE TABLE IF NOT EXISTS product_versions ("
                + "product_version_id CHAR(36) PRIMARY KEY, "
                + "product_id CHAR(36) NOT NULL, "
                + "product_version_title VARCHAR(256) NOT NULL, "
                + "product_version_description TEXT NULL, "
                + "product_version_slug VARCHAR(256) NOT NULL, "
                + "product_version_price DECIMAL(10, 2) NOT NULL DEFAULT 0, "
                + "product_version_stock INT NOT NULL DEFAULT 0, "
                + "product_version_order INT NOT NULL DEFAULT 10000, "
                + "product_version_image VARCHAR(256) NULL, "
                + "product_version_delete_dt DATETIME NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error creating product_versions table", ex);
            throw ex;
        }
    }

    public boolean addProductVersion(ProductVersion version) throws SQLException {
        if (dbService == null || version == null) {
            return false;
        }
        ensureInstalled();
        String sql = "INSERT INTO product_versions "
                + "(product_version_id, product_id, product_version_title, product_version_description, "
                + "product_version_slug, product_version_price, product_version_stock, product_version_order, product_version_image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, version.getId() != null ? version.getId() : UUID.randomUUID().toString());
            prep.setString(2, version.getProductId());
            prep.setString(3, version.getTitle());
            prep.setString(4, version.getDescription());
            prep.setString(5, version.getSlug());
            prep.setDouble(6, version.getPrice());
            prep.setInt(7, version.getStock());
            prep.setInt(8, version.getOrder());
            prep.setString(9, version.getImageUrl());
            return prep.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, sql, ex);
            throw ex;
        }
    }

    public List<ProductVersion> getVersionsByProductId(String productId) {
        List<ProductVersion> list = new ArrayList<>();
        if (dbService == null || productId == null) {
            return list;
        }
        ensureInstalled();
        String sql = "SELECT * FROM product_versions "
                + "WHERE product_id = ? AND product_version_delete_dt IS NULL "
                + "ORDER BY product_version_order";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, productId);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    ProductVersion v = ProductVersion.fromResultSet(rs);
                    if (v != null) {
                        list.add(v);
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error getting versions by product: " + ex.getMessage());
        }
        return list;
    }

    public List<ProductVersion> getAllVersions() {
        List<ProductVersion> list = new ArrayList<>();
        if (dbService == null) {
            return list;
        }
        ensureInstalled();
        String sql = "SELECT * FROM product_versions "
                + "WHERE product_version_delete_dt IS NULL "
                + "ORDER BY product_version_order";
        try (Statement stmt = dbService.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ProductVersion v = ProductVersion.fromResultSet(rs);
                if (v != null) {
                    list.add(v);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error getting all versions: " + ex.getMessage());
        }
        return list;
    }
}
