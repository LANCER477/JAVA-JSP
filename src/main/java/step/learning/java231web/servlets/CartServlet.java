package step.learning.java231web.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import step.learning.java231web.data.dao.CartDao;
import step.learning.java231web.data.dao.ProductVersionDao;
import step.learning.java231web.data.dao.TokenDao;
import step.learning.java231web.data.dto.Cart;
import step.learning.java231web.data.dto.CartItem;
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.data.dto.UserToken;
import step.learning.java231web.rest.RestMeta;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;

@Singleton
public class CartServlet extends HttpServlet {
    private final CartDao cartDao;
    private final TokenDao tokenDao;
    private final ProductVersionDao productVersionDao;
    private final Gson gson;
    private final Gson restGson;
    private final RestMeta meta;

    @Inject
    public CartServlet(CartDao cartDao, TokenDao tokenDao, ProductVersionDao productVersionDao) {
        this(cartDao, tokenDao, productVersionDao, null);
    }

    public CartServlet(CartDao cartDao) {
        this(cartDao, null, null, null);
    }

    public CartServlet(CartDao cartDao, Gson gson) {
        this(cartDao, null, null, gson);
    }

    public CartServlet(CartDao cartDao, TokenDao tokenDao) {
        this(cartDao, tokenDao, null, null);
    }

    public CartServlet(CartDao cartDao, TokenDao tokenDao, Gson gson) {
        this(cartDao, tokenDao, null, gson);
    }

    public CartServlet(CartDao cartDao, TokenDao tokenDao, ProductVersionDao productVersionDao, Gson gson) {
        this.cartDao = cartDao;
        this.tokenDao = tokenDao;
        this.productVersionDao = productVersionDao;
        this.gson = gson != null ? gson : new GsonBuilder().serializeNulls().create();
        this.restGson = new Gson();
        this.meta = new RestMeta("API 'User carts'", "object");
        this.meta.setManipulations(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
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

        if (userToken != null) {
            Cart cart = null;
            if (cartDao != null) {
                try {
                    if (TokenDao.DEMO_USER_ID.equals(userToken.getUserId())) {
                        cartDao.resetDemoCartForUser6();
                    }
                    cart = cartDao.getUserActiveCart(userToken.getUserId());
                    if (cart == null) {
                        cart = cartDao.getOrCreateActiveCart(userToken.getUserId());
                    }
                } catch (Exception ignored) {
                }
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(gson.toJson(cart));
            return;
        }

        sendUnauthorized(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        if (cartDao != null) {
            cartDao.ensureInstalled();
        }
        if (tokenDao != null) {
            tokenDao.ensureInstalled();
        }
        if (productVersionDao != null) {
            productVersionDao.ensureInstalled();
        }

        UserToken userToken = getUserToken(req);
        if (userToken == null) {
            sendUnauthorized(resp);
            return;
        }
        String userId = userToken.getUserId();

        // прийом товару (версії) та додавання до кошику
        // - якщо немає активного кошику, то створюємо новий
        // - якщо є активний кошик, то перевіряємо чи є в ньому цей товар
        //  = якщо є, то вживаємо статус 208 "Already Reported"
        //  = якщо ні, то створюємо CartItem з відповідним товаром
        RestResponse restResponse = null;

        // перевіряємо чи є в запиті параметр pvid
        String pvid = req.getParameter("pvid");
        if (pvid == null) {
            pvid = extractPvidFromJson(req);
        }

        if (pvid == null) {
            restResponse = RestResponse.QueryParamMissed(
                    meta.setDataType("string"), 
                    "'pvid' - product version id"
            );
        }
        else {
            // перевіряємо чи є такий товар з pvid
            ProductVersion pv = null;
            boolean isQueryOk;
            try {
                pv = productVersionDao != null ? productVersionDao.getById(pvid) : null;
                isQueryOk = true;
            } 
            catch (SQLException ex) {
                restResponse = RestResponse.ErrorInternal(
                    meta.setDataType("null"), 
                    null
                );
                isQueryOk = false;
            }
            
            if (isQueryOk) {                
                if (pv == null) {
                    restResponse = RestResponse.NotFound(
                        meta.setDataType("null"), 
                        null
                    );
                }
                else if (pv.getDeletedAt() != null) {
                    restResponse = RestResponse.Unprocessable(
                        meta.setDataType("string"), 
                        "Product version deleted"
                    );
                }
                else if (pv.getStock() == 0) {
                    restResponse = RestResponse.Unprocessable(
                        meta.setDataType("string"), 
                        "Product out of stock"
                    );
                }
                else {
                    String cartItemId;
                    try {
                        cartItemId = cartDao != null ? cartDao.addToCart(userId, pv) : null;
                        if (cartItemId == null) {
                            restResponse = RestResponse.AlreadyReported(
                                    meta.setDataType("string"), 
                                    "Product is already in cart"
                            );
                        }
                        else {
                            restResponse = RestResponse.Ok(
                                    meta.setDataType("object"), 
                                    cartDao.getUserActiveCart(userId)
                            );
                        }
                    } 
                    catch (SQLException ex) {
                        restResponse = RestResponse.ErrorInternal(
                            meta.setDataType("null"), 
                            null
                        );
                    }                    
                }
            }
        }

        if (restResponse != null && restResponse.getStatus() != null) {
            resp.setStatus(restResponse.getStatus().getCode());
        }
        resp.getWriter().print( gson.toJson( restResponse ) );
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        if (cartDao != null) {
            cartDao.ensureInstalled();
        }
        if (tokenDao != null) {
            tokenDao.ensureInstalled();
        }

        UserToken userToken = getUserToken(req);
        if (userToken == null) {
            sendUnauthorized(resp);
            return;
        }
        String userId = userToken.getUserId();

        RestResponse restResponse = null;

        // Перевіряємо наявність параметра ciid
        String ciid = req.getParameter("ciid");
        if (ciid == null) {
            ciid = extractCiidFromJson(req);
        }

        if (ciid == null || ciid.trim().isEmpty()) {
            restResponse = RestResponse.QueryParamMissed(
                    meta.setDataType("string"),
                    "'ciid' - cart item id"
            );
        } else {
            // Перевіряємо наявність параметра delta (+1, -1) або cnt
            String deltaStr = req.getParameter("delta");
            String cntStr = req.getParameter("cnt");
            Integer delta = null;
            Integer cnt = null;

            if (deltaStr != null) {
                try {
                    delta = Integer.parseInt(deltaStr.trim());
                } catch (NumberFormatException ignored) {}
            }
            if (cntStr != null) {
                try {
                    cnt = Integer.parseInt(cntStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            if (delta == null && cnt == null) {
                JsonObject jsonBody = getJsonBody(req);
                if (jsonBody != null) {
                    if (jsonBody.has("delta") && !jsonBody.get("delta").isJsonNull()) {
                        try { delta = jsonBody.get("delta").getAsInt(); } catch (Exception ignored) {}
                    }
                    if (jsonBody.has("cnt") && !jsonBody.get("cnt").isJsonNull()) {
                        try { cnt = jsonBody.get("cnt").getAsInt(); } catch (Exception ignored) {}
                    }
                    if (jsonBody.has("quantity") && !jsonBody.get("quantity").isJsonNull()) {
                        try { cnt = jsonBody.get("quantity").getAsInt(); } catch (Exception ignored) {}
                    }
                }
            }

            if (delta == null && cnt == null) {
                restResponse = RestResponse.Unprocessable(
                        meta.setDataType("string"),
                        "Missing or invalid 'delta' or 'cnt' parameter"
                );
            } else {
                ciid = ciid.trim();
                // Шукаємо ciid в активному кошику користувача
                Cart cart = null;
                CartItem userItem = null;
                try {
                    cart = cartDao != null ? cartDao.getUserActiveCart(userId) : null;
                    if (cart != null && cart.getCartItems() != null) {
                        for (CartItem item : cart.getCartItems()) {
                            if (ciid.equalsIgnoreCase(item.getId())) {
                                userItem = item;
                                break;
                            }
                        }
                    }
                } catch (SQLException ex) {
                    restResponse = RestResponse.ErrorInternal(meta.setDataType("null"), null);
                }

                if (restResponse == null) {
                    if (userItem != null) {
                        // Товар знайдено в кошику користувача -> оновлюємо кількість
                        try {
                            int newQuantity = delta != null ? userItem.getQuantity() + delta : cnt;
                            if (newQuantity <= 0) {
                                cartDao.deleteCartItem(ciid);
                            } else {
                                cartDao.updateCartItemQuantity(ciid, newQuantity);
                            }
                            Cart updatedCart = cartDao.getUserActiveCart(userId);
                            restResponse = RestResponse.Ok(
                                    meta.setDataType("object"),
                                    updatedCart
                            );
                        } catch (SQLException ex) {
                            restResponse = RestResponse.ErrorInternal(meta.setDataType("null"), null);
                        }
                    } else {
                        // У разі, якщо в кошику користувача немає зазначеного ciid,
                        // здійснюємо перевірку чи такий запис взагалі існує в БД
                        try {
                            CartItem dbItem = cartDao != null ? cartDao.getCartItemById(ciid) : null;
                            if (dbItem == null) {
                                // Запис не існує в БД взагалі -> 404 Not Found
                                restResponse = RestResponse.NotFound(
                                        meta.setDataType("string"),
                                        "Cart item not found: " + ciid
                                );
                            } else {
                                // Запис існує в БД, але належить іншому кошику / іншій сутності -> 403 Forbidden
                                restResponse = RestResponse.Forbidden(
                                        meta.setDataType("string"),
                                        "Cart item belongs to another entity"
                                );
                            }
                        } catch (SQLException ex) {
                            restResponse = RestResponse.ErrorInternal(meta.setDataType("null"), null);
                        }
                    }
                }
            }
        }

        if (restResponse != null && restResponse.getStatus() != null) {
            resp.setStatus(restResponse.getStatus().getCode());
        }
        resp.getWriter().print(gson.toJson(restResponse));
    }

    private UserToken getUserToken(HttpServletRequest req) {
        if (req == null) return null;
        Object auth = req.getAttribute("auth");
        if (auth instanceof UserToken) {
            return (UserToken) auth;
        }
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

    private JsonObject getJsonBody(HttpServletRequest req) {
        if (req == null) return null;
        Object cached = req.getAttribute("jsonBody");
        if (cached instanceof JsonObject) {
            return (JsonObject) cached;
        }
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString().trim();
            if (!body.isEmpty() && body.startsWith("{")) {
                JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
                req.setAttribute("jsonBody", obj);
                return obj;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractCiidFromJson(HttpServletRequest req) {
        JsonObject obj = getJsonBody(req);
        if (obj != null) {
            if (obj.has("ciid") && !obj.get("ciid").isJsonNull()) {
                return obj.get("ciid").getAsString();
            }
            if (obj.has("cartItemId") && !obj.get("cartItemId").isJsonNull()) {
                return obj.get("cartItemId").getAsString();
            }
        }
        return null;
    }

    private String extractPvidFromJson(HttpServletRequest req) {
        JsonObject obj = getJsonBody(req);
        if (obj != null) {
            if (obj.has("pvid") && !obj.get("pvid").isJsonNull()) {
                return obj.get("pvid").getAsString();
            }
            if (obj.has("productVersionId") && !obj.get("productVersionId").isJsonNull()) {
                return obj.get("productVersionId").getAsString();
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
