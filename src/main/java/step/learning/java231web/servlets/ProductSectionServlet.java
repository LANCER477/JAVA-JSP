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
import java.util.List;
import step.learning.java231web.data.dao.ProductSectionDao;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;

@Singleton
public class ProductSectionServlet extends HttpServlet {
    private final ProductSectionDao productSectionDao;
    private final Gson gson;

    @Inject
    public ProductSectionServlet(ProductSectionDao productSectionDao) {
        this.productSectionDao = productSectionDao;
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    public ProductSectionServlet(ProductSectionDao productSectionDao, Gson gson) {
        this.productSectionDao = productSectionDao;
        this.gson = gson != null ? gson : new GsonBuilder().serializeNulls().create();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        if (productSectionDao != null) {
            productSectionDao.ensureInstalled();
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
                    sendRest(resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                            "Query parameter 'page' must be greater than 0", null);
                    return;
                }
            } catch (NumberFormatException ex) {
                sendRest(resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                        "Query parameter 'page' must be a valid integer", null);
                return;
            }
        }

        String perPageParam = req.getParameter("perPage");
        if (perPageParam == null || perPageParam.trim().isEmpty()) {
            perPageParam = req.getParameter("size");
        }
        if (perPageParam != null && !perPageParam.trim().isEmpty()) {
            try {
                perPage = Integer.parseInt(perPageParam.trim());
                if (perPage < 1) {
                    sendRest(resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                            "Query parameter 'perPage' must be greater than 0", null);
                    return;
                }
            } catch (NumberFormatException ex) {
                sendRest(resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                        "Query parameter 'perPage' must be a valid integer", null);
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
                sendRest(resp, HttpServletResponse.SC_OK, RestStatus.Ok, sections, pagination);
            } else {
                // Запит конкретного розділу за слагом з каскадом товарів
                ProductSection section = productSectionDao.getSectionBySlug(slug, pagination);
                if (section == null) {
                    sendRest(resp, HttpServletResponse.SC_NOT_FOUND, RestStatus.NotFound, "Section not found", null);
                    return;
                }

                long totalProducts = productSectionDao.getProductsCountBySectionSlug(slug);
                pagination.setTotal((int) totalProducts);
                sendRest(resp, HttpServletResponse.SC_OK, RestStatus.Ok, section, pagination);
            }
        } catch (SQLException ex) {
            sendRest(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RestStatus.DatabaseError,
                    ex.getMessage(), null);
        } catch (Exception ex) {
            sendRest(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RestStatus.InternalServerError,
                    ex.getMessage(), null);
        }
    }

    private void sendRest(HttpServletResponse resp, int httpStatusCode, RestStatus status, Object data, RestPagination pagination) throws IOException {
        resp.setStatus(httpStatusCode);
        RestResponse restResponse = new RestResponse(status, data);
        if (pagination != null) {
            restResponse.setPagination(pagination);
        }
        restResponse.getMeta().setService("ProductSectionServlet");
        resp.getWriter().print(gson.toJson(restResponse));
    }
}
