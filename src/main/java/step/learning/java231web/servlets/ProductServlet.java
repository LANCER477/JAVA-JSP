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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import step.learning.java231web.data.dao.ProductDao;
import step.learning.java231web.data.dao.ProductVersionDao;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;

@Singleton
public class ProductServlet extends HttpServlet {
    private final ProductDao productDao;
    private final ProductVersionDao productVersionDao;
    private final Gson gson;

    @Inject
    public ProductServlet(ProductDao productDao, ProductVersionDao productVersionDao) {
        this.productDao = productDao;
        this.productVersionDao = productVersionDao;
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    public ProductServlet(ProductDao productDao, ProductVersionDao productVersionDao, Gson gson) {
        this.productDao = productDao;
        this.productVersionDao = productVersionDao;
        this.gson = gson != null ? gson : new GsonBuilder().serializeNulls().create();
    }

    public ProductServlet(ProductDao productDao, Gson gson) {
        this(productDao, null, gson);
    }

    public ProductServlet(ProductDao productDao) {
        this(productDao, null, null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        if (productVersionDao != null) {
            productVersionDao.ensureInstalled();
        }

        String pathInfo = req.getPathInfo();
        String slug = pathInfo != null ? pathInfo : "";

        String baseUrl = (req != null && req.getServerName() != null)
                ? req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath()
                : "http://localhost:8080/java231web";

        Product product = null;
        if (productDao != null && pathInfo != null && !pathInfo.trim().isEmpty() && !pathInfo.equals("/")) {
            product = productDao.getProductByIdOrSlug(pathInfo);
        }

        if (product != null) {
            if (productVersionDao != null) {
                List<ProductVersion> versions = productVersionDao.getVersionsByProductId(product.getId());
                product.setVersions(versions != null ? versions : new ArrayList<>());
            }
            product.setImageUrl(fixImageUrl(product.getImageUrl(), baseUrl));
            if (product.getVersions() != null) {
                for (ProductVersion v : product.getVersions()) {
                    v.setImageUrl(fixImageUrl(v.getImageUrl(), baseUrl));
                }
            }
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
            if (paramSlug.startsWith("/")) {
                paramSlug = paramSlug.substring(1);
            }
            params.put("slug", paramSlug);
            restResponse.getMeta().setParameters(params);
            restResponse.getMeta().setPagination(null);
        }

        resp.getWriter().print(gson.toJson(restResponse));
    }

    private String fixImageUrl(String raw, String baseUrl) {
        if (raw == null || raw.trim().isEmpty() || raw.equals("null")) {
            return null;
        }
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
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
