package step.learning.java231web.data.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.services.db.IDbService;

@Singleton
public class ProductSectionDao {
    private final IDbService dbService;
    private final Logger logger;
    private boolean isInstalled = false;

    @Inject
    public ProductSectionDao(IDbService dbService) {
        this.dbService = dbService;
        this.logger = Logger.getLogger(ProductSectionDao.class.getName());
    }

    public ProductSectionDao(IDbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger != null ? logger : Logger.getLogger(ProductSectionDao.class.getName());
    }

    public synchronized void ensureInstalled() {
        if (isInstalled || dbService == null) {
            return;
        }
        try {
            createTable();
            seedData();
            isInstalled = true;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Auto install or seed error", ex);
        }
    }

    public void createTable() throws SQLException {
        if (dbService == null) {
            return;
        }
        String sqlSections = "CREATE TABLE IF NOT EXISTS product_sections ("
                + "product_section_id CHAR(36) PRIMARY KEY, "
                + "product_section_parent_id CHAR(36) NULL, "
                + "product_section_title VARCHAR(128) NOT NULL, "
                + "product_section_description TEXT NULL, "
                + "product_section_slug VARCHAR(128) NOT NULL UNIQUE, "
                + "product_section_image VARCHAR(256) NULL, "
                + "product_section_order INT NOT NULL DEFAULT 10000, "
                + "product_section_delete_dt DATETIME NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        String sqlProducts = "CREATE TABLE IF NOT EXISTS products ("
                + "product_id CHAR(36) PRIMARY KEY, "
                + "product_section_id CHAR(36) NOT NULL, "
                + "product_title VARCHAR(256) NOT NULL, "
                + "product_description TEXT NULL, "
                + "product_slug VARCHAR(256) NOT NULL, "
                + "product_price DECIMAL(10, 2) NOT NULL DEFAULT 0, "
                + "product_stock INT NOT NULL DEFAULT 0, "
                + "product_order INT NOT NULL DEFAULT 10000, "
                + "product_image VARCHAR(256) NULL, "
                + "product_delete_dt DATETIME NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        String sqlProductVersions = "CREATE TABLE IF NOT EXISTS product_versions ("
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
            stmt.executeUpdate(sqlSections);
            stmt.executeUpdate(sqlProducts);
            stmt.executeUpdate(sqlProductVersions);
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error creating tables", ex);
            throw ex;
        }
    }

    public void seedData() throws SQLException {
        if (dbService == null) {
            return;
        }
        createTable();
        if (getSectionsCount() > 0) {
            return;
        }

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("sections.json")) {
            if (in != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ProductSection>>() {}.getType();
                    List<ProductSection> sections = gson.fromJson(reader, listType);
                    if (sections != null) {
                        for (ProductSection section : sections) {
                            addProductSection(section);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "Could not seed sections from JSON: " + ex.getMessage());
        }

        seedDefaultProductsIfEmpty();
    }

    private void seedDefaultProductsIfEmpty() {
        try {
            String checkSql = "SELECT COUNT(*) FROM products";
            try (Statement stmt = dbService.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }

            // Найдемо id секції 'tv' та додамо демонстраційні товари
            String tvSectionId = null;
            String tvSql = "SELECT product_section_id FROM product_sections WHERE product_section_slug = 'tv'";
            try (Statement stmt = dbService.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(tvSql)) {
                if (rs.next()) {
                    tvSectionId = rs.getString(1);
                }
            }

            if (tvSectionId != null) {
                String insertProductSql = "INSERT INTO products "
                        + "(product_id, product_section_id, product_title, product_description, product_slug, "
                        + "product_price, product_stock, product_order, product_image) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement prep = dbService.getConnection().prepareStatement(insertProductSql)) {
                    prep.setString(1, UUID.randomUUID().toString());
                    prep.setString(2, tvSectionId);
                    prep.setString(3, "Samsung Smart 4K UHD TV 55\"");
                    prep.setString(4, "Телевізор Samsung Crystal UHD 4K");
                    prep.setString(5, "samsung-smart-4k-55");
                    prep.setDouble(6, 18999.00);
                    prep.setInt(7, 10);
                    prep.setInt(8, 1);
                    prep.setString(9, "samsung_55.webp");
                    prep.executeUpdate();

                    prep.setString(1, UUID.randomUUID().toString());
                    prep.setString(2, tvSectionId);
                    prep.setString(3, "LG OLED 65\" Cinema Series");
                    prep.setString(4, "Телевізор LG OLED з підтримкою HDR10");
                    prep.setString(5, "lg-oled-65");
                    prep.setDouble(6, 45999.00);
                    prep.setInt(7, 5);
                    prep.setInt(8, 2);
                    prep.setString(9, "lg_oled_65.webp");
                    prep.executeUpdate();
                }
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "Could not seed sample products: " + ex.getMessage());
        }
    }

    public boolean addProductSection(ProductSection section) throws SQLException {
        if (dbService == null || section == null) {
            return false;
        }
        String sql = "INSERT INTO product_sections "
                + "(product_section_id, product_section_parent_id, product_section_title, "
                + "product_section_description, product_section_slug, product_section_image, product_section_order) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, section.getId() != null ? section.getId() : UUID.randomUUID().toString());
            prep.setString(2, section.getParentId());
            prep.setString(3, section.getTitle());
            prep.setString(4, section.getDescription());
            prep.setString(5, section.getSlug());
            prep.setString(6, section.getImageUrl());
            prep.setInt(7, section.getOrder());
            return prep.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, sql, ex);
            throw ex;
        }
    }

    public List<ProductSection> loadSectionsFromJson() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("sections.json")) {
            if (in != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ProductSection>>() {}.getType();
                    List<ProductSection> list = gson.fromJson(reader, listType);
                    return list != null ? list : new ArrayList<>();
                }
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "Could not load sections from JSON: " + ex.getMessage());
        }
        return new ArrayList<>();
    }

    public List<ProductSection> getAllSections() {
        List<ProductSection> list = new ArrayList<>();
        if (dbService != null) {
            try {
                String sql = "SELECT * FROM product_sections "
                        + "WHERE product_section_delete_dt IS NULL "
                        + "ORDER BY product_section_order";
                try (Statement stmt = dbService.getConnection().createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        ProductSection section = ProductSection.fromResultSet(rs, false);
                        if (section.getImageUrl() != null) {
                            section.setImageUrl("" + section.getImageUrl());
                        }
                        list.add(section);
                    }
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (Exception ex) {
                logger.log(Level.INFO, "DB getAllSections error: " + ex.getMessage());
            }
        }
        return loadSectionsFromJson();
    }

    public List<ProductSection> buildSectionTree(List<ProductSection> flatList) {
        if (flatList == null || flatList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, ProductSection> map = new LinkedHashMap<>();
        for (ProductSection s : flatList) {
            if (s.getChildren() == null) {
                s.setChildren(new ArrayList<>());
            }
            if (s.getProducts() == null) {
                s.setProducts(new ArrayList<>());
            }
            map.put(s.getId(), s);
        }

        List<ProductSection> roots = new ArrayList<>();
        for (ProductSection s : flatList) {
            String parentId = s.getParentId();
            if (parentId != null && map.containsKey(parentId) && !parentId.equals(s.getId())) {
                ProductSection parent = map.get(parentId);
                if (!parent.getChildren().contains(s)) {
                    parent.getChildren().add(s);
                }
            } else {
                roots.add(s);
            }
        }
        return roots;
    }

    public void populateChildren(ProductSection section) {
        if (section == null || section.getId() == null) {
            return;
        }
        List<ProductSection> all = getAllSections();
        for (ProductSection s : all) {
            if (section.getId().equals(s.getParentId()) && !section.getChildren().contains(s)) {
                section.getChildren().add(s);
            }
        }
    }

    public long getSectionsCount() throws SQLException {
        List<ProductSection> all = getAllSections();
        List<ProductSection> tree = buildSectionTree(all);
        return tree.size();
    }

    public List<ProductSection> getSections() throws SQLException {
        return getSections(new RestPagination(1, 100, 0));
    }

    public List<ProductSection> getSections(RestPagination pagination) throws SQLException {
        List<ProductSection> all = getAllSections();
        List<ProductSection> tree = buildSectionTree(all);

        int limit = pagination != null && pagination.getPageSize() > 0 ? pagination.getPageSize() : 10;
        int page = pagination != null && pagination.getPage() > 0 ? pagination.getPage() : 1;
        int offset = limit * (page - 1);

        int fromIndex = Math.min(offset, tree.size());
        int toIndex = Math.min(offset + limit, tree.size());
        return new ArrayList<>(tree.subList(fromIndex, toIndex));
    }

    public long getProductsCountBySectionSlug(String slug) throws SQLException {
        if (dbService == null) {
            return 0;
        }
        String sql = "SELECT COUNT(p.product_id) FROM products p "
                + "JOIN product_sections ps ON p.product_section_id = ps.product_section_id "
                + "WHERE ps.product_section_slug = ? AND p.product_delete_dt IS NULL";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, slug);
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, sql, ex);
            throw ex;
        }
        return 0;
    }

    public ProductSection getSectionBySlug(String slug, RestPagination pagination) throws SQLException {
        String sql = "SELECT ps.*, p.* FROM product_sections ps "
                + "LEFT JOIN products p ON ps.product_section_id = p.product_section_id "
                + "WHERE ps.product_section_slug = ? "
                + "ORDER BY p.product_order LIMIT ? OFFSET ?";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, slug);
            prep.setLong(2, pagination.getPageSize());
            prep.setLong(3, pagination.getPageSize() * (pagination.getPage() - 1));
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    ProductSection ps = ProductSection.fromResultSet(rs, true);
                    // коригуємо посилання на зображення, оскільки у БД
                    // знаходяться лише імена файлів
                    // http://localhost:8080/java231web/storage/{filename}
                    for (Product p : ps.getProducts()) {
                        p.setImageUrl( "" + p.getImageUrl() );
                    }
                    // Д.З. Впровадити коригування поля ImageUrl для самого розділу(section)
                    // В АРІ /section/{slug} аналогічно до корекцій, внесених до товарів
                    ps.setImageUrl( "" + ps.getImageUrl() );

                    populateChildren(ps);
                    return ps;
                }
            }
        }
        catch ( SQLException ex ) {
            logger.log(Level.WARNING, sql, ex);
            throw ex;
        }

        // Fallback to sections.json if not found in DB
        List<ProductSection> jsonSections = loadSectionsFromJson();
        for (ProductSection s : jsonSections) {
            if (slug.equalsIgnoreCase(s.getSlug())) {
                populateChildren(s);
                return s;
            }
        }

        return null;
    }
}
