package step.learning.java231web.dao;

import org.junit.jupiter.api.Test;
import step.learning.java231web.data.dao.CartDao;
import step.learning.java231web.data.dto.Cart;
import step.learning.java231web.data.dto.CartItem;
import step.learning.java231web.services.db.IDbService;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class CartDaoTest {

    @Test
    public void testCreateTableExecutesSql() throws Exception {
        AtomicBoolean sqlExecuted = new AtomicBoolean(false);

        Statement mockStmt = (Statement) Proxy.newProxyInstance(
                Statement.class.getClassLoader(),
                new Class<?>[]{Statement.class},
                (proxy, method, args) -> {
                    if ("executeUpdate".equals(method.getName())) {
                        sqlExecuted.set(true);
                        return 1;
                    }
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );

        Connection mockConn = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("createStatement".equals(method.getName())) {
                        return mockStmt;
                    }
                    return null;
                }
        );

        IDbService mockDbService = () -> mockConn;
        CartDao cartDao = new CartDao(mockDbService);

        assertDoesNotThrow(() -> cartDao.createTable());
        assertTrue(sqlExecuted.get());
    }

    @Test
    public void testCartDtoSettersAndGetters() {
        Cart cart = new Cart();
        cart.setId("cart-123");
        cart.setUserId("user-456");
        cart.setStatus(0);

        assertEquals("cart-123", cart.getId());
        assertEquals("user-456", cart.getUserId());
        assertEquals(0, cart.getStatus());
        assertNotNull(cart.getItems());

        CartItem item = new CartItem();
        item.setId("item-1");
        item.setCartId("cart-123");
        item.setProductId("prod-1");
        item.setCount(2);
        item.setPrice(150.0);

        assertEquals("item-1", item.getId());
        assertEquals("cart-123", item.getCartId());
        assertEquals("prod-1", item.getProductId());
        assertEquals(2, item.getCount());
        assertEquals(150.0, item.getPrice());
    }
}