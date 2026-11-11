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
        Cart cart = null;
        try {
            RestResponse restResponse = gson.fromJson(result, RestResponse.class);
            if (restResponse != null && restResponse.getData() != null) {
                cart = gson.fromJson(gson.toJson(restResponse.getData()), Cart.class);
            }
        } catch (Exception ignored) {}
        if (cart == null) {
            cart = gson.fromJson(result, Cart.class);
        }

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

    @Test
    public void testPatchCartExceededStockLimitReturns200WithStockMessage() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        // Burgundy item stock is 25. Quantity is 1. inc=30 exceeds stock limit.
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
                    if ("getParameter".equals(method.getName()) && "inc".equals(args[0])) {
                        return "30";
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
        assertEquals("'inc' exceeded stock limit", resp.getData());
        assertEquals("string", resp.getMeta().getDataType());
    }

    @Test
    public void testPatchCartCausesZeroQuantityReturnsMessage() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        Cart active = fakeCartDao.getUserActiveCart(TokenDao.DEMO_USER_ID);
        int currentQty = active != null && !active.getCartItems().isEmpty() ? active.getCartItems().get(0).getQuantity() : 1;

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
                        return "656b0596-9c3d-46c0-90bb-e0ec0cc0025a";
                    }
                    if ("getParameter".equals(method.getName()) && "inc".equals(args[0])) {
                        return String.valueOf(-currentQty);
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
        assertEquals("'inc' causes zero quantity. For deletion use DELETE request", resp.getData());
    }

    @Test
    public void testPatchCartCausesNegativeQuantityReturnsMessage() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        Cart active = fakeCartDao.getUserActiveCart(TokenDao.DEMO_USER_ID);
        int currentQty = active != null && !active.getCartItems().isEmpty() ? active.getCartItems().get(0).getQuantity() : 1;

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
                        return "656b0596-9c3d-46c0-90bb-e0ec0cc0025a";
                    }
                    if ("getParameter".equals(method.getName()) && "inc".equals(args[0])) {
                        return String.valueOf(-currentQty - 5);
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
        assertEquals("'inc' causes negative quantity", resp.getData());
    }

    @Test
    public void testDeleteCartItemSuccess() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, gson);

        fakeCartDao.resetDemoCartForUser6();
        try {
            Cart active = fakeCartDao.getUserActiveCart(TokenDao.DEMO_USER_ID);
            assertNotNull(active);
            assertFalse(active.getCartItems().isEmpty());
            String itemToDelete = active.getCartItems().get(0).getId();

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            AtomicInteger statusCode = new AtomicInteger(200);

            HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    (proxy, method, args) -> {
                        if ("getMethod".equals(method.getName())) return "DELETE";
                        if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
                            return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                        }
                        if ("getParameter".equals(method.getName()) && "ciid".equals(args[0])) {
                            return itemToDelete;
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
            assertNotNull(resp.getMeta());
            assertTrue(resp.getMeta().getServiceName().contains("Drop Cart Item"));
        } finally {
            fakeCartDao.resetDemoCartForUser6();
        }
    }

    @Test
    public void testDeleteCartItemNotFoundReturns404() throws Exception {
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
                    if ("getMethod".equals(method.getName())) return "DELETE";
                    if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
                        return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                    }
                    if ("getParameter".equals(method.getName()) && "ciid".equals(args[0])) {
                        return "non-existing-ciid-12345";
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
        assertEquals("Cart item not found", resp.getData());
    }

    @Test
    public void testDeleteCartItemOtherEntityReturns403() throws Exception {
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
                    if ("getMethod".equals(method.getName())) return "DELETE";
                    if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
                        return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                    }
                    if ("getParameter".equals(method.getName()) && "ciid".equals(args[0])) {
                        return "77777777-7777-7777-7777-777777777777"; // other entity
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
    public void testDeleteCartUnauthorizedReturns401() throws Exception {
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
                    if ("getMethod".equals(method.getName())) return "DELETE";
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
    public void testCartHistoryReturnsArray() throws Exception {
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
                    if ("getMethod".equals(method.getName())) return "GET";
                    if ("getPathInfo".equals(method.getName())) return "/history";
                    if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
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
        assertEquals(200, statusCode.get());

        String jsonString = stringWriter.toString();
        assertTrue(jsonString.contains("\"cartItems\":"));
        assertTrue(jsonString.contains("\"createDt\":"));
        assertTrue(jsonString.contains("\"deleteDt\":"));
        assertTrue(jsonString.contains("\"orderDt\":"));
        assertFalse(jsonString.contains("\"userId\":"));
        assertFalse(jsonString.contains("\"openAt\":"));
        assertFalse(jsonString.contains("\"closeAt\":"));

        RestResponse resp = new Gson().fromJson(jsonString, RestResponse.class);
        assertNotNull(resp);
        assertEquals(200, resp.getStatus().getCode());
        assertNotNull(resp.getMeta());
        assertEquals("array", resp.getMeta().getDataType());
        assertNotNull(resp.getData());
        assertTrue(resp.getData() instanceof java.util.List);
    }

    @Test
    public void testCartHistoryContainsFullImageUrlsForProductAndProductVersion() throws Exception {
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
                    if ("getMethod".equals(method.getName())) return "GET";
                    if ("getPathInfo".equals(method.getName())) return "/history";
                    if ("getServerName".equals(method.getName())) return "localhost";
                    if ("getServerPort".equals(method.getName())) return 8080;
                    if ("getScheme".equals(method.getName())) return "http";
                    if ("getContextPath".equals(method.getName())) return "/java231web";
                    if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
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
        assertEquals(200, statusCode.get());

        String jsonString = stringWriter.toString();
        assertTrue(jsonString.contains("http://localhost:8080/java231web/storage/"));
        assertTrue(jsonString.contains("\"imageUrl\":"));
    }

    @Test
    public void testDeleteCartItemBelongsToClosedCartReturns409() throws Exception {
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
                    if ("getMethod".equals(method.getName())) return "DELETE";
                    if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
                        return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                    }
                    if ("getParameter".equals(method.getName()) && "ciid".equals(args[0])) {
                        return "88888888-8888-8888-8888-888888888888"; // closed cart item of demo user
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
        assertEquals(409, statusCode.get());
        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals(409, resp.getStatus().getCode());
        assertTrue(resp.getData().toString().contains("closed"));
    }

    @Test
    public void testDeleteCartItemUserHasNoOpenCartReturns422() throws Exception {
        CartDao fakeCartDao = new CartDao(null) {
            @Override
            public Cart getUserActiveCart(String userId) {
                return null; // Simulate no active cart
            }
        };
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
                    if ("getMethod".equals(method.getName())) return "DELETE";
                    if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
                        return "Bearer " + TokenDao.DEMO_TOKEN_ID;
                    }
                    if ("getParameter".equals(method.getName()) && "ciid".equals(args[0])) {
                        return "88888888-8888-8888-8888-888888888888"; // owned by demo user
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
        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals(422, resp.getStatus().getCode());
        assertTrue(resp.getData().toString().contains("no open cart"));
    }

    @Test
    public void testCartOrderedReturnsOnlyStatus1() throws Exception {
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
                    if ("getMethod".equals(method.getName())) return "GET";
                    if ("getPathInfo".equals(method.getName())) return "/ordered";
                    if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
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
        assertEquals(200, statusCode.get());

        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals(200, resp.getStatus().getCode());
        assertNotNull(resp.getMeta());
        assertEquals("array", resp.getMeta().getDataType());
        assertNotNull(resp.getData());
        assertTrue(resp.getData() instanceof java.util.List);

        java.util.List<?> list = (java.util.List<?>) resp.getData();
        assertFalse(list.isEmpty());
        Gson cartGson = CartServlet.buildCartGson();
        for (Object item : list) {
            String itemJson = cartGson.toJson(item);
            Cart c = cartGson.fromJson(itemJson, Cart.class);
            assertNotNull(c.getStatus());
            assertEquals(1, c.getStatus());
            assertNotNull(c.getCloseAt());
        }
    }

    @Test
    public void testCartDroppedReturnsOnlyStatus0() throws Exception {
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
                    if ("getMethod".equals(method.getName())) return "GET";
                    if ("getPathInfo".equals(method.getName())) return "/dropped";
                    if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
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
        assertEquals(200, statusCode.get());

        RestResponse resp = new Gson().fromJson(stringWriter.toString(), RestResponse.class);
        assertNotNull(resp);
        assertEquals(200, resp.getStatus().getCode());
        assertNotNull(resp.getMeta());
        assertEquals("array", resp.getMeta().getDataType());
        assertNotNull(resp.getData());
        assertTrue(resp.getData() instanceof java.util.List);

        java.util.List<?> list = (java.util.List<?>) resp.getData();
        assertFalse(list.isEmpty());
        Gson cartGson = CartServlet.buildCartGson();
        for (Object item : list) {
            String itemJson = cartGson.toJson(item);
            Cart c = cartGson.fromJson(itemJson, Cart.class);
            assertNotNull(c.getStatus());
            assertEquals(0, c.getStatus());
            assertNotNull(c.getCloseAt());
        }
    }

    @Test
    public void testCartOrderedUnauthorizedReturns401() throws Exception {
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
                    if ("getMethod".equals(method.getName())) return "GET";
                    if ("getPathInfo".equals(method.getName())) return "/ordered";
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
    public void testCartDroppedUnauthorizedReturns401() throws Exception {
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
                    if ("getMethod".equals(method.getName())) return "GET";
                    if ("getPathInfo".equals(method.getName())) return "/dropped";
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
}
