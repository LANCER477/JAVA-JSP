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
        isInstalled = true;
        try {
            createTable();
            seedSampleDataIfEmpty();
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
                + "product_version_title VARCHAR(256) NULL, "
                + "product_version_title_addon VARCHAR(256) NULL, "
                + "product_version_description TEXT NULL, "
                + "product_version_description_addon TEXT NULL, "
                + "product_version_slug VARCHAR(256) NULL, "
                + "product_version_slug_addon VARCHAR(256) NULL, "
                + "product_version_price DECIMAL(10, 2) NOT NULL DEFAULT 0, "
                + "product_version_stock INT NOT NULL DEFAULT 0, "
                + "product_version_order INT NOT NULL DEFAULT 10000, "
                + "product_version_order_in_price INT NOT NULL DEFAULT 10000, "
                + "product_version_image VARCHAR(256) NULL, "
                + "product_version_delete_dt DATETIME NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
            
            // Add columns if table already existed with older schema
            try { stmt.executeUpdate("ALTER TABLE product_versions ADD COLUMN product_version_title_addon VARCHAR(256) NULL"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE product_versions ADD COLUMN product_version_description_addon TEXT NULL"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE product_versions ADD COLUMN product_version_slug_addon VARCHAR(256) NULL"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE product_versions ADD COLUMN product_version_order_in_price INT NOT NULL DEFAULT 10000"); } catch (Exception ignored) {}
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
                + "(product_version_id, product_id, product_version_title_addon, product_version_title, "
                + "product_version_description_addon, product_version_description, "
                + "product_version_slug_addon, product_version_slug, "
                + "product_version_price, product_version_stock, product_version_order_in_price, product_version_order, product_version_image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, version.getId() != null ? version.getId() : UUID.randomUUID().toString());
            prep.setString(2, version.getProductId());
            prep.setString(3, version.getTitleAddon());
            prep.setString(4, version.getTitleAddon());
            prep.setString(5, version.getDescriptionAddon());
            prep.setString(6, version.getDescriptionAddon());
            prep.setString(7, version.getSlugAddon());
            prep.setString(8, version.getSlugAddon());
            prep.setDouble(9, version.getPrice());
            prep.setInt(10, version.getStock());
            prep.setInt(11, version.getOrderInPrice() > 0 ? version.getOrderInPrice() : 1);
            prep.setInt(12, version.getOrderInPrice() > 0 ? version.getOrderInPrice() : 1);
            prep.setString(13, version.getImageUrl());
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
                + "ORDER BY COALESCE(product_version_order_in_price, product_version_order, 10000), product_version_id";
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

    public ProductVersion getVersionById(String versionId) {
        if (dbService == null || versionId == null || versionId.trim().isEmpty()) {
            return null;
        }
        ensureInstalled();
        String sql = "SELECT * FROM product_versions WHERE product_version_id = ?";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, versionId.trim());
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    return ProductVersion.fromResultSet(rs);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error getting version by id: " + ex.getMessage());
        }
        return null;
    }

    public List<ProductVersion> getAllVersions() {
        List<ProductVersion> list = new ArrayList<>();
        if (dbService == null) {
            return list;
        }
        ensureInstalled();
        String sql = "SELECT * FROM product_versions "
                + "WHERE product_version_delete_dt IS NULL "
                + "ORDER BY COALESCE(product_version_order_in_price, product_version_order, 10000)";
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

    private void seedSampleDataIfEmpty() {
        try {
            String iphoneId = "29cc99c6-ab15-4a6a-8e19-12b91244401a";
            String androidSectionId = "3c6bee19-cada-446a-b6f5-deb5c46d01f2";

            // 1. Ensure android section exists with id 3c6bee19-cada-446a-b6f5-deb5c46d01f2
            String checkSecSql = "SELECT product_section_id FROM product_sections WHERE product_section_slug = 'android'";
            try (Statement stmtSec = dbService.getConnection().createStatement();
                 ResultSet rsSec = stmtSec.executeQuery(checkSecSql)) {
                if (rsSec.next()) {
                    androidSectionId = rsSec.getString(1);
                    try (Statement updateStmt = dbService.getConnection().createStatement()) {
                        updateStmt.executeUpdate("UPDATE product_sections SET product_section_image = '48846693-e5e7-4fe6-be55-36d9832bb52d.webp', product_section_order = 5 WHERE product_section_id = '" + androidSectionId + "'");
                    }
                } else {
                    String insertSecSql = "INSERT INTO product_sections "
                            + "(product_section_id, product_section_parent_id, product_section_title, product_section_description, product_section_slug, product_section_image, product_section_order) "
                            + "VALUES (?, NULL, 'Смартфони', 'Смартфони Android', 'android', '48846693-e5e7-4fe6-be55-36d9832bb52d.webp', 5)";
                    try (PreparedStatement prepSec = dbService.getConnection().prepareStatement(insertSecSql)) {
                        prepSec.setString(1, androidSectionId);
                        prepSec.executeUpdate();
                    }
                }
            }

            // 2. Ensure iPhone 18 Pro Max exists and is attached to android section
            String checkProductSql = "SELECT COUNT(*) FROM products WHERE product_slug = 'iphone-18-pro-max'";
            try (Statement stmt = dbService.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(checkProductSql)) {
                if (!rs.next() || rs.getInt(1) == 0) {
                    String insertProdSql = "INSERT INTO products "
                            + "(product_id, product_section_id, product_title, product_description, product_slug, "
                            + "product_price, product_stock, product_order, product_image) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement prep = dbService.getConnection().prepareStatement(insertProdSql)) {
                        prep.setString(1, iphoneId);
                        prep.setString(2, androidSectionId);
                        prep.setString(3, "iPhone 18 Pro Max");
                        prep.setString(4, "best iphone for you and your family");
                        prep.setString(5, "iphone-18-pro-max");
                        prep.setDouble(6, 125000.0);
                        prep.setInt(7, 100);
                        prep.setInt(8, 1);
                        prep.setString(9, "45b247e7-39bf-4308-9064-7351f48efe3c.webp");
                        prep.executeUpdate();
                    }
                } else {
                    try (Statement updateProd = dbService.getConnection().createStatement()) {
                        updateProd.executeUpdate("UPDATE products SET product_section_id = '" + androidSectionId + "', product_order = 1 WHERE product_id = '" + iphoneId + "'");
                    }
                }
            }

            // 3. Ensure the 3 versions exist
            String checkVersionsSql = "SELECT COUNT(*) FROM product_versions WHERE product_id = '" + iphoneId + "'";
            try (Statement stmt = dbService.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(checkVersionsSql)) {
                if (!rs.next() || rs.getInt(1) == 0) {
                    insertSampleVersion("f1e287b9-3902-49c7-921c-05df55825276", iphoneId, null, null, null, null, 1, 125000.0, 100);
                    insertSampleVersion("9fc54501-bebe-4045-9157-beae6451baa2", iphoneId, "Black", "Black", "black", "15741a25-730e-46c0-8019-26e36de1039c.webp", 2, 126000.0, 50);
                    insertSampleVersion("9328122e-abd6-4a0b-b339-404a905459e5", iphoneId, "Glacier Blue", "Glacier Blue", "glacier-blue", "e0c51951-19c0-46c1-b01f-c6a1b724790f.webp", 3, 130000.0, 10);
                } else {
                    try (Statement updateV1 = dbService.getConnection().createStatement()) {
                        updateV1.executeUpdate("UPDATE product_versions SET product_version_image = NULL WHERE product_version_id = 'f1e287b9-3902-49c7-921c-05df55825276'");
                    }
                }
            }

            // 4. Ensure sample deleted version exists for API tests (matches instructor's collection)
            String deletedId = "5c2052dc-cd4c-4ac4-8bdd-9719415afa96";
            String checkDelSql = "SELECT COUNT(*) FROM product_versions WHERE product_version_id = '" + deletedId + "'";
            try (Statement stmtDel = dbService.getConnection().createStatement();
                 ResultSet rsDel = stmtDel.executeQuery(checkDelSql)) {
                if (!rsDel.next() || rsDel.getInt(1) == 0) {
                    insertSampleVersion(deletedId, iphoneId, "Deleted Edition", "Deleted Edition", "deleted-edition", null, 99, 120000.0, 10);
                    try (Statement updateDel = dbService.getConnection().createStatement()) {
                        updateDel.executeUpdate("UPDATE product_versions SET product_version_delete_dt = CURRENT_TIMESTAMP WHERE product_version_id = '" + deletedId + "'");
                    }
                }
            }

            // 5. Ensure sample out-of-stock version exists for API tests
            String outOfStockId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
            String checkOosSql = "SELECT COUNT(*) FROM product_versions WHERE product_version_id = '" + outOfStockId + "'";
            try (Statement stmtOos = dbService.getConnection().createStatement();
                 ResultSet rsOos = stmtOos.executeQuery(checkOosSql)) {
                if (!rsOos.next() || rsOos.getInt(1) == 0) {
                    insertSampleVersion(outOfStockId, iphoneId, "Out of Stock Edition", "Out of Stock Edition", "out-of-stock", null, 100, 125000.0, 0);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "Could not seed sample versions: " + ex.getMessage());
        }
    }

    private void insertSampleVersion(String id, String productId, String titleAddon, String descAddon, String slugAddon, String image, int order, double price, int stock) {
        String sql = "INSERT INTO product_versions "
                + "(product_version_id, product_id, product_version_title_addon, product_version_title, "
                + "product_version_description_addon, product_version_description, "
                + "product_version_slug_addon, product_version_slug, "
                + "product_version_price, product_version_stock, product_version_order_in_price, product_version_order, product_version_image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, id);
            prep.setString(2, productId);
            prep.setString(3, titleAddon);
            prep.setString(4, titleAddon);
            prep.setString(5, descAddon);
            prep.setString(6, descAddon);
            prep.setString(7, slugAddon);
            prep.setString(8, slugAddon);
            prep.setDouble(9, price);
            prep.setInt(10, stock);
            prep.setInt(11, order);
            prep.setInt(12, order);
            prep.setString(13, image);
            prep.executeUpdate();
        } catch (Exception ex) {
            logger.log(Level.INFO, "Insert sample version failed: " + ex.getMessage());
        }
    }
}
