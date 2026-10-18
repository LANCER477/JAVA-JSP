package step.learning.java231web.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import step.learning.java231web.data.dao.ProductDao;
import step.learning.java231web.data.dto.Product;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServletTest {
    private ProductDao fakeDao;
    private ProductServlet servlet;
    private Gson gson;

    @BeforeEach
    public void setUp() {
        gson = new GsonBuilder().serializeNulls().create();
        fakeDao = new ProductDao(null) {
            @Override
            public Product getProductByIdOrSlug(String idOrSlug) {
                if ("9590e16c-c6f7-44c1-aac7-5027f5caf11f".equals(idOrSlug)
                        || "/9590e16c-c6f7-44c1-aac7-5027f5caf11f".equals(idOrSlug)) {
                    Product p = new Product();
                    p.setId("9590e16c-c6f7-44c1-aac7-5027f5caf11f");
                    p.setSectionId("3a27db1d-d97e-406f-a36a-148b4f724a6e");
                    p.setTitle("Навушники Apple AirPods Pro 3 (MFHP4)(2025)");
                    p.setDescription("Навушники Apple AirPods Pro 3 (MFHP4)(2025)");
                    p.setSlug("Навушники Apple AirPods Pro 3 (MFHP4)(2025)");
                    p.setImageUrl("64181f77-8902-4eb3-82f8-45aad6eed9c0.png");
                    p.setOrderInPrice(2);
                    return p;
                }
                return null;
            }
        };
        servlet = new ProductServlet(fakeDao, gson);
    }

    @Test
    public void testGetProductNotFoundReturnsOkWithNullData() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        HttpServletRequest request = createMockRequest("/123");
        HttpServletResponse response = createMockResponse(printWriter);

        servlet.doGet(request, response);

        String json = stringWriter.toString();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        assertEquals(200, root.getAsJsonObject("status").get("code").getAsInt());
        assertTrue(root.getAsJsonObject("status").get("isOk").getAsBoolean());
        assertEquals("API 'Products'", root.getAsJsonObject("meta").get("serviceName").getAsString());
        assertEquals("123", root.getAsJsonObject("meta").getAsJsonObject("parameters").get("slug").getAsString());
        assertTrue(root.get("data").isJsonNull());
    }

    @Test
    public void testGetProductFoundReturnsProductData() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        HttpServletRequest request = createMockRequest("/9590e16c-c6f7-44c1-aac7-5027f5caf11f");
        HttpServletResponse response = createMockResponse(printWriter);

        servlet.doGet(request, response);

        String json = stringWriter.toString();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        assertEquals(200, root.getAsJsonObject("status").get("code").getAsInt());
        assertTrue(root.getAsJsonObject("status").get("isOk").getAsBoolean());
        assertFalse(root.get("data").isJsonNull());

        JsonObject data = root.getAsJsonObject("data");
        assertEquals("9590e16c-c6f7-44c1-aac7-5027f5caf11f", data.get("id").getAsString());
        assertEquals("Навушники Apple AirPods Pro 3 (MFHP4)(2025)", data.get("title").getAsString());
        assertTrue(data.get("imageUrl").getAsString().endsWith("64181f77-8902-4eb3-82f8-45aad6eed9c0.png"));
        assertEquals(2, data.get("orderInPrice").getAsInt());
        assertNotNull(data.get("versions"));
        assertTrue(data.get("versions").isJsonArray());
    }

    private HttpServletRequest createMockRequest(String pathInfo) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getPathInfo".equals(method.getName())) {
                        return pathInfo;
                    }
                    return null;
                }
        );
    }

    private HttpServletResponse createMockResponse(PrintWriter writer) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("getWriter".equals(method.getName())) {
                        return writer;
                    }
                    if ("setContentType".equals(method.getName()) || "setStatus".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );
    }
}
