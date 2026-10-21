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
import step.learning.java231web.data.dto.Cart;
import step.learning.java231web.data.dto.CartItem;
import step.learning.java231web.services.db.IDbService;

@Singleton
public class CartDao {
    private final IDbService dbService;
    private final Logger logger;
    private boolean isInstalled = false;

    @Inject
    public CartDao(IDbService dbService) {
        this.dbService = dbService;
        this.logger = Logger.getLogger(CartDao.class.getName());
    }

    public CartDao(IDbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger != null ? logger : Logger.getLogger(CartDao.class.getName());
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
                + "cart_status INT NOT NULL DEFAULT 0"
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
        String checkSql = "SELECT COUNT(*) FROM carts";
        try (Statement stmt = dbService.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        String cartId = UUID.randomUUID().toString();
        String insertCartSql = "INSERT INTO carts (cart_id, user_id, cart_status) VALUES (?, NULL, 0)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(insertCartSql)) {
            prep.setString(1, cartId);
            prep.executeUpdate();
        } catch (SQLException ex) {
            logger.log(Level.INFO, "Cart seed info: " + ex.getMessage());
            throw ex;
        }
    }

    public Cart getActiveCart(String userId) throws SQLException {
        if (dbService == null) {
            return null;
        }
        String sql = userId != null
                ? "SELECT * FROM carts WHERE user_id = ? AND cart_status = 0 ORDER BY cart_create_dt DESC LIMIT 1"
                : "SELECT * FROM carts WHERE user_id IS NULL AND cart_status = 0 ORDER BY cart_create_dt DESC LIMIT 1";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            if (userId != null) {
                prep.setString(1, userId);
            }
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    Cart cart = Cart.fromResultSet(rs);
                    if (cart != null) {
                        cart.setItems(getCartItems(cart.getId()));
                    }
                    return cart;
                }
            }
        }
        return null;
    }

    public List<CartItem> getCartItems(String cartId) throws SQLException {
        List<CartItem> list = new ArrayList<>();
        if (dbService == null || cartId == null) {
            return list;
        }
        String sql = "SELECT * FROM cart_items WHERE cart_id = ? ORDER BY add_dt";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, cartId);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    CartItem item = CartItem.fromResultSet(rs);
                    if (item != null) {
                        list.add(item);
                    }
                }
            }
        }
        return list;
    }

    public Cart getOrCreateActiveCart(String userId) throws SQLException {
        ensureInstalled();
        Cart cart = getActiveCart(userId);
        if (cart != null) {
            return cart;
        }
        if (dbService == null) {
            return null;
        }
        String cartId = UUID.randomUUID().toString();
        String sql = "INSERT INTO carts (cart_id, user_id, cart_status) VALUES (?, ?, 0)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, cartId);
            prep.setString(2, userId);
            prep.executeUpdate();
        }
        cart = new Cart();
        cart.setId(cartId);
        cart.setUserId(userId);
        cart.setStatus(0);
        cart.setItems(new ArrayList<>());
        return cart;
    }

    public CartItem addOrUpdateItem(String cartId, String productId, String productVersionId, int count, double price) throws SQLException {
        if (dbService == null || cartId == null) {
            return null;
        }
        ensureInstalled();
        String checkSql = productVersionId != null
                ? "SELECT cart_item_id, cnt FROM cart_items WHERE cart_id = ? AND product_version_id = ?"
                : "SELECT cart_item_id, cnt FROM cart_items WHERE cart_id = ? AND product_id = ? AND product_version_id IS NULL";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(checkSql)) {
            prep.setString(1, cartId);
            prep.setString(2, productVersionId != null ? productVersionId : productId);
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    String itemId = rs.getString("cart_item_id");
                    int currentCnt = rs.getInt("cnt");
                    int newCnt = currentCnt + count;
                    String updateSql = "UPDATE cart_items SET cnt = ?, price = ? WHERE cart_item_id = ?";
                    try (PreparedStatement updatePrep = dbService.getConnection().prepareStatement(updateSql)) {
                        updatePrep.setInt(1, newCnt);
                        updatePrep.setDouble(2, price);
                        updatePrep.setString(3, itemId);
                        updatePrep.executeUpdate();
                    }
                    CartItem item = new CartItem();
                    item.setId(itemId);
                    item.setCartId(cartId);
                    item.setProductId(productId);
                    item.setProductVersionId(productVersionId);
                    item.setCount(newCnt);
                    item.setPrice(price);
                    return item;
                }
            }
        }

        String itemId = UUID.randomUUID().toString();
        String insertSql = "INSERT INTO cart_items (cart_item_id, cart_id, product_id, product_version_id, cnt, price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(insertSql)) {
            prep.setString(1, itemId);
            prep.setString(2, cartId);
            prep.setString(3, productId);
            prep.setString(4, productVersionId);
            prep.setInt(5, count);
            prep.setDouble(6, price);
            prep.executeUpdate();
        }
        CartItem item = new CartItem();
        item.setId(itemId);
        item.setCartId(cartId);
        item.setProductId(productId);
        item.setProductVersionId(productVersionId);
        item.setCount(count);
        item.setPrice(price);
        return item;
    }
}