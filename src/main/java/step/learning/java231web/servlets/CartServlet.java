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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import step.learning.java231web.data.dao.CartDao;
import step.learning.java231web.data.dao.ProductVersionDao;
import step.learning.java231web.data.dao.TokenDao;
import step.learning.java231web.data.dto.Cart;
import step.learning.java231web.data.dto.CartItem;
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.data.dto.UserToken;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;

@Singleton
public class CartServlet extends HttpServlet {
    private final CartDao cartDao;
    private final TokenDao tokenDao;
    private final ProductVersionDao productVersionDao;
    private final Gson gson;
    private final Gson restGson;

    @Inject
    public CartServlet(CartDao cartDao, TokenDao tokenDao, ProductVersionDao productVersionDao) {
        this.cartDao = cartDao;
        this.tokenDao = tokenDao;
        this.productVersionDao = productVersionDao;
        this.gson = new GsonBuilder().serializeNulls().create();
        this.restGson = this.gson;
    }

    public CartServlet(CartDao cartDao, TokenDao tokenDao, ProductVersionDao productVersionDao, Gson gson) {
        this.cartDao = cartDao;
        this.tokenDao = tokenDao;
        this.productVersionDao = productVersionDao;
        this.gson = gson != null ? gson : new GsonBuilder().serializeNulls().create();
        this.restGson = this.gson;
    }

    public CartServlet(CartDao cartDao, TokenDao tokenDao) {
        this(cartDao, tokenDao, null, null);
    }

    public CartServlet(CartDao cartDao) {
        this(cartDao, null, null, null);
    }

    public CartServlet(CartDao cartDao, Gson gson) {
        this(cartDao, null, null, gson);
    }

    public CartServlet(CartDao cartDao, TokenDao tokenDao, Gson gson) {
        this(cartDao, tokenDao, null, gson);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        if (cartDao != null) {
            cartDao.ensureInstalled();
        }
        if (tokenDao != null) {
            tokenDao.ensureInstalled();
        }

        UserToken userToken = getUserToken(req);

        // If valid user token was found -> 200 OK with UserToken JSON
        if (userToken != null) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(gson.toJson(userToken));
            return;
        }

        // If token is invalid or missing -> 401 Unauthorized with RestResponse
        sendUnauthorized(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        if (cartDao != null) {
            cartDao.ensureInstalled();
        }
        if (productVersionDao != null) {
            productVersionDao.ensureInstalled();
        }

        String pvid = req != null ? req.getParameter("pvid") : null;

        // 1. Missing pvid
        if (pvid == null || pvid.trim().isEmpty()) {
            RestResponse restResponse = new RestResponse(RestStatus.Ok, "Missing required parameter 'pvid'");
            fillCartMeta(restResponse, "string");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(restGson.toJson(restResponse));
            return;
        }

        UserToken userToken = getUserToken(req);
        String userId = userToken != null ? userToken.getUserId() : null;

        try {
            ProductVersion pv = productVersionDao != null ? productVersionDao.getVersionById(pvid.trim()) : null;
            if (pv == null) {
                RestResponse notFoundResponse = new RestResponse(RestStatus.NotFound, null);
                fillCartMeta(notFoundResponse, "null");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(restGson.toJson(notFoundResponse));
                return;
            }

            if (pv.getDeletedAt() != null) {
                RestResponse deletedResponse = new RestResponse(RestStatus.Ok, "Product version deleted");
                fillCartMeta(deletedResponse, "string");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(restGson.toJson(deletedResponse));
                return;
            }

            if (pv.getStock() <= 0) {
                RestResponse outOfStockResponse = new RestResponse(RestStatus.Ok, "Product version out of stock");
                fillCartMeta(outOfStockResponse, "string");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(restGson.toJson(outOfStockResponse));
                return;
            }

            Cart cart = cartDao != null ? cartDao.getOrCreateActiveCart(userId) : null;
            CartItem item = null;
            if (cart != null && cartDao != null) {
                item = cartDao.addOrUpdateItem(cart.getId(), pv.getProductId(), pv.getId(), 1, pv.getPrice());
            }

            RestResponse okResponse = new RestResponse(RestStatus.Ok, item != null ? item : "Product version added to cart");
            fillCartMeta(okResponse, item != null ? "object" : "string");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(restGson.toJson(okResponse));
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            RestResponse errResponse = new RestResponse(RestStatus.InternalServerError, ex.getMessage());
            fillCartMeta(errResponse, "string");
            resp.getWriter().print(restGson.toJson(errResponse));
        }
    }

    private void fillCartMeta(RestResponse restResponse, String dataType) {
        if (restResponse != null && restResponse.getMeta() != null) {
            restResponse.getMeta().setServiceName("API 'User carts'");
            restResponse.getMeta().setDataType(dataType);
            restResponse.getMeta().setServerTime(System.currentTimeMillis());
            restResponse.getMeta().setCacheTime(0);
            restResponse.getMeta().setManipulations(Arrays.asList("GET", "POST", "DELETE", "PATCH"));

            Map<String, String> links = new LinkedHashMap<>();
            links.put("details", "/cart/{item-id}");
            links.put("base", "/cart");
            restResponse.getMeta().setLinks(links);

            restResponse.getMeta().setParameters(null);
            restResponse.getMeta().setPagination(null);
        }
    }

    private UserToken getUserToken(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        // 1. Check attribute 'auth' populated by AuthFilter
        Object auth = req.getAttribute("auth");
        if (auth instanceof UserToken) {
            return (UserToken) auth;
        }

        // 2. Direct check from Authorization header as fallback
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
            String tokenId = authHeader.substring(7).trim();
            if (tokenDao != null) {
                UserToken token = tokenDao.getToken(tokenId);
                if (token != null) {
                    return token;
                }
            }
            if (TokenDao.DEMO_TOKEN_ID.equalsIgnoreCase(tokenId)) {
                return new UserToken(
                        TokenDao.DEMO_TOKEN_ID,
                        TokenDao.DEMO_USER_ID,
                        TokenDao.DEMO_LOGIN,
                        TokenDao.DEMO_SALT,
                        TokenDao.DEMO_DK,
                        TokenDao.DEMO_ROLE
                );
            }
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        RestResponse restResponse = new RestResponse(RestStatus.Unauthorized, null);
        if (restResponse.getMeta() != null) {
            restResponse.getMeta().setServiceName("API 'User carts'");
            restResponse.getMeta().setDataType("null");
            restResponse.getMeta().setServerTime(System.currentTimeMillis());
            restResponse.getMeta().setCacheTime(0);
            restResponse.getMeta().setManipulations(Collections.singletonList("GET"));
            restResponse.getMeta().setLinks(null);
            restResponse.getMeta().setParameters(null);
            restResponse.getMeta().setPagination(null);
        }

        resp.getWriter().print(restGson.toJson(restResponse));
    }
}
