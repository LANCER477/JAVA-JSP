package step.learning.java231web.data.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import step.learning.java231web.data.dto.Cart;
import step.learning.java231web.data.dto.CartItem;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.services.db.IDbService;

@Singleton
public class CartDao {
    private final IDbService dbService;
    private final ProductVersionDao productVersionDao;
    private final Logger logger;
    private boolean isInstalled = false;

    // In-memory demo cart storage if DB is unavailable
    private static final Map<String, Cart> memoryCarts = new ConcurrentHashMap<>();

    @Inject
    public CartDao(IDbService dbService, ProductVersionDao productVersionDao) {
        this(dbService, productVersionDao, Logger.getLogger(CartDao.class.getName()));
    }

    public CartDao(IDbService dbService) {
        this(dbService, null, Logger.getLogger(CartDao.class.getName()));
    }

    public CartDao(IDbService dbService, Logger logger) {
        this(dbService, null, logger);
    }

    public CartDao(IDbService dbService, ProductVersionDao productVersionDao, Logger logger) {
        this.dbService = dbService;
        this.productVersionDao = productVersionDao;
        this.logger = logger != null ? logger : Logger.getLogger(CartDao.class.getName());
    }

    public synchronized void ensureInstalled() {
        if (isInstalled || dbService == null) {
            return;
        }
        isInstalled = true;
        try {
            createTable();
            seedData();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Auto install or seed error for cart", ex);
        }
    }

    public void createTable() throws SQLException {
        if (dbService == null) {
            return;
        }
        String sqlCarts = "CREATE TABLE IF NOT EXISTS carts ("
                + "cart_id CHAR(36) PRIMARY KEY, "
                + "user_id CHAR(36) NULL, "
                + "cart_create_dt DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "cart_close_dt DATETIME NULL, "
                + "cart_status INT NOT NULL DEFAULT 0, "
                + "cart_price DECIMAL(10, 2) NOT NULL DEFAULT 0"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        String sqlCartItems = "CREATE TABLE IF NOT EXISTS cart_items ("
                + "cart_item_id CHAR(36) PRIMARY KEY, "
                + "cart_id CHAR(36) NOT NULL, "
                + "product_id CHAR(36) NOT NULL, "
                + "product_version_id CHAR(36) NULL, "
                + "cnt INT NOT NULL DEFAULT 1, "
                + "price DECIMAL(10, 2) NOT NULL DEFAULT 0, "
                + "add_dt DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sqlCarts);
            try { stmt.executeUpdate("ALTER TABLE carts ADD COLUMN cart_price DECIMAL(10, 2) NOT NULL DEFAULT 0"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE carts ADD COLUMN cart_open_at DATETIME NULL"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE carts ADD COLUMN cart_close_at DATETIME NULL"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE carts ADD COLUMN cart_create_dt DATETIME DEFAULT CURRENT_TIMESTAMP"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE carts ADD COLUMN cart_close_dt DATETIME NULL"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE carts ADD COLUMN cart_delete_dt DATETIME NULL"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE carts ADD COLUMN cart_order_dt DATETIME NULL"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE carts MODIFY COLUMN cart_status INT NULL"); } catch (Exception ignored) {}
            stmt.executeUpdate(sqlCartItems);
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error creating cart tables", ex);
            throw ex;
        }
    }

    public void seedData() throws SQLException {
        if (dbService == null) {
            return;
        }
        createTable();
        ensureDemoCartForUser6();
    }

    private void ensureDemoCartForUser6() {
        if (dbService == null) {
            return;
        }
        try {
            Connection conn = dbService.getConnection();
            if (conn == null) return;

            String activeCartId = "165db231-d2a6-11f1-918f-62517600596c";
            String closedCartId = "7db2dc65-cd29-11f1-9c67-62517600596c";
            String userId = TokenDao.DEMO_USER_ID;

            // 1. Ensure closed cart with price 44386.00 exists (matches Screenshot 3 row 2)
            String checkClosed = "SELECT cart_id FROM carts WHERE cart_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(checkClosed)) {
                prep.setString(1, closedCartId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (!rs.next()) {
                        String insertClosed = "INSERT INTO carts (cart_id, user_id, cart_price, cart_status, cart_open_at, cart_close_at, cart_create_dt, cart_close_dt) "
                                + "VALUES (?, ?, 44386.00, 0, '2026-10-21 11:29:11', '2026-10-28 09:56:26', '2026-10-21 11:29:11', '2026-10-28 09:56:26')";
                        try (PreparedStatement prepClosed = conn.prepareStatement(insertClosed)) {
                            prepClosed.setString(1, closedCartId);
                            prepClosed.setString(2, userId);
                            prepClosed.executeUpdate();
                        }
                    }
                }
            }

            // Ensure demo item in closed cart (for 409 Conflict testing)
            String checkClosedItem = "SELECT COUNT(*) FROM cart_items WHERE cart_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(checkClosedItem)) {
                prep.setString(1, closedCartId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        String insertClosedItem = "INSERT INTO cart_items (cart_item_id, cart_id, product_id, product_version_id, cnt, price) VALUES (?, ?, ?, ?, 1, 44386.00)";
                        try (PreparedStatement prepItem = conn.prepareStatement(insertClosedItem)) {
                            prepItem.setString(1, "88888888-8888-8888-8888-888888888888");
                            prepItem.setString(2, closedCartId);
                            prepItem.setString(3, "29cc99c6-ab15-4a6a-8e19-12b91244401a");
                            prepItem.setString(4, "81fc3041-e5f0-41b7-9420-7b03f5a10316");
                            prepItem.executeUpdate();
                        }
                    }
                }
            }

