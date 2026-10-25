package step.learning.java231web.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import step.learning.java231web.data.dao.CartDao;
import step.learning.java231web.data.dao.ProductVersionDao;
import step.learning.java231web.data.dao.TokenDao;
import step.learning.java231web.data.dto.Cart;
import step.learning.java231web.rest.RestResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CartServletTest {

    @Test
    public void testGetCartReturns401WhenUnauthorized() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);

        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(200);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName()) && args != null && args.length > 0) {
                        if ("Authorization".equalsIgnoreCase(args[0].toString())) {
                            return "Bearer " + TokenDao.DEMO_TOKEN_ID + "1";
                        }
                    }
                    if ("getWriter".equals(method.getName())) {
                        return printWriter;
                    }
                    return null;
                }
        );

        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                        return null;
                    }
                    if ("getWriter".equals(method.getName())) {
                        return printWriter;
                    }
                    return null;
                }
        );

        servlet.doGet(request, response);

        assertEquals(401, statusCode.get());

        String result = stringWriter.toString().trim();
        RestResponse restResponse = new Gson().fromJson(result, RestResponse.class);

        assertNotNull(restResponse);
        assertNotNull(restResponse.getStatus());
        assertFalse(restResponse.getStatus().isOk());
        assertEquals(401, restResponse.getStatus().getCode());
        assertEquals("Unauthorized", restResponse.getStatus().getMessage());
        assertNotNull(restResponse.getMeta());
        assertEquals("API 'User carts'", restResponse.getMeta().getServiceName());
    }

    @Test
    public void testGetCartReturnsActiveCartWhenAuthorized() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);

        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName()) && args != null && args.length > 0) {
                        if ("Authorization".equalsIgnoreCase(args[0].toString())) {
                            return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                        }
                    }
                    return null;
                }
        );

        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                        return null;
                    }
                    if ("getWriter".equals(method.getName())) {
                        return printWriter;
                    }
                    return null;
                }
        );

        servlet.doGet(request, response);

        assertEquals(200, statusCode.get());

        String result = stringWriter.toString().trim();
        Cart cart = gson.fromJson(result, Cart.class);

        assertNotNull(cart);
        assertNotNull(cart.getCartItems());
        assertFalse(cart.getCartItems().isEmpty());
        assertEquals(1, cart.getCartItems().get(0).getQuantity());
        assertNotNull(cart.getCartItems().get(0).getProductVersion());
        assertEquals("Burgundy", cart.getCartItems().get(0).getProductVersion().getTitleAddon());
    }

    @Test
    public void testPostAddProductNoIdReturns442() throws Exception {
        ProductVersionDao fakePvDao = new ProductVersionDao(null);
        CartDao fakeCartDao = new CartDao(null, fakePvDao);
        TokenDao fakeTokenDao = new TokenDao(null);
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, fakePvDao);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName()) && args != null && args.length > 0) {
                        if ("Authorization".equalsIgnoreCase(args[0].toString())) {
                            return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                        }
                    }
                    if ("getParameter".equals(method.getName())) {
                        return null; // no pvid!
                    }
                    if ("getReader".equals(method.getName())) {
                        return new java.io.BufferedReader(new java.io.StringReader(""));
                    }
                    return null;
                }
        );

        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                        return null;
                    }
                    if ("getWriter".equals(method.getName())) {
                        return printWriter;
                    }
                    return null;
                }
        );

        servlet.doPost(request, response);

        assertEquals(442, statusCode.get());
        String json = stringWriter.toString();
        RestResponse resp = new Gson().fromJson(json, RestResponse.class);
        assertNotNull(resp);
        assertEquals(442, resp.getStatus().getCode());
        assertEquals("'pvid' - product version id", resp.getData());
    }

    @Test
    public void testPostAddProductErrIdReturns404() throws Exception {
        ProductVersionDao fakePvDao = new ProductVersionDao(null);
        CartDao fakeCartDao = new CartDao(null, fakePvDao);
        TokenDao fakeTokenDao = new TokenDao(null);
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, fakePvDao);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName()) && args != null && args.length > 0) {
                        if ("Authorization".equalsIgnoreCase(args[0].toString())) {
                            return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                        }
                    }
                    if ("getParameter".equals(method.getName()) && "pvid".equals(args[0])) {
                        return "00000000-0000-0000-0000-000000000000";
                    }
                    return null;
                }
        );

        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                        return null;
                    }
                    if ("getWriter".equals(method.getName())) {
                        return printWriter;
                    }
                    return null;
                }
        );

        servlet.doPost(request, response);

        assertEquals(404, statusCode.get());
        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals(404, resp.getStatus().getCode());
    }

    @Test
    public void testPostAddProductDeletedReturns422() throws Exception {
        ProductVersionDao fakePvDao = new ProductVersionDao(null);
        CartDao fakeCartDao = new CartDao(null, fakePvDao);
        TokenDao fakeTokenDao = new TokenDao(null);
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, fakePvDao);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName()) && args != null && args.length > 0) {
                        if ("Authorization".equalsIgnoreCase(args[0].toString())) {
                            return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                        }
                    }
                    if ("getParameter".equals(method.getName()) && "pvid".equals(args[0])) {
                        return "5c2052dc-cd4c-4ac4-8bdd-9719415afa96"; // deleted
                    }
                    return null;
                }
        );

        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                        return null;
                    }
                    if ("getWriter".equals(method.getName())) {
                        return printWriter;
                    }
                    return null;
                }
        );

        servlet.doPost(request, response);

        assertEquals(422, statusCode.get());
        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals("Product version deleted", resp.getData());
    }

    @Test
    public void testPostAddProductOutOfStockReturns422() throws Exception {
        ProductVersionDao fakePvDao = new ProductVersionDao(null);
        CartDao fakeCartDao = new CartDao(null, fakePvDao);
        TokenDao fakeTokenDao = new TokenDao(null);
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, fakePvDao);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName()) && args != null && args.length > 0) {
                        if ("Authorization".equalsIgnoreCase(args[0].toString())) {
                            return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                        }
                    }
                    if ("getParameter".equals(method.getName()) && "pvid".equals(args[0])) {
                        return "3fa85f64-5717-4562-b3fc-2c963f66afa6"; // out of stock
                    }
                    return null;
                }
        );

        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                        return null;
                    }
                    if ("getWriter".equals(method.getName())) {
                        return printWriter;
                    }
                    return null;
                }
        );

        servlet.doPost(request, response);

        assertEquals(422, statusCode.get());
        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals("Product out of stock", resp.getData());
    }

    @Test
    public void testPostAddProductAlreadyReportedReturns208() throws Exception {
        ProductVersionDao fakePvDao = new ProductVersionDao(null);
        CartDao fakeCartDao = new CartDao(null, fakePvDao);
        TokenDao fakeTokenDao = new TokenDao(null);
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, fakePvDao);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        // Burgundy version is already in demo user's active cart
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName()) && args != null && args.length > 0) {
                        if ("Authorization".equalsIgnoreCase(args[0].toString())) {
                            return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                        }
                    }
                    if ("getParameter".equals(method.getName()) && "pvid".equals(args[0])) {
                        return "81fc3041-e5f0-41b7-9420-7b03f5a10316";
                    }
                    return null;
                }
        );

        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                        return null;
                    }
                    if ("getWriter".equals(method.getName())) {
                        return printWriter;
                    }
                    return null;
                }
        );

        servlet.doPost(request, response);

        assertEquals(208, statusCode.get());
        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals(208, resp.getStatus().getCode());
        assertEquals("Product is already in cart", resp.getData());
    }

    @Test
    public void testPatchCartReturns401WhenUnauthorized() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getMethod".equals(method.getName())) return "PATCH";
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );

        servlet.service(request, response);
        assertEquals(401, statusCode.get());
    }

    @Test
    public void testPatchCartReturns442WhenMissingCiid() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getMethod".equals(method.getName())) return "PATCH";
                    if ("getHeader".equals(method.getName()) && "Authorization".equalsIgnoreCase((String) args[0])) {
                        return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );

        servlet.service(request, response);
        assertEquals(442, statusCode.get());
    }

    @Test
    public void testPatchCartReturns422WhenMissingDelta() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getMethod".equals(method.getName())) return "PATCH";
                    if ("getHeader".equals(method.getName()) && "Authorization".equalsIgnoreCase((String) args[0])) {
                        return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                    }
                    if ("getParameter".equals(method.getName()) && "ciid".equals(args[0])) {
                        return "some-ciid";
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );

        servlet.service(request, response);
        assertEquals(422, statusCode.get());
    }

    @Test
    public void testPatchCartReturns404WhenCiidNotFoundInDb() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getMethod".equals(method.getName())) return "PATCH";
                    if ("getHeader".equals(method.getName()) && "Authorization".equalsIgnoreCase((String) args[0])) {
                        return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                    }
                    if ("getParameter".equals(method.getName()) && "ciid".equals(args[0])) {
                        return "non-existent-ciid";
                    }
                    if ("getParameter".equals(method.getName()) && "delta".equals(args[0])) {
                        return "1";
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );

        servlet.service(request, response);
        assertEquals(404, statusCode.get());
        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals(404, resp.getStatus().getCode());
    }

    @Test
    public void testPatchCartReturns403WhenCiidBelongsToAnotherEntity() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        // 77777777-7777-7777-7777-777777777777 is configured as belonging to another entity/user
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getMethod".equals(method.getName())) return "PATCH";
                    if ("getHeader".equals(method.getName()) && "Authorization".equalsIgnoreCase((String) args[0])) {
                        return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                    }
                    if ("getParameter".equals(method.getName()) && "ciid".equals(args[0])) {
                        return "77777777-7777-7777-7777-777777777777";
                    }
                    if ("getParameter".equals(method.getName()) && "delta".equals(args[0])) {
                        return "1";
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );

        servlet.service(request, response);
        assertEquals(403, statusCode.get());
        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals(403, resp.getStatus().getCode());
        assertEquals("Cart item belongs to another entity", resp.getData());
    }

    @Test
    public void testPatchCartSuccessUpdatesQuantityReturns200() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        // 656b0596-9c3d-46c0-90bb-e0ec0cc0025a is in demo user's active cart
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getMethod".equals(method.getName())) return "PATCH";
                    if ("getHeader".equals(method.getName()) && "Authorization".equalsIgnoreCase((String) args[0])) {
                        return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                    }
                    if ("getParameter".equals(method.getName()) && "ciid".equals(args[0])) {
                        return "656b0596-9c3d-46c0-90bb-e0ec0cc0025a";
                    }
                    if ("getParameter".equals(method.getName()) && "delta".equals(args[0])) {
                        return "1";
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length > 0) {
                        statusCode.set((Integer) args[0]);
                    }
                    if ("getWriter".equals(method.getName())) return printWriter;
                    return null;
                }
        );

        servlet.service(request, response);
        assertEquals(200, statusCode.get());
        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals(200, resp.getStatus().getCode());
    }
}
