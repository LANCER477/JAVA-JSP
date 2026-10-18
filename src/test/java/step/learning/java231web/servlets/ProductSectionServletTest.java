package step.learning.java231web.servlets;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import step.learning.java231web.data.dao.ProductSectionDao;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;
import step.learning.java231web.services.db.IDbService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ProductSectionServletTest {

    private ProductSectionDao fakeDao;
    private ProductSectionServlet servlet;
    private Gson gson;

    static class ResponseContext {
        int status = 200;
        String contentType;
        StringWriter body = new StringWriter();
    }

    private step.learning.java231web.data.dao.ProductVersionDao fakeVersionDao;

    @BeforeEach
    public void setUp() {
        gson = new Gson();
        fakeDao = new ProductSectionDao(null) {
            @Override
            public void ensureInstalled() {
            }

            @Override
            public ProductSection getSectionBySlug(String slug, RestPagination pagination) throws SQLException {
                if ("tv".equals(slug)) {
                    ProductSection section = new ProductSection();
                    section.setId("sec-tv");
                    section.setTitle("Телевізори");
                    section.setSlug("tv");
                    // Д.З.: Коригування поля ImageUrl для самого розділу (section)
                    section.setImageUrl("" + "tv_section_image.webp");

                    Product p = new Product();
                    p.setId("p-1");
                    p.setTitle("Samsung 55");
                    p.setImageUrl("" + "samsung_55.webp");
                    section.getProducts().add(p);
                    return section;
                }
                return null;
            }

            @Override
            public long getProductsCountBySectionSlug(String slug) throws SQLException {
                return "tv".equals(slug) ? 1 : 0;
            }

            @Override
            public long getSectionsCount() throws SQLException {
                return 1;
            }

            @Override
            public List<ProductSection> getSections(RestPagination pagination) throws SQLException {
                List<ProductSection> list = new ArrayList<>();
                ProductSection section = new ProductSection();
                section.setId("sec-tv");
                section.setTitle("Телевізори");
                section.setSlug("tv");
                section.setImageUrl("" + "tv_section_image.webp");
                ProductSection child = new ProductSection();
                child.setId("sec-oled");
                child.setTitle("OLED TV");
                child.setImageUrl("oled_child.webp");
                section.getChildren().add(child);
                list.add(section);
                return list;
            }
        };

        fakeVersionDao = new step.learning.java231web.data.dao.ProductVersionDao(null) {
            @Override
            public void ensureInstalled() {
            }

            @Override
            public List<step.learning.java231web.data.dto.ProductVersion> getVersionsByProductId(String productId) {
                List<step.learning.java231web.data.dto.ProductVersion> vList = new ArrayList<>();
                if ("p-1".equals(productId)) {
                    step.learning.java231web.data.dto.ProductVersion v = new step.learning.java231web.data.dto.ProductVersion();
                    v.setId("v-1");
                    v.setProductId("p-1");
                    v.setPrice(1000);
                    v.setStock(10);
                    vList.add(v);
                }
                return vList;
            }
        };

        servlet = new ProductSectionServlet(fakeDao, fakeVersionDao, gson);
    }

    private HttpServletRequest createMockRequest(String pathInfo, Map<String, String> params) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getPathInfo".equals(name)) return pathInfo;
                    if ("getParameter".equals(name)) {
                        String key = (String) args[0];
                        return params != null ? params.get(key) : null;
                    }
                    return null;
                }
        );
    }

    private HttpServletResponse createMockResponse(ResponseContext ctx) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("setStatus".equals(name)) {
                        ctx.status = (Integer) args[0];
                        return null;
                    }
                    if ("setContentType".equals(name)) {
                        ctx.contentType = (String) args[0];
                        return null;
                    }
                    if ("getWriter".equals(name)) {
                        return new PrintWriter(ctx.body);
                    }
                    return null;
                }
        );
    }

    @Test
    public void testGetSectionBySlugReturnsOkWithCorrectedImageUrl() throws Exception {
        ResponseContext ctx = new ResponseContext();
        HttpServletRequest request = createMockRequest("/tv", null);
        HttpServletResponse response = createMockResponse(ctx);

        servlet.doGet(request, response);

        assertEquals(200, ctx.status);
        String json = ctx.body.toString();
        RestResponse resp = gson.fromJson(json, RestResponse.class);

        assertNotNull(resp);
        assertEquals(200, resp.getStatus().getCode());
        assertTrue(resp.getStatus().isOk());
        assertNotNull(resp.getData());
        assertNotNull(resp.getPagination());
        assertEquals(1, resp.getPagination().getTotal());
        assertTrue(json.contains("tv_section_image.webp"));
        assertTrue(json.contains("samsung_55.webp"));
    }

    @Test
    public void testGetSectionNotFoundReturns404() throws Exception {
        ResponseContext ctx = new ResponseContext();
        HttpServletRequest request = createMockRequest("/unknown-slug", null);
        HttpServletResponse response = createMockResponse(ctx);

        servlet.doGet(request, response);

        assertEquals(404, ctx.status);
        String json = ctx.body.toString();
        RestResponse resp = gson.fromJson(json, RestResponse.class);
        assertNotNull(resp);
        assertEquals(404, resp.getStatus().getCode());
        assertFalse(resp.getStatus().isOk());
    }

    @Test
    public void testGetSectionInvalidPageReturns400() throws Exception {
        ResponseContext ctx = new ResponseContext();
        Map<String, String> params = new HashMap<>();
        params.put("page", "-1");
        HttpServletRequest request = createMockRequest("/tv", params);
        HttpServletResponse response = createMockResponse(ctx);

        servlet.doGet(request, response);

        assertEquals(400, ctx.status);
        String json = ctx.body.toString();
        RestResponse resp = gson.fromJson(json, RestResponse.class);
        assertNotNull(resp);
        assertEquals(443, resp.getStatus().getCode()); // QueryParameterMalformed
    }

    @Test
    public void testGetSectionsReturnsTreeWithPageSizeAndNormalizedChildren() throws Exception {
        ResponseContext ctx = new ResponseContext();
        Map<String, String> params = new HashMap<>();
        params.put("pageSize", "3");
        HttpServletRequest request = createMockRequest(null, params);
        HttpServletResponse response = createMockResponse(ctx);

        servlet.doGet(request, response);

        assertEquals(200, ctx.status);
        String json = ctx.body.toString();
        RestResponse resp = gson.fromJson(json, RestResponse.class);

        assertNotNull(resp);
        assertEquals(200, resp.getStatus().getCode());
        assertTrue(resp.getStatus().isOk());
        assertNotNull(resp.getPagination());
        assertEquals(3, resp.getPagination().getPageSize());
        assertTrue(json.contains("tv_section_image.webp"));
        assertTrue(json.contains("oled_child.webp"));
    }
}