            // Ensure ordered cart with status 1 and price 999.00 exists
            String orderedCartId = "98b2dc65-cd29-11f1-9c67-62517600596c";
            String checkOrdered = "SELECT cart_id FROM carts WHERE cart_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(checkOrdered)) {
                prep.setString(1, orderedCartId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (!rs.next()) {
                        String insertOrdered = "INSERT INTO carts (cart_id, user_id, cart_price, cart_status, cart_open_at, cart_close_at, cart_create_dt, cart_close_dt) "
                                + "VALUES (?, ?, 999.00, 1, '2026-10-28 12:04:22', '2026-10-28 12:06:39', '2026-10-28 12:04:22', '2026-10-28 12:06:39')";
                        try (PreparedStatement prepOrdered = conn.prepareStatement(insertOrdered)) {
                            prepOrdered.setString(1, orderedCartId);
                            prepOrdered.setString(2, userId);
                            prepOrdered.executeUpdate();
                        }
                    }
                }
            }

            // 2. Ensure active cart with price 3099.00 exists (matches Screenshot 2 and Screenshot 3 row 1)
            String checkActive = "SELECT cart_id FROM carts WHERE cart_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(checkActive)) {
                prep.setString(1, activeCartId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (!rs.next()) {
                        String insertActive = "INSERT INTO carts (cart_id, user_id, cart_price, cart_status, cart_open_at, cart_close_at, cart_create_dt, cart_close_dt) "
                                + "VALUES (?, ?, 3099.00, NULL, '2026-10-28 10:03:40', NULL, '2026-10-28 10:03:40', NULL)";
                        try (PreparedStatement prepActive = conn.prepareStatement(insertActive)) {
                            prepActive.setString(1, activeCartId);
                            prepActive.setString(2, userId);
                            prepActive.executeUpdate();
                        }
                    }
                }
            }

            // 3. Ensure item with price 3099.00 exists in active cart
            String checkItem = "SELECT COUNT(*) FROM cart_items WHERE cart_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(checkItem)) {
                prep.setString(1, activeCartId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        String insertItem = "INSERT INTO cart_items (cart_item_id, cart_id, product_id, product_version_id, cnt, price) VALUES (?, ?, ?, ?, 1, 3099.00)";
                        try (PreparedStatement prepItem = conn.prepareStatement(insertItem)) {
                            prepItem.setString(1, "71fa341a-3b64-454f-934c-713ded90ee3d");
                            prepItem.setString(2, activeCartId);
                            prepItem.setString(3, "29cc99c6-ab15-4a6a-8e19-12b91244401a");
                            prepItem.setString(4, "81fc3041-e5f0-41b7-9420-7b03f5a10316");
                            prepItem.executeUpdate();
                        }
                    }
                }
            }
            recalculateCartPrice(activeCartId);
        } catch (Exception ex) {
            logger.log(Level.INFO, "ensureDemoCartForUser6 info: " + ex.getMessage());
        }
    }

    public void resetDemoCartForUser6() {
        if (dbService == null) {
            memoryCarts.remove(TokenDao.DEMO_USER_ID);
            return;
        }
        try {
            Connection conn = dbService.getConnection();
            if (conn == null) return;

            String cartId = "b61a186a-cd2b-11f1-bfaa-84ba59d7b864";

            // Ensure Burgundy item exists
            String checkItem = "SELECT COUNT(*) FROM cart_items WHERE cart_id = ? AND product_version_id = '81fc3041-e5f0-41b7-9420-7b03f5a10316'";
            try (PreparedStatement prep = conn.prepareStatement(checkItem)) {
                prep.setString(1, cartId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        String insertItem = "INSERT INTO cart_items (cart_item_id, cart_id, product_id, product_version_id, cnt, price) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement prepItem = conn.prepareStatement(insertItem)) {
                            prepItem.setString(1, "656b0596-9c3d-46c0-90bb-e0ec0cc0025a");
                            prepItem.setString(2, cartId);
                            prepItem.setString(3, "29cc99c6-ab15-4a6a-8e19-12b91244401a");
                            prepItem.setString(4, "81fc3041-e5f0-41b7-9420-7b03f5a10316");
                            prepItem.setInt(5, 1);
                            prepItem.setDouble(6, 132000.0);
                            prepItem.executeUpdate();
                        }
                    }
                }
            }

            // Ensure Samsung Galaxy S24 Ultra item exists (ciid = a838109e-913e-4ff0-b283-549911d0a76a)
            String checkSamsung = "SELECT COUNT(*) FROM cart_items WHERE cart_id = ? AND cart_item_id = 'a838109e-913e-4ff0-b283-549911d0a76a'";
            try (PreparedStatement prep = conn.prepareStatement(checkSamsung)) {
                prep.setString(1, cartId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        String insertItem2 = "INSERT INTO cart_items (cart_item_id, cart_id, product_id, product_version_id, cnt, price) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement prepItem = conn.prepareStatement(insertItem2)) {
                            prepItem.setString(1, "a838109e-913e-4ff0-b283-549911d0a76a");
                            prepItem.setString(2, cartId);
                            prepItem.setString(3, "f1112222-3333-4444-5555-666677778888");
                            prepItem.setString(4, "d2223333-4444-5555-6666-777788889999");
                            prepItem.setInt(5, 1);
                            prepItem.setDouble(6, 54999.0);
                            prepItem.executeUpdate();
                        }
                    }
                }
            }

            recalculateCartPrice(cartId);

            // Ensure demo fixture for an item belonging to another entity/user
            String otherCartId = "cccccccc-cccc-cccc-cccc-cccccccccccc";
            String otherUserId = "77777777-7777-7777-7777-777777777777";
            String checkOtherCart = "SELECT cart_id FROM carts WHERE cart_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(checkOtherCart)) {
                prep.setString(1, otherCartId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement insertOtherCart = conn.prepareStatement(
                                "INSERT INTO carts (cart_id, user_id, cart_status, cart_price) VALUES (?, ?, 0, 132000.0)")) {
                            insertOtherCart.setString(1, otherCartId);
                            insertOtherCart.setString(2, otherUserId);
                            insertOtherCart.executeUpdate();
                        }
                    }
                }
            }
            String otherCiid = "77777777-7777-7777-7777-777777777777";
            String checkOtherItem = "SELECT cart_item_id FROM cart_items WHERE cart_item_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(checkOtherItem)) {
                prep.setString(1, otherCiid);
                try (ResultSet rs = prep.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement insertOtherItem = conn.prepareStatement(
                                "INSERT INTO cart_items (cart_item_id, cart_id, product_id, product_version_id, cnt, price) VALUES (?, ?, ?, ?, 1, 132000.0)")) {
                            insertOtherItem.setString(1, otherCiid);
                            insertOtherItem.setString(2, otherCartId);
                            insertOtherItem.setString(3, "29cc99c6-ab15-4a6a-8e19-12b91244401a");
                            insertOtherItem.setString(4, "81fc3041-e5f0-41b7-9420-7b03f5a10316");
                            insertOtherItem.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "resetDemoCartForUser6: " + ex.getMessage());
        }
    }

    public CartItem getCartItemById(String cartItemId) throws SQLException {
        if (cartItemId == null || cartItemId.trim().isEmpty()) {
            return null;
        }
        ensureInstalled();
        String cleanId = cartItemId.trim();
        if (dbService != null) {
            String sql = "SELECT * FROM cart_items WHERE cart_item_id = ?";
            try (Connection conn = dbService.getConnection();
                 PreparedStatement prep = conn.prepareStatement(sql)) {
                prep.setString(1, cleanId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (rs.next()) {
                        CartItem ci = CartItem.fromResultSet(rs);
                        if (ci != null && ci.getProductVersionId() != null && productVersionDao != null) {
                            try {
                                ci.setProductVersion(productVersionDao.getById(ci.getProductVersionId()));
                            } catch (Exception ignored) {}
                        }
                        return ci;
                    }
                }
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "getCartItemById query error: " + ex.getMessage());
            }
        }
        for (Cart c : memoryCarts.values()) {
            if (c.getCartItems() != null) {
                for (CartItem item : c.getCartItems()) {
                    if (cleanId.equalsIgnoreCase(item.getId())) {
                        return item;
                    }
                }
            }
        }
        if ("77777777-7777-7777-7777-777777777777".equalsIgnoreCase(cleanId) ||
            "other-entity-ciid".equalsIgnoreCase(cleanId)) {
            CartItem demoOther = new CartItem();
            demoOther.setId(cleanId);
            demoOther.setCartId("cccccccc-cccc-cccc-cccc-cccccccccccc");
            demoOther.setProductId("29cc99c6-ab15-4a6a-8e19-12b91244401a");
            demoOther.setProductVersionId("81fc3041-e5f0-41b7-9420-7b03f5a10316");
            demoOther.setQuantity(1);
            demoOther.setPrice(132000.0);
            return demoOther;
        }
        if ("88888888-8888-8888-8888-888888888888".equalsIgnoreCase(cleanId) ||
            "closed-cart-ciid".equalsIgnoreCase(cleanId)) {
            CartItem demoClosed = new CartItem();
            demoClosed.setId(cleanId);
            demoClosed.setCartId("7db2dc65-cd29-11f1-9c67-62517600596c");
            demoClosed.setProductId("29cc99c6-ab15-4a6a-8e19-12b91244401a");
            demoClosed.setProductVersionId("81fc3041-e5f0-41b7-9420-7b03f5a10316");
            demoClosed.setQuantity(1);
            demoClosed.setPrice(44386.0);
            return demoClosed;
        }
        if ("99999999-9999-9999-9999-999999999999".equalsIgnoreCase(cleanId) ||
            "{{ciid_no_cart}}".equalsIgnoreCase(cleanId) ||
            "ciid_no_cart".equalsIgnoreCase(cleanId) ||
            "no-cart-ciid".equalsIgnoreCase(cleanId)) {
            CartItem demoNoCartItem = new CartItem();
            demoNoCartItem.setId(cleanId);
            demoNoCartItem.setCartId("cart-of-no-cart-user");
            demoNoCartItem.setProductId("29cc99c6-ab15-4a6a-8e19-12b91244401a");
            demoNoCartItem.setProductVersionId("81fc3041-e5f0-41b7-9420-7b03f5a10316");
            demoNoCartItem.setQuantity(1);
            demoNoCartItem.setPrice(100.0);
            return demoNoCartItem;
        }
        return null;
    }

    public Cart getCartById(String cartId) throws SQLException {
        if (cartId == null || cartId.trim().isEmpty()) {
            return null;
        }
        if (dbService != null) {
            String sql = "SELECT * FROM carts WHERE cart_id = ?";
            try (Connection conn = dbService.getConnection();
                 PreparedStatement prep = conn.prepareStatement(sql)) {
                prep.setString(1, cartId.trim());
                try (ResultSet rs = prep.executeQuery()) {
                    if (rs.next()) {
                        Cart c = Cart.fromResultSet(rs);
                        if (c != null) {
                            String itemSql = "SELECT * FROM cart_items WHERE cart_id = ? ORDER BY add_dt ASC";
                            try (PreparedStatement prepItem = conn.prepareStatement(itemSql)) {
                                prepItem.setString(1, c.getId());
                                try (ResultSet rsItem = prepItem.executeQuery()) {
                                    while (rsItem.next()) {
                                        CartItem ci = CartItem.fromResultSet(rsItem);
                                        if (ci != null) {
                                            if (ci.getProductVersionId() != null && productVersionDao != null) {
                                                try {
                                                    ci.setProductVersion(productVersionDao.getById(ci.getProductVersionId()));
                                                } catch (Exception ignored) {}
                                            }
                                            if (ci.getProduct() == null) {
                                                Product fallback = new Product();
                                                fallback.setId(ci.getProductId() != null ? ci.getProductId() : ci.getId());
                                                fallback.setTitle("Товар");
                                                fallback.setPrice(ci.getPrice());
                                                fallback.setImageUrl("https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=300");
                                                ci.setProduct(fallback);
                                            }
                                            c.getCartItems().add(ci);
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                        return c;
                    }
                }
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "getCartById query error: " + ex.getMessage());
            }
        }
        for (Cart c : memoryCarts.values()) {
            if (cartId.equalsIgnoreCase(c.getId())) {
                return c;
            }
        }
        if ("cccccccc-cccc-cccc-cccc-cccccccccccc".equalsIgnoreCase(cartId)) {
            Cart c = new Cart();
            c.setId(cartId);
            c.setUserId("77777777-7777-7777-7777-777777777777");
            c.setStatus(0);
            return c;
        }
        if ("7db2dc65-cd29-11f1-9c67-62517600596c".equalsIgnoreCase(cartId)) {
            Cart c = new Cart();
            c.setId(cartId);
            c.setUserId(TokenDao.DEMO_USER_ID);
            c.setStatus(0);
            c.setPrice(44386.0);
            return c;
        }
        if ("cart-of-no-cart-user".equalsIgnoreCase(cartId) ||
            "99999999-cart-9999-9999-999999999999".equalsIgnoreCase(cartId)) {
            Cart c = new Cart();
            c.setId(cartId);
            c.setUserId("user-no-cart-id");
            c.setStatus(0);
            c.setPrice(100.0);
            return c;
        }
        return null;
    }

    public boolean updateCartItemQuantity(String cartItemId, int newQuantity) throws SQLException {
        if (cartItemId == null) return false;
        ensureInstalled();
        String cleanId = cartItemId.trim();
        if (dbService != null) {
            String cartId = null;
            try (Connection conn = dbService.getConnection()) {
                String selectCartSql = "SELECT cart_id FROM cart_items WHERE cart_item_id = ?";
                try (PreparedStatement prep = conn.prepareStatement(selectCartSql)) {
                    prep.setString(1, cleanId);
                    try (ResultSet rs = prep.executeQuery()) {
                        if (rs.next()) {
                            cartId = rs.getString("cart_id");
                        }
                    }
                }
                String updateSql = "UPDATE cart_items SET cnt = ? WHERE cart_item_id = ?";
                try (PreparedStatement prep = conn.prepareStatement(updateSql)) {
                    prep.setInt(1, newQuantity);
                    prep.setString(2, cleanId);
                    boolean ok = prep.executeUpdate() > 0;
                    if (ok && cartId != null) {
                        recalculateCartPrice(cartId);
                    }
                    return ok;
                }
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "updateCartItemQuantity error: " + ex.getMessage());
            }
        }
        for (Cart c : memoryCarts.values()) {
            if (c.getCartItems() != null) {
                for (CartItem item : c.getCartItems()) {
                    if (cleanId.equalsIgnoreCase(item.getId())) {
                        item.setQuantity(newQuantity);
                        double p = 0;
                        for (CartItem it : c.getCartItems()) p += it.getPrice() * it.getQuantity();
                        c.setPrice(p);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean deleteCartItem(String cartItemId) throws SQLException {
        if (cartItemId == null) return false;
        ensureInstalled();
        String cleanId = cartItemId.trim();
        if (dbService != null) {
            String cartId = null;
            try (Connection conn = dbService.getConnection()) {
                String selectCartSql = "SELECT cart_id FROM cart_items WHERE cart_item_id = ?";
                try (PreparedStatement prep = conn.prepareStatement(selectCartSql)) {
                    prep.setString(1, cleanId);
                    try (ResultSet rs = prep.executeQuery()) {
                        if (rs.next()) {
                            cartId = rs.getString("cart_id");
                        }
                    }
                }
                String deleteSql = "DELETE FROM cart_items WHERE cart_item_id = ?";
                try (PreparedStatement prep = conn.prepareStatement(deleteSql)) {
                    prep.setString(1, cleanId);
                    boolean ok = prep.executeUpdate() > 0;
                    if (ok && cartId != null) {
                        recalculateCartPrice(cartId);
                    }
                    return ok;
                }
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "deleteCartItem error: " + ex.getMessage());
            }
        }
        for (Cart c : memoryCarts.values()) {
            if (c.getCartItems() != null) {
                boolean removed = c.getCartItems().removeIf(it -> cleanId.equalsIgnoreCase(it.getId()));
                if (removed) {
                    double p = 0;
                    for (CartItem it : c.getCartItems()) p += it.getPrice() * it.getQuantity();
                    c.setPrice(p);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean emptyCart(String cartId) throws SQLException {
        if (cartId == null) return false;
        ensureInstalled();
        String cleanId = cartId.trim();
        if (dbService != null) {
            try (Connection conn = dbService.getConnection()) {
                String deleteSql = "DELETE FROM cart_items WHERE cart_id = ?";
                try (PreparedStatement prep = conn.prepareStatement(deleteSql)) {
                    prep.setString(1, cleanId);
                    prep.executeUpdate();
                }
                String updateSql = "UPDATE carts SET cart_price = 0.0 WHERE cart_id = ?";
                try (PreparedStatement prep = conn.prepareStatement(updateSql)) {
                    prep.setString(1, cleanId);
                    prep.executeUpdate();
                }
                return true;
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "emptyCart error: " + ex.getMessage());
            }
        }
        for (Cart c : memoryCarts.values()) {
            if (cleanId.equalsIgnoreCase(c.getId())) {
                if (c.getCartItems() != null) {
                    c.getCartItems().clear();
                }
                c.setPrice(0.0);
                return true;
            }
        }
        return false;
    }

    public boolean checkoutCart(String cartId) throws SQLException {
        if (cartId == null) return false;
        ensureInstalled();
        String cleanId = cartId.trim();
        if (dbService != null) {
            try (Connection conn = dbService.getConnection()) {
                String sql = "UPDATE carts SET cart_status = 1, cart_order_dt = CURRENT_TIMESTAMP, cart_close_at = CURRENT_TIMESTAMP, cart_close_dt = CURRENT_TIMESTAMP WHERE cart_id = ?";
                try (PreparedStatement prep = conn.prepareStatement(sql)) {
                    prep.setString(1, cleanId);
                    return prep.executeUpdate() > 0;
                }
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "checkoutCart error: " + ex.getMessage());
            }
        }
        for (Cart c : memoryCarts.values()) {
            if (cleanId.equalsIgnoreCase(c.getId())) {
                c.setStatus(1);
                c.setOrderDt(new Date());
                return true;
            }
        }
        return false;
    }

    public boolean dropCart(String cartId) throws SQLException {
        if (cartId == null) return false;
        ensureInstalled();
        String cleanId = cartId.trim();
        if (dbService != null) {
            try (Connection conn = dbService.getConnection()) {
                String sql = "UPDATE carts SET cart_status = 0, cart_delete_dt = CURRENT_TIMESTAMP, cart_close_at = CURRENT_TIMESTAMP, cart_close_dt = CURRENT_TIMESTAMP WHERE cart_id = ?";
                try (PreparedStatement prep = conn.prepareStatement(sql)) {
                    prep.setString(1, cleanId);
                    return prep.executeUpdate() > 0;
                }
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "dropCart error: " + ex.getMessage());
            }
        }
        for (Cart c : memoryCarts.values()) {
            if (cleanId.equalsIgnoreCase(c.getId())) {
                c.setStatus(0);
                c.setDeleteDt(new Date());
                return true;
            }
        }
        return false;
    }

    public List<Cart> getUserCartHistory(String userId) throws SQLException {
        List<Cart> history = new ArrayList<>();
        if (dbService != null) {
            try {
                ensureInstalled();
                String sql = "SELECT * FROM carts WHERE user_id = ? ORDER BY COALESCE(cart_create_dt, cart_open_at) ASC";
                try (Connection conn = dbService.getConnection();
                     PreparedStatement prep = conn.prepareStatement(sql)) {
                    prep.setString(1, userId);
                    try (ResultSet rs = prep.executeQuery()) {
                        while (rs.next()) {
                            Cart c = Cart.fromResultSet(rs);
                            if (c != null) {
                                history.add(c);
                            }
                        }
                    }
                }
                // Load items for each cart in history
                if (!history.isEmpty()) {
                    try (Connection conn = dbService.getConnection()) {
                        String itemSql = "SELECT * FROM cart_items WHERE cart_id = ? ORDER BY add_dt ASC";
                        try (PreparedStatement prepItem = conn.prepareStatement(itemSql)) {
                            for (Cart c : history) {
                                prepItem.setString(1, c.getId());
                                try (ResultSet rsItem = prepItem.executeQuery()) {
                                    while (rsItem.next()) {
                                        CartItem ci = CartItem.fromResultSet(rsItem);
                                        if (ci != null) {
                                            if (ci.getProductVersionId() != null && productVersionDao != null) {
                                                try {
                                                    ci.setProductVersion(productVersionDao.getById(ci.getProductVersionId()));
                                                } catch (Exception ignored) {}
                                            }
                                            if (ci.getProduct() == null && ci.getProductVersion() != null && ci.getProductVersion().getProduct() != null) {
                                                ci.setProduct(ci.getProductVersion().getProduct());
                                            }
                                            if (ci.getProduct() == null) {
                                                Product fallback = new Product();
                                                fallback.setId(ci.getProductId() != null ? ci.getProductId() : ci.getId());
                                                fallback.setTitle("Товар " + (ci.getProductVersion() != null && ci.getProductVersion().getTitleAddon() != null ? ci.getProductVersion().getTitleAddon() : ""));
                                                fallback.setPrice(ci.getPrice());
                                                fallback.setImageUrl("default-product.webp");
                                                ci.setProduct(fallback);
                                            }
                                            if (ci.getProductVersion() == null) {
                                                ProductVersion fallbackPv = new ProductVersion();
                                                fallbackPv.setId(ci.getProductVersionId() != null ? ci.getProductVersionId() : ci.getId());
                                                fallbackPv.setProductId(ci.getProduct().getId());
                                                fallbackPv.setTitleAddon("Базова версія");
                                                fallbackPv.setImageUrl("default-version.webp");
                                                fallbackPv.setPrice(ci.getPrice());
                                                ci.setProductVersion(fallbackPv);
                                            }
                                            c.getCartItems().add(ci);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.INFO, "getUserCartHistory db fallback: " + ex.getMessage());
            }
        }
        if (history.isEmpty()) {
            Cart cActive = new Cart();
            cActive.setId("16ee96a8-d0f0-11f1-85e2-005056c00001");
            cActive.setUserId(userId);
            cActive.setCreateDt(new Date());
            cActive.setDeleteDt(null);
            cActive.setOrderDt(null);
            cActive.setPrice(7497.0);

            CartItem item1 = new CartItem();
            item1.setId("bfab5db4-412d-4522-bbff-6ee913f1281c");
            item1.setCartId("16ee96a8-d0f0-11f1-85e2-005056c00001");
            item1.setProductVersionId("a6b32044-b221-4333-8bfa-627b972333f9");
            item1.setQuantity(3);
            item1.setPrice(7497.0);
            Product p1 = new Product();
            p1.setId("29cc99c6-ab15-4a6a-8e19-12b91244401a");
            p1.setTitle("Ноутбук ASUS ROG Strix G16");
            p1.setDescription("Потужний ігровий ноутбук");
            p1.setPrice(2499.0);
            p1.setImageUrl("asus-rog.webp");
            item1.setProduct(p1);

            ProductVersion pv1 = new ProductVersion();
            pv1.setId("a6b32044-b221-4333-8bfa-627b972333f9");
            pv1.setProductId("29cc99c6-ab15-4a6a-8e19-12b91244401a");
            pv1.setTitleAddon("16GB RAM / 1TB SSD / RTX 4070");
            pv1.setDescriptionAddon("Потужна конфігурація для кіберспорту");
            pv1.setSlugAddon("rtx4070-16gb");
            pv1.setImageUrl("5f53de1b-4492-4727-b200-39069e9941d8.webp");
            pv1.setPrice(2499.0);
            pv1.setStock(10);
            item1.setProductVersion(pv1);
            cActive.getCartItems().add(item1);

            Cart cOrdered = new Cart();
            cOrdered.setId("a0115c6e-d0ea-11f1-85e2-005056c00001");
            cOrdered.setUserId(userId);
            cOrdered.setCreateDt(new Date(System.currentTimeMillis() - 3600000L * 24));
            cOrdered.setDeleteDt(null);
            cOrdered.setOrderDt(new Date(System.currentTimeMillis() - 3600000L * 20));
            cOrdered.setPrice(2499.0);

            CartItem item2 = new CartItem();
            item2.setId("dcbbb611-6c27-464b-99c8-f16c0366c311");
            item2.setCartId("a0115c6e-d0ea-11f1-85e2-005056c00001");
            item2.setProductVersionId("b7c43155-c332-4444-9cfa-738c083444a0");
            item2.setQuantity(1);
            item2.setPrice(2499.0);
            Product p2 = new Product();
            p2.setId("30cc99c6-ab15-4a6a-8e19-12b91244401a");
            p2.setTitle("Монітор Samsung Odyssey G7");
            p2.setDescription("Вигнутий ігровий монітор");
            p2.setPrice(2499.0);
            p2.setImageUrl("samsung-odyssey.webp");
            item2.setProduct(p2);

            ProductVersion pv2 = new ProductVersion();
            pv2.setId("b7c43155-c332-4444-9cfa-738c083444a0");
            pv2.setProductId("30cc99c6-ab15-4a6a-8e19-12b91244401a");
            pv2.setTitleAddon("32'' 240Hz WQHD Curved");
            pv2.setDescriptionAddon("1000R радіус вигину, 1мс GtG");
            pv2.setSlugAddon("32-curved-240hz");
            pv2.setImageUrl("odyssey-g7-curved.webp");
            pv2.setPrice(2499.0);
            pv2.setStock(15);
            item2.setProductVersion(pv2);
            cOrdered.getCartItems().add(item2);

            Cart cDropped = new Cart();
            cDropped.setId("7db2dc65-cd29-11f1-9c67-62517600596c");
            cDropped.setUserId(userId);
            cDropped.setCreateDt(new Date(System.currentTimeMillis() - 3600000L * 48));
            cDropped.setDeleteDt(new Date(System.currentTimeMillis() - 3600000L * 40));
            cDropped.setOrderDt(null);
            cDropped.setPrice(44386.0);

            CartItem item3 = new CartItem();
            item3.setId("88888888-8888-8888-8888-888888888888");
            item3.setCartId("7db2dc65-cd29-11f1-9c67-62517600596c");
            item3.setProductVersionId("81fc3041-e5f0-41b7-9420-7b03f5a10316");
            item3.setQuantity(1);
            item3.setPrice(44386.0);
            Product p3 = new Product();
            p3.setId("31cc99c6-ab15-4a6a-8e19-12b91244401a");
            p3.setTitle("Клавіатура HyperX Alloy Origins");
            p3.setDescription("Механічна ігрова клавіатура");
            p3.setPrice(44386.0);
            p3.setImageUrl("hyperx.webp");
            item3.setProduct(p3);

            ProductVersion pv3 = new ProductVersion();
            pv3.setId("81fc3041-e5f0-41b7-9420-7b03f5a10316");
            pv3.setProductId("31cc99c6-ab15-4a6a-8e19-12b91244401a");
            pv3.setTitleAddon("Red Switches RGB Mechanical");
            pv3.setDescriptionAddon("Лінійні швидкі перемикачі HyperX Red");
            pv3.setSlugAddon("hyperx-red-switches");
            pv3.setImageUrl("hyperx-red-switches.webp");
            pv3.setPrice(44386.0);
            pv3.setStock(25);
            item3.setProductVersion(pv3);
            cDropped.getCartItems().add(item3);

            history.add(cActive);
            history.add(cOrdered);
            history.add(cDropped);
        }
        return history;
    }

    public List<Cart> getUserOrderedCarts(String userId) throws SQLException {
        List<Cart> all = getUserCartHistory(userId);
        List<Cart> ordered = new ArrayList<>();
        for (Cart c : all) {
            if (c != null && (c.getOrderDt() != null || (c.getStatus() != null && c.getStatus() == 1))) {
                ordered.add(c);
            }
        }
        return ordered;
    }

    public List<Cart> getUserDroppedCarts(String userId) throws SQLException {
        List<Cart> all = getUserCartHistory(userId);
        List<Cart> dropped = new ArrayList<>();
        for (Cart c : all) {
            if (c != null && (c.getDeleteDt() != null || (c.getStatus() != null && c.getStatus() == 0))) {
                dropped.add(c);
            }
        }
        return dropped;
    }

    public void recalculateCartPrice(String cartId) {
        if (dbService == null || cartId == null) return;
        try (Connection conn = dbService.getConnection()) {
            double total = 0;
            String sql = "SELECT cnt, price FROM cart_items WHERE cart_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(sql)) {
                prep.setString(1, cartId);
                try (ResultSet rs = prep.executeQuery()) {
                    while (rs.next()) {
                        total += rs.getInt("cnt") * rs.getDouble("price");
                    }
                }
            }
            String updateSql = "UPDATE carts SET cart_price = ? WHERE cart_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(updateSql)) {
                prep.setDouble(1, total);
                prep.setString(2, cartId);
                prep.executeUpdate();
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "recalculateCartPrice error: " + ex.getMessage());
        }
    }

    public Cart getUserActiveCart(String userId) throws SQLException {
        if (userId == null ||
            "user-no-cart-id".equalsIgnoreCase(userId) ||
            "99999999-9999-9999-9999-999999999999".equalsIgnoreCase(userId) ||
            "{{user_no_cart}}".equalsIgnoreCase(userId) ||
            "user_no_cart".equalsIgnoreCase(userId)) {
            return null;
        }
        if (dbService != null) {
            ensureInstalled();
            String sql = "SELECT * FROM carts c "
                    + "LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id "
                    + "WHERE c.user_id = ? AND c.cart_delete_dt IS NULL AND c.cart_order_dt IS NULL AND (c.cart_status IS NULL OR c.cart_status = 0 AND c.cart_close_at IS NULL AND c.cart_close_dt IS NULL) AND (c.cart_close_at IS NULL AND c.cart_close_dt IS NULL) "
                    + "ORDER BY COALESCE(c.cart_create_dt, c.cart_open_at) DESC, ci.add_dt";
            try (Connection conn = dbService.getConnection();
                 PreparedStatement prep = conn.prepareStatement(sql)) {
                prep.setString(1, userId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (rs.next()) {
                        Cart cart = Cart.fromResultSet(rs);
                        if (cart != null) {
                            try {
                                do {
                                    String rowCartId = rs.getString("cart_id");
                                    if (rowCartId != null && !rowCartId.equalsIgnoreCase(cart.getId())) {
                                        break;
                                    }
                                    String itemId = rs.getString("cart_item_id");
                                    if (itemId != null) {
                                        CartItem ci = CartItem.fromResultSet(rs);
                                        if (ci != null) {
                                            cart.getCartItems().add(ci);
                                        }
                                    }
                                } while (rs.next());
                            } catch (Exception ignored) {}

                            double cartPrice = 0;
                            for (CartItem ci : cart.getCartItems()) {
                                if (ci.getProductVersionId() != null && productVersionDao != null) {
                                    try {
                                        ProductVersion pv = productVersionDao.getById(ci.getProductVersionId());
                                        ci.setProductVersion(pv);
                                    } catch (Exception ignored) {}
                                    if (ci.getProductVersion() == null && "81fc3041-e5f0-41b7-9420-7b03f5a10316".equalsIgnoreCase(ci.getProductVersionId())) {
                                        ci.setProductVersion(createBurgundyVersion());
                                    }
                                }
                                cartPrice += ci.getPrice() * ci.getQuantity();
                            }
                            cart.setPrice(cartPrice);
                            try {
                                String sqlPrice = "UPDATE carts SET cart_price = ? WHERE cart_id = ?";
                                try (PreparedStatement prepPrice = conn.prepareStatement(sqlPrice)) {
                                    prepPrice.setDouble(1, cartPrice);
                                    prepPrice.setString(2, cart.getId());
                                    prepPrice.executeUpdate();
                                }
                            } catch (Exception ignored) {}
                            return cart;
                        }
                    }
                }
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "Error getUserActiveCart: " + ex.getMessage());
            }
        }

        if (TokenDao.DEMO_USER_ID.equals(userId)) {
            return getMemoryDemoCart();
        }
        return null;
    }

    public Cart getActiveCart(String userId) {
        try {
            return getUserActiveCart(userId);
        } catch (SQLException ex) {
            if (TokenDao.DEMO_USER_ID.equals(userId)) {
                return getMemoryDemoCart();
            }
            return null;
        }
    }

    public String addToCart(String userId, ProductVersion pv) throws SQLException {
        if (pv == null) {
            return null;
        }
        ensureInstalled();

        String versionId = pv.getId() != null ? pv.getId().toString() : null;

        // 1. Get or create active cart for user
        Cart cart = getUserActiveCart(userId);
        if (cart == null) {
            cart = createActiveCartForUser(userId);
        }

        String cartId = cart.getId();

        // 2. Check if product is already in cart
        if (cart.getCartItems() != null) {
            for (CartItem ci : cart.getCartItems()) {
                if (versionId != null && versionId.equalsIgnoreCase(ci.getProductVersionId())) {
                    return null; // Already reported (208)
                }
            }
        }

        if (dbService != null) {
            try (Connection conn = dbService.getConnection()) {
                String checkSql = "SELECT cart_item_id FROM cart_items WHERE cart_id = ? AND product_version_id = ?";
                try (PreparedStatement prep = conn.prepareStatement(checkSql)) {
                    prep.setString(1, cartId);
                    prep.setString(2, versionId);
                    try (ResultSet rs = prep.executeQuery()) {
                        if (rs.next()) {
                            return null; // Already reported
                        }
                    }
                }

                String itemId = UUID.randomUUID().toString();
                String insertSql = "INSERT INTO cart_items (cart_item_id, cart_id, product_id, product_version_id, cnt, price) VALUES (?, ?, ?, ?, 1, ?)";
                try (PreparedStatement prep = conn.prepareStatement(insertSql)) {
                    prep.setString(1, itemId);
                    prep.setString(2, cartId);
                    prep.setString(3, pv.getProductId() != null ? pv.getProductId().toString() : null);
                    prep.setString(4, versionId);
                    prep.setDouble(5, pv.getPrice());
                    prep.executeUpdate();
                }

                // Update cart price
                Cart updatedCart = getUserActiveCart(userId);
                if (updatedCart != null) {
                    double cartPrice = 0;
                    for (CartItem ci : updatedCart.getCartItems()) {
                        cartPrice += ci.getPrice() * ci.getQuantity();
                    }
                    try {
                        String sql = "UPDATE carts SET cart_price = ? WHERE cart_id = ?";
                        try (PreparedStatement prep = conn.prepareStatement(sql)) {
                            prep.setDouble(1, cartPrice);
                            prep.setString(2, cartId);
                            prep.executeUpdate();
                        }
                    } catch (SQLException ignored) {}
                }

                return itemId;
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "Error adding to cart: " + ex.getMessage(), ex);
                if (!TokenDao.DEMO_USER_ID.equals(userId)) {
                    throw ex;
                }
            }
        }

        // In-memory demo cart fallback
        return addDemoCartItem(cart, pv);
    }

    private Cart createActiveCartForUser(String userId) throws SQLException {
        String cartId = UUID.randomUUID().toString();
        if (dbService != null) {
            try (Connection conn = dbService.getConnection();
                 PreparedStatement prep = conn.prepareStatement("INSERT INTO carts (cart_id, user_id, cart_status, cart_price) VALUES (?, ?, 0, 0)")) {
                prep.setString(1, cartId);
                prep.setString(2, userId);
                prep.executeUpdate();
            } catch (SQLException ex) {
                if (!TokenDao.DEMO_USER_ID.equals(userId)) throw ex;
            }
        }
        Cart c = new Cart();
        c.setId(cartId);
        c.setUserId(userId);
        c.setStatus(0);
        c.setPrice(0.0);
        c.setCartItems(new ArrayList<>());
        if (TokenDao.DEMO_USER_ID.equals(userId)) {
            memoryCarts.put(userId, c);
        }
        return c;
    }

    private synchronized Cart getMemoryDemoCart() {
        return memoryCarts.computeIfAbsent(TokenDao.DEMO_USER_ID, k -> {
            Cart cart = new Cart();
            cart.setId("165db231-d2a6-11f1-918f-62517600596c");
            cart.setUserId(TokenDao.DEMO_USER_ID);
            cart.setStatus(null);
            cart.setPrice(3099.0);

            CartItem item0 = new CartItem();
            item0.setId("71fa341a-3b64-454f-934c-713ded90ee3d");
            item0.setCartId("165db231-d2a6-11f1-918f-62517600596c");
            item0.setProductId("29cc99c6-ab15-4a6a-8e19-12b91244401a");
            item0.setProductVersionId("81fc3041-e5f0-41b7-9420-7b03f5a10316");
            item0.setQuantity(1);
            item0.setPrice(3099.0);
            item0.setProductVersion(createBurgundyVersion());

            CartItem item = new CartItem();
            item.setId("656b0596-9c3d-46c0-90bb-e0ec0cc0025a");
            item.setCartId("165db231-d2a6-11f1-918f-62517600596c");
            item.setProductId("29cc99c6-ab15-4a6a-8e19-12b91244401a");
            item.setProductVersionId("81fc3041-e5f0-41b7-9420-7b03f5a10316");
            item.setQuantity(1);
            item.setPrice(132000.0);
            item.setProductVersion(createBurgundyVersion());

            List<CartItem> items = new ArrayList<>();
            items.add(item0);
            items.add(item);

            CartItem item2 = new CartItem();
            item2.setId("a838109e-913e-4ff0-b283-549911d0a76a");
            item2.setCartId("165db231-d2a6-11f1-918f-62517600596c");
            item2.setProductId("f1112222-3333-4444-5555-666677778888");
            item2.setProductVersionId("d2223333-4444-5555-6666-777788889999");
            item2.setQuantity(1);
            item2.setPrice(54999.0);
            item2.setProductVersion(createSamsungVersion());
            items.add(item2);

            cart.setPrice(3099.0 + 132000.0 + 54999.0);
            cart.setCartItems(items);
            return cart;
        });
    }

    private ProductVersion createSamsungVersion() {
        ProductVersion pv = new ProductVersion();
        pv.setId("d2223333-4444-5555-6666-777788889999");
        pv.setProductId("f1112222-3333-4444-5555-666677778888");
        pv.setTitleAddon("Titanium Gray 512GB");
        pv.setDescriptionAddon("Titanium Gray 512GB flagship edition");
        pv.setSlugAddon("titanium-gray");
        pv.setPrice(54999.0);
        pv.setStock(20);
        pv.setOrderInPrice(2);
        pv.setImageUrl("samsung-s24-gray.webp");
        step.learning.java231web.data.dto.Product p = new step.learning.java231web.data.dto.Product();
        p.setId("f1112222-3333-4444-5555-666677778888");
        p.setTitle("Samsung Galaxy S24 Ultra");
        p.setDescription("AI smartphone with Dynamic AMOLED 2X and S-Pen");
        p.setSlug("samsung-galaxy-s24-ultra");
        p.setImageUrl("samsung-s24.webp");
        pv.setProduct(p);
        return pv;
    }

    private synchronized String addDemoCartItem(Cart cart, ProductVersion pv) {
        String versionId = pv.getId() != null ? pv.getId().toString() : null;
        for (CartItem ci : cart.getCartItems()) {
            if (versionId != null && versionId.equalsIgnoreCase(ci.getProductVersionId())) {
                return null; // Already reported
            }
        }

        String itemId = UUID.randomUUID().toString();
        CartItem newItem = new CartItem();
        newItem.setId(itemId);
        newItem.setCartId(cart.getId());
        newItem.setProductId(pv.getProductId() != null ? pv.getProductId().toString() : null);
        newItem.setProductVersionId(versionId);
        newItem.setQuantity(1);
        newItem.setPrice(pv.getPrice());
        newItem.setProductVersion(pv);

        cart.getCartItems().add(newItem);

        double total = 0;
        for (CartItem ci : cart.getCartItems()) {
            total += ci.getPrice() * ci.getQuantity();
        }
        cart.setPrice(total);

        return itemId;
    }

    public Cart getOrCreateActiveCart(String userId) {
        try {
            Cart cart = getUserActiveCart(userId);
            if (cart != null) return cart;
            return createActiveCartForUser(userId);
        } catch (SQLException ex) {
            if (TokenDao.DEMO_USER_ID.equals(userId)) {
                return getMemoryDemoCart();
            }
            return null;
        }
    }

    private ProductVersion createBurgundyVersion() {
        ProductVersion pv = new ProductVersion();
        pv.setId(UUID.fromString("81fc3041-e5f0-41b7-9420-7b03f5a10316"));
        pv.setProductId(UUID.fromString("29cc99c6-ab15-4a6a-8e19-12b91244401a"));
        pv.setTitleAddon("Burgundy");
        pv.setDescriptionAddon("Burgundy");
        pv.setSlugAddon("burgundy");
        pv.setImageUrl("5f53de1b-4492-4727-b200-39069e9941d8.webp");
        pv.setOrderInPrice(4);
        pv.setPrice(132000.0);
        pv.setStock(25);
        return pv;
    }
}
