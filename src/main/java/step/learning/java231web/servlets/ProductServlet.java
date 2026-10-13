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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import step.learning.java231web.data.dao.ProductDao;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;

@Singleton
public class ProductServlet extends HttpServlet {
    private final ProductDao productDao;
    private final Gson gson;

    @Inject
    public ProductServlet(ProductDao productDao) {
        this.productDao = productDao;
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    public ProductServlet(ProductDao productDao, Gson gson) {
        this.productDao = productDao;
        this.gson = gson != null ? gson : new GsonBuilder().serializeNulls().create();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String pathInfo = req.getPathInfo();
        String slug = pathInfo != null ? pathInfo : "";

        Product product = null;
        if (productDao != null && pathInfo != null && !pathInfo.trim().isEmpty() && !pathInfo.equals("/")) {
            product = productDao.getProductByIdOrSlug(pathInfo);
        }

        RestResponse restResponse = new RestResponse(RestStatus.Ok, product);
        if (restResponse.getMeta() != null) {
            restResponse.getMeta().setServiceName("API 'Products'");
            restResponse.getMeta().setDataType("object");
            restResponse.getMeta().setManipulations(Arrays.asList("GET"));

            Map<String, String> links = new LinkedHashMap<>();
            links.put("details", "/{slug}");
            restResponse.getMeta().setLinks(links);

            Map<String, String> params = new LinkedHashMap<>();
            String paramSlug = slug;
            if (product != null && paramSlug.startsWith("/")) {
                paramSlug = paramSlug.substring(1);
            }
            params.put("slug", paramSlug);
            restResponse.getMeta().setParameters(params);
            restResponse.getMeta().setPagination(null);
        }

        resp.getWriter().print(gson.toJson(restResponse));
    }
}
