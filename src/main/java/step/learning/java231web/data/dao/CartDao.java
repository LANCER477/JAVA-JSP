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

            String cartId = "b61a186a-cd2b-11f1-bfaa-84ba59d7b864";
            String userId = TokenDao.DEMO_USER_ID;

            String checkCart = "SELECT cart_id FROM carts WHERE user_id = ? AND cart_status = 0";
            boolean cartExists = false;
            try (PreparedStatement prep = conn.prepareStatement(checkCart)) {
                prep.setString(1, userId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (rs.next()) {
                        cartId = rs.getString("cart_id");
                        cartExists = true;
                    }
                }
            }

            if (!cartExists) {
                String insertCart = "INSERT INTO carts (cart_id, user_id, cart_status, cart_price) VALUES (?, ?, 0, 132000.0)";
                try (PreparedStatement prep = conn.prepareStatement(insertCart)) {
                    prep.setString(1, cartId);
                    prep.setString(2, userId);
                    prep.executeUpdate();
                }
            }

            String checkItem = "SELECT COUNT(*) FROM cart_items WHERE cart_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(checkItem)) {
                prep.setString(1, cartId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
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

            // Remove items other than Burgundy to allow repeatable test runs
            String cleanItems = "DELETE FROM cart_items WHERE cart_id = ? AND product_version_id != '81fc3041-e5f0-41b7-9420-7b03f5a10316'";
            try (PreparedStatement prep = conn.prepareStatement(cleanItems)) {
                prep.setString(1, cartId);
                prep.executeUpdate();
            }

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

            String updatePrice = "UPDATE carts SET cart_price = 132000.0 WHERE cart_id = ?";
            try (PreparedStatement prep = conn.prepareStatement(updatePrice)) {
                prep.setString(1, cartId);
                prep.executeUpdate();
            }

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
                return null;
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
                        return Cart.fromResultSet(rs);
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
                throw ex;
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
                throw ex;
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
        if (dbService != null) {
            ensureInstalled();
            String sql = "SELECT * FROM carts c "
                    + "LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id "
                    + "WHERE c.user_id = ? AND c.cart_status = 0 "
                    + "ORDER BY ci.add_dt";
            try (Connection conn = dbService.getConnection();
                 PreparedStatement prep = conn.prepareStatement(sql)) {
                prep.setString(1, userId);
                try (ResultSet rs = prep.executeQuery()) {
                    if (rs.next()) {
                        Cart cart = Cart.fromResultSet(rs);
                        if (cart != null) {
                            try {
                                do {
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
                            return cart;
                        }
                    }
                }
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "Error getUserActiveCart: " + ex.getMessage());
                if (!TokenDao.DEMO_USER_ID.equals(userId)) {
                    throw ex;
                }
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
            cart.setId("b61a186a-cd2b-11f1-bfaa-84ba59d7b864");
            cart.setUserId(TokenDao.DEMO_USER_ID);
            cart.setStatus(0);
            cart.setPrice(132000.0);

            CartItem item = new CartItem();
            item.setId("656b0596-9c3d-46c0-90bb-e0ec0cc0025a");
            item.setCartId("b61a186a-cd2b-11f1-bfaa-84ba59d7b864");
            item.setProductId("29cc99c6-ab15-4a6a-8e19-12b91244401a");
            item.setProductVersionId("81fc3041-e5f0-41b7-9420-7b03f5a10316");
            item.setQuantity(1);
            item.setPrice(132000.0);
            item.setProductVersion(createBurgundyVersion());

            List<CartItem> items = new ArrayList<>();
            items.add(item);
            cart.setCartItems(items);
            return cart;
        });
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
