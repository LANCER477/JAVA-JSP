package step.learning.java231web.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import step.learning.java231web.data.dao.CartDao;
import step.learning.java231web.data.dao.ProductVersionDao;
import step.learning.java231web.data.dao.TokenDao;
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.data.dto.UserToken;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;

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
                            // Invalid token ending with extra 1
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
        assertEquals("null", restResponse.getMeta().getDataType());
    }

    @Test
    public void testGetCartReturnsUserTokenWhenAuthorized() throws Exception {
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
        UserToken token = gson.fromJson(result, UserToken.class);

        assertNotNull(token);
        assertEquals(TokenDao.DEMO_TOKEN_ID, token.getId());
        assertEquals(TokenDao.DEMO_USER_ID, token.getUserId());
        assertEquals("user6", token.getLogin());
        assertEquals("7ff8e35c22d04efa", token.getSalt());
        assertEquals("66bd532cf48ca2ad32ace9d900dc0d82", token.getDk());
        assertEquals("guest", token.getRole());
    }

    @Test
    public void testPostCartMissingPvidReturnsMissingParameterMessage() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        ProductVersionDao fakeProductVersionDao = new ProductVersionDao(null);

        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, fakeProductVersionDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(200);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getParameter".equals(method.getName()) && args != null && args.length > 0) {
                        if ("pvid".equals(args[0])) {
                            return null; // missing pvid parameter
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

        servlet.doPost(request, response);

        assertEquals(200, statusCode.get());

        String result = stringWriter.toString().trim();
        RestResponse restResponse = new Gson().fromJson(result, RestResponse.class);

        assertNotNull(restResponse);
        assertEquals("Missing required parameter 'pvid'", restResponse.getData());
        assertNotNull(restResponse.getMeta());
        assertEquals("string", restResponse.getMeta().getDataType());
        assertEquals("API 'User carts'", restResponse.getMeta().getServiceName());
        assertTrue(restResponse.getMeta().getManipulations().contains("POST"));
        assertNotNull(restResponse.getMeta().getLinks());
        assertEquals("/cart", restResponse.getMeta().getLinks().get("base"));
        assertEquals("/cart/{item-id}", restResponse.getMeta().getLinks().get("details"));
    }

    @Test
    public void testPostCartDeletedProductReturnsProductVersionDeleted() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        ProductVersionDao fakeProductVersionDao = new ProductVersionDao(null) {
            @Override
            public ProductVersion getVersionById(String versionId) {
                if ("5c2052dc-cd4c-4ac4-8bdd-9719415afa96".equals(versionId)) {
                    ProductVersion pv = new ProductVersion();
                    pv.setId(versionId);
                    pv.setDeletedAt(new java.util.Date());
                    return pv;
                }
                return null;
            }
            @Override
            public void ensureInstalled() {}
        };

        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, fakeProductVersionDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getParameter".equals(method.getName()) && args != null && args.length > 0) {
                        if ("pvid".equals(args[0])) {
                            return "5c2052dc-cd4c-4ac4-8bdd-9719415afa96";
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

        servlet.doPost(request, response);

        assertEquals(200, statusCode.get());

        String result = stringWriter.toString().trim();
        RestResponse restResponse = new Gson().fromJson(result, RestResponse.class);

        assertNotNull(restResponse);
        assertTrue(restResponse.getStatus().isOk());
        assertEquals("Product version deleted", restResponse.getData());
        assertEquals("string", restResponse.getMeta().getDataType());
    }

    @Test
    public void testPostCartOutOfStockReturnsProductVersionOutOfStock() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        ProductVersionDao fakeProductVersionDao = new ProductVersionDao(null) {
            @Override
            public ProductVersion getVersionById(String versionId) {
                if ("oos-id".equals(versionId)) {
                    ProductVersion pv = new ProductVersion();
                    pv.setId(versionId);
                    pv.setStock(0);
                    return pv;
                }
                return null;
            }
            @Override
            public void ensureInstalled() {}
        };

        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, fakeProductVersionDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getParameter".equals(method.getName()) && args != null && args.length > 0) {
                        if ("pvid".equals(args[0])) {
                            return "oos-id";
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

        servlet.doPost(request, response);

        assertEquals(200, statusCode.get());

        String result = stringWriter.toString().trim();
        RestResponse restResponse = new Gson().fromJson(result, RestResponse.class);

        assertNotNull(restResponse);
        assertTrue(restResponse.getStatus().isOk());
        assertEquals("Product version out of stock", restResponse.getData());
        assertEquals("string", restResponse.getMeta().getDataType());
    }

    @Test
    public void testPostCartProductNotFoundReturnsNullData() throws Exception {
        CartDao fakeCartDao = new CartDao(null);
        TokenDao fakeTokenDao = new TokenDao(null);
        ProductVersionDao fakeProductVersionDao = new ProductVersionDao(null) {
            @Override
            public ProductVersion getVersionById(String versionId) {
                return null;
            }
            @Override
            public void ensureInstalled() {}
        };

        Gson gson = new GsonBuilder().serializeNulls().create();
        CartServlet servlet = new CartServlet(fakeCartDao, fakeTokenDao, fakeProductVersionDao, gson);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        AtomicInteger statusCode = new AtomicInteger(0);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getParameter".equals(method.getName()) && args != null && args.length > 0) {
                        if ("pvid".equals(args[0])) {
                            return "wrong-id";
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

        servlet.doPost(request, response);

        assertEquals(200, statusCode.get());

        String result = stringWriter.toString().trim();
        RestResponse restResponse = new Gson().fromJson(result, RestResponse.class);

        assertNotNull(restResponse);
        assertFalse(restResponse.getStatus().isOk());
        assertEquals(404, restResponse.getStatus().getCode());
        assertNull(restResponse.getData());
        assertEquals("null", restResponse.getMeta().getDataType());
    }
}
