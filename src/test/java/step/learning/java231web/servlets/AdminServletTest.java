package step.learning.java231web.servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AdminServletTest {

    private AdminServlet adminServlet;

    @BeforeEach
    public void setUp() {
        adminServlet = new AdminServlet();
    }

    @Test
    public void testAdminHomeRouteDefault() throws Exception {
        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest(null, ctx);
        HttpServletResponse response = createMockResponse(ctx);

        adminServlet.doGet(request, response);

        assertEquals("Admin", ctx.attributes.get("servlet"));
        assertEquals("home", ctx.attributes.get("adminPage"));
        assertEquals("/admin_home.jsp", ctx.attributes.get("adminView"));
        assertEquals(200, ctx.status);
        assertEquals("/index.jsp", ctx.forwardedUrl);
    }

    @Test
    public void testAdminProductRoute() throws Exception {
        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest("/product", ctx);
        HttpServletResponse response = createMockResponse(ctx);

        adminServlet.doGet(request, response);

        assertEquals("product", ctx.attributes.get("adminPage"));
        assertEquals("/admin_product.jsp", ctx.attributes.get("adminView"));
        assertNotNull(ctx.attributes.get("sections"));
        assertNotNull(ctx.attributes.get("viewModel"));
        assertTrue(ctx.attributes.get("viewModel") instanceof step.learning.java231web.data.models.AdminProductViewModel);
        @SuppressWarnings("unchecked")
        List<String> sections = (List<String>) ctx.attributes.get("sections");
        assertTrue(sections.contains("Garminfull"));
        assertEquals(200, ctx.status);
    }

    @Test
    public void testAdminProductVersionDoPost() throws Exception {
        Map<String, String> fields = new HashMap<>();
        fields.put("product-id", "prod-123");
        fields.put("product-title", "Red Switch");
        fields.put("product-description", "Red Cherry MX Switch Edition");
        fields.put("product-slug", "red-switch");
        fields.put("product-price", "2999.00");
        fields.put("product-stock", "15");
        fields.put("product-order", "1");

        step.learning.java231web.services.form.FormParseResult formResult = new step.learning.java231web.services.form.FormParseResult() {
            @Override
            public Map<String, String> getFields() {
                return fields;
            }

            @Override
            public Map<String, org.apache.commons.fileupload2.core.FileItem> getFiles() {
                return java.util.Collections.emptyMap();
            }
        };

        step.learning.java231web.services.form.IFormParseService mockFormService = req -> formResult;
        AdminServlet servletWithMock = new AdminServlet(null, mockFormService, null, null, null, null);

        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest("/product-version", ctx);
        HttpServletResponse response = createMockResponse(ctx);

        servletWithMock.doPost(request, response);

        assertNotNull(ctx.attributes.get("viewModel"));
        step.learning.java231web.data.models.AdminProductViewModel vm =
                (step.learning.java231web.data.models.AdminProductViewModel) ctx.attributes.get("viewModel");
        assertNotNull(vm.getOkMessage());
        assertTrue(vm.getOkMessage().contains("Red Switch"));
        assertEquals("product", ctx.attributes.get("adminPage"));
    }

    @Test
    public void testAdminProductVersionDoPostValidationErrors() throws Exception {
        Map<String, String> invalidFields = new HashMap<>();
        invalidFields.put("product-id", ""); // missing product id
        invalidFields.put("product-title", "X"); // too short
        invalidFields.put("product-description", ""); // empty description
        invalidFields.put("product-slug", "INVALID SLUG!");
        invalidFields.put("product-price", "-10"); // negative price
        invalidFields.put("product-stock", "-5"); // negative stock
        invalidFields.put("product-order", "-1"); // negative order

        step.learning.java231web.services.form.FormParseResult formResult = new step.learning.java231web.services.form.FormParseResult() {
            @Override
            public Map<String, String> getFields() {
                return invalidFields;
            }

            @Override
            public Map<String, org.apache.commons.fileupload2.core.FileItem> getFiles() {
                return java.util.Collections.emptyMap();
            }
        };

        step.learning.java231web.services.form.IFormParseService mockFormService = req -> formResult;
        AdminServlet servletWithMock = new AdminServlet(null, mockFormService, null, null, null, null);

        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest("/product-version", ctx);
        HttpServletResponse response = createMockResponse(ctx);

        servletWithMock.doPost(request, response);

        assertNotNull(ctx.attributes.get("validationErrors"));
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) ctx.attributes.get("validationErrors");
        assertTrue(errors.containsKey("product-id"));
        assertTrue(errors.containsKey("product-title"));
        assertTrue(errors.containsKey("product-description"));
        assertTrue(errors.containsKey("product-slug"));
        assertTrue(errors.containsKey("product-price"));
        assertTrue(errors.containsKey("product-stock"));
        assertTrue(errors.containsKey("product-order"));
        assertEquals("version", ctx.attributes.get("formType"));
        assertNotNull(ctx.attributes.get("formError"));
        assertEquals("product", ctx.attributes.get("adminPage"));
    }

    @Test
    public void testAdminSectionRoute() throws Exception {
        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest("/section", ctx);
        HttpServletResponse response = createMockResponse(ctx);

        adminServlet.doGet(request, response);

        assertEquals("section", ctx.attributes.get("adminPage"));
        assertEquals("/admin_section.jsp", ctx.attributes.get("adminView"));
        assertEquals(200, ctx.status);
    }

    @Test
    public void testAdminInternalRouteNotFoundReturns404() throws Exception {
        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest("/unknown-subpage", ctx);
        HttpServletResponse response = createMockResponse(ctx);

        adminServlet.doGet(request, response);

        assertEquals(404, ctx.status, "Internal route not found must return HTTP 404");
        assertEquals("404", ctx.attributes.get("adminPage"));
        assertEquals("/admin_404.jsp", ctx.attributes.get("adminView"));
        assertEquals("/unknown-subpage", ctx.attributes.get("invalidRoute"));
        assertEquals("/index.jsp", ctx.forwardedUrl);
    }

    @Test
    public void testAdminProductDoPostValidationErrors() throws Exception {
        Map<String, String> invalidFields = new HashMap<>();
        invalidFields.put("product-title", "A"); // too short
        invalidFields.put("product-price", "-5"); // negative price
        invalidFields.put("product-slug", "invalid slug with spaces");

        step.learning.java231web.services.form.FormParseResult formResult = new step.learning.java231web.services.form.FormParseResult() {
            @Override
            public Map<String, String> getFields() {
                return invalidFields;
            }

            @Override
            public Map<String, org.apache.commons.fileupload2.core.FileItem> getFiles() {
                return java.util.Collections.emptyMap();
            }
        };

        step.learning.java231web.services.form.IFormParseService mockFormService = req -> formResult;

        AdminServlet servletWithMock = new AdminServlet(null, mockFormService, null);

        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest("/product", ctx);
        HttpServletResponse response = createMockResponse(ctx);

        servletWithMock.doPost(request, response);

        assertNotNull(ctx.attributes.get("validationErrors"));
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) ctx.attributes.get("validationErrors");
        assertTrue(errors.containsKey("product-title"));
        assertTrue(errors.containsKey("product-price"));
        assertTrue(errors.containsKey("product-slug"));
        assertTrue(errors.containsKey("product-image"));
        assertNotNull(ctx.attributes.get("formError"));
        assertEquals("product", ctx.attributes.get("adminPage"));
    }

    @Test
    public void testAdminDoPostSectionValidationErrors() throws Exception {
        Map<String, String> invalidFields = new HashMap<>();
        invalidFields.put("section-title", "");
        invalidFields.put("section-description", "short");
        invalidFields.put("section-slug", "INVALID_SLUG!");
        invalidFields.put("section-order", "-1");

        step.learning.java231web.services.form.FormParseResult formResult = new step.learning.java231web.services.form.FormParseResult() {
            @Override
            public Map<String, String> getFields() {
                return invalidFields;
            }

            @Override
            public Map<String, org.apache.commons.fileupload2.core.FileItem> getFiles() {
                return java.util.Collections.emptyMap();
            }
        };

        step.learning.java231web.services.form.IFormParseService mockFormService = req -> formResult;

        AdminServlet servletWithMock = new AdminServlet(null, mockFormService, null);

        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest("/section", ctx);
        HttpServletResponse response = createMockResponse(ctx);

        servletWithMock.doPost(request, response);

        assertNotNull(ctx.attributes.get("validationErrors"));
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) ctx.attributes.get("validationErrors");
        assertTrue(errors.containsKey("section-title"));
        assertTrue(errors.containsKey("section-slug"));
        assertTrue(errors.containsKey("section-order"));
        assertTrue(errors.containsKey("section-image"));
        assertNotNull(ctx.attributes.get("formError"));
        assertEquals("section", ctx.attributes.get("adminPage"));
    }

    private static class TestContext {
        int status = 200;
        Map<String, Object> attributes = new HashMap<>();
        String forwardedUrl;
    }

    private HttpServletRequest createMockRequest(String pathInfo, TestContext ctx) {
        RequestDispatcher dispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getPathInfo".equals(method.getName())) {
                        return pathInfo;
                    }
                    if ("setAttribute".equals(method.getName())) {
                        ctx.attributes.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getAttribute".equals(method.getName())) {
                        return ctx.attributes.get((String) args[0]);
                    }
                    if ("getRequestDispatcher".equals(method.getName())) {
                        ctx.forwardedUrl = (String) args[0];
                        return dispatcher;
                    }
                    return null;
                }
        );
    }

    private HttpServletResponse createMockResponse(TestContext ctx) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName())) {
                        ctx.status = (Integer) args[0];
                        return null;
                    }
                    return null;
                }
        );
    }
}
