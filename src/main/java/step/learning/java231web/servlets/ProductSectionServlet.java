package step.learning.java231web.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import step.learning.java231web.data.dao.ProductSectionDao;
import step.learning.java231web.data.dao.ProductVersionDao;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;

@Singleton
public class ProductSectionServlet extends HttpServlet {
    private final ProductSectionDao productSectionDao;
    private final ProductVersionDao productVersionDao;
    private final Gson gson;

    @Inject
    public ProductSectionServlet(ProductSectionDao productSectionDao, ProductVersionDao productVersionDao) {
        this.productSectionDao = productSectionDao;
        this.productVersionDao = productVersionDao;
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    public ProductSectionServlet(ProductSectionDao productSectionDao, ProductVersionDao productVersionDao, Gson gson) {
        this.productSectionDao = productSectionDao;
        this.productVersionDao = productVersionDao;
        this.gson = gson != null ? gson : new GsonBuilder().serializeNulls().create();
    }

    public ProductSectionServlet(ProductSectionDao productSectionDao, Gson gson) {
        this(productSectionDao, null, gson);
    }

    public ProductSectionServlet(ProductSectionDao productSectionDao) {
        this(productSectionDao, null, null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        if (productSectionDao != null) {
            productSectionDao.ensureInstalled();
        }
        if (productVersionDao != null) {
            productVersionDao.ensureInstalled();
        }

        String pathInfo = req.getPathInfo();
        String slug = null;
        if (pathInfo != null && !pathInfo.trim().isEmpty() && !pathInfo.equals("/")) {
            slug = pathInfo.trim();
            if (slug.startsWith("/")) {
                slug = slug.substring(1);
            }
            if (slug.endsWith("/")) {
                slug = slug.substring(0, slug.length() - 1);
            }
        }

        int page = 1;
        int perPage = 10;

        String pageParam = req.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
                if (page < 1) {
                    sendRest(req, resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                            "Query parameter 'page' must be greater than 0", null, "object");
                    return;
                }
            } catch (NumberFormatException ex) {
                sendRest(req, resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                        "Query parameter 'page' must be a valid integer", null, "object");
                return;
            }
        }

        String perPageParam = req.getParameter("pageSize");
        if (perPageParam == null || perPageParam.trim().isEmpty()) {
            perPageParam = req.getParameter("perPage");
        }
        if (perPageParam == null || perPageParam.trim().isEmpty()) {
            perPageParam = req.getParameter("size");
        }
        if (perPageParam != null && !perPageParam.trim().isEmpty()) {
            try {
                perPage = Integer.parseInt(perPageParam.trim());
                if (perPage < 1) {
                    sendRest(req, resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                            "Query parameter 'pageSize' must be greater than 0", null, "object");
                    return;
                }
            } catch (NumberFormatException ex) {
                sendRest(req, resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                        "Query parameter 'pageSize' must be a valid integer", null, "object");
                return;
            }
        }

        RestPagination pagination = new RestPagination(page, perPage, 0);

        try {
            if (slug == null || slug.isEmpty()) {
                // Запит усіх розділів
                long total = productSectionDao.getSectionsCount();
                pagination.setTotal((int) total);
                List<ProductSection> sections = productSectionDao.getSections(pagination);
                sendRest(req, resp, HttpServletResponse.SC_OK, RestStatus.Ok, sections, pagination, "array");
            } else {
                // Запит конкретного розділу за слагом з каскадом товарів
                ProductSection section = productSectionDao.getSectionBySlug(slug, pagination);
                if (section == null) {
                    sendRest(req, resp, HttpServletResponse.SC_NOT_FOUND, RestStatus.NotFound, "Section not found", null, "object");
                    return;
                }

                if (productVersionDao != null && section.getProducts() != null) {
                    for (Product p : section.getProducts()) {
                        List<ProductVersion> versions = productVersionDao.getVersionsByProductId(p.getId());
                        p.setVersions(versions != null ? versions : new ArrayList<>());
                    }
                }

                long totalProducts = productSectionDao.getProductsCountBySectionSlug(slug);
                pagination.setTotal((int) totalProducts);
                sendRest(req, resp, HttpServletResponse.SC_OK, RestStatus.Ok, section, pagination, "object");
            }
        } catch (SQLException ex) {
            sendRest(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RestStatus.DatabaseError,
                    ex.getMessage(), null, "object");
        } catch (Exception ex) {
            sendRest(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RestStatus.InternalServerError,
                    ex.getMessage(), null, "object");
        }
    }

    private void sendRest(HttpServletRequest req, HttpServletResponse resp, int httpStatusCode, RestStatus status, Object data, RestPagination pagination, String dataType) throws IOException {
        resp.setStatus(httpStatusCode);
        resp.setContentType("application/json; charset=UTF-8");

        String baseUrl = (req != null && req.getServerName() != null)
                ? req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath()
                : "http://localhost:8080/java231web";

        normalizeImageUrls(data, baseUrl);

        RestResponse restResponse = new RestResponse(status, data);
        if (restResponse.getMeta() != null) {
            restResponse.getMeta().setServiceName("API 'Product Sections'");
            restResponse.getMeta().setDataType(dataType);
            restResponse.getMeta().setManipulations(Arrays.asList("GET"));
        }
        if (pagination != null) {
            restResponse.setPagination(pagination);
        }
        resp.getWriter().print(gson.toJson(restResponse));
    }

    @SuppressWarnings("unchecked")
    private void normalizeImageUrls(Object data, String baseUrl) {
        if (data instanceof List) {
            for (Object item : (List<?>) data) {
                if (item instanceof ProductSection) {
                    normalizeSection((ProductSection) item, baseUrl);
                }
            }
        } else if (data instanceof ProductSection) {
            normalizeSection((ProductSection) data, baseUrl);
        }
    }

    private void normalizeSection(ProductSection section, String baseUrl) {
        if (section == null) return;
        if (section.getProducts() != null) {
            for (Product p : section.getProducts()) {
                p.setImageUrl(fixImageUrl(p.getImageUrl(), baseUrl));
            }
        }
        if (section.getChildren() != null) {
            for (ProductSection child : section.getChildren()) {
                normalizeSection(child, baseUrl);
            }
        }
    }

    private String fixImageUrl(String raw, String baseUrl) {
        if (raw == null || raw.trim().isEmpty() || raw.equals("null")) {
            return null;
        }
        if (raw.startsWith("http://localhost:8080/") || raw.startsWith("http://127.0.0.1:8080/")) {
            return raw;
        }
        String filename = raw;
        int slash = filename.lastIndexOf('/');
        if (slash >= 0) {
            filename = filename.substring(slash + 1);
        }
        return baseUrl + "/storage/" + filename;
    }
}
