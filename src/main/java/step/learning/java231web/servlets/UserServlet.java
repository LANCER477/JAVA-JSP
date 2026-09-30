package step.learning.java231web.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import step.learning.java231web.dao.UserDao;
import step.learning.java231web.models.UserAccessItem;
import step.learning.java231web.models.UserSignupFormModel;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;

@Singleton
public class UserServlet extends HttpServlet {
    private final UserDao userDao;
    private final Gson gson;

    @Inject
    public UserServlet(UserDao userDao) {
        this.userDao = userDao;
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String body;
        try (BufferedReader reader = req.getReader()) {
            body = reader.lines().collect(Collectors.joining("\n"));
        }

        try {
            UserSignupFormModel formModel = gson.fromJson(body, UserSignupFormModel.class);
            if (formModel == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print(gson.toJson(new RestResponse(RestStatus.BadRequest, "Body is empty")));
                return;
            }

            userDao.signupUser(formModel);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("name", formModel.getName());
            result.put("email", formModel.getEmail());
            result.put("login", formModel.getLogin() != null ? formModel.getLogin() : formModel.getEmail());
            result.put("message", "User registered successfully");

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(gson.toJson(new RestResponse(RestStatus.Created, result)));
        } catch (IllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(gson.toJson(new RestResponse(RestStatus.BadRequest, ex.getMessage())));
        } catch (Exception ex) {
            System.getLogger(UserServlet.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(gson.toJson(new RestResponse(RestStatus.InternalServerError, ex.getMessage())));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String servletPath = req.getServletPath();
        boolean isAccess = (pathInfo != null && (pathInfo.equals("/access") || pathInfo.equals("/access/")))
                || (servletPath != null && (servletPath.endsWith("/access") || servletPath.endsWith("/access/")));

        if (isAccess) {
            access(req, resp);
        } else {
            authenticate(req, resp);
        }
    }

    public void access(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String pageParam = req.getParameter("page");
        String perPageParam = req.getParameter("perPage");
        if (perPageParam == null) {
            perPageParam = req.getParameter("size");
        }

        int page = 1;
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
                if (page < 1) {
                    sendRest(resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                            "Query parameter 'page' must be greater than 0", "UserServlet::access", null);
                    return;
                }
            } catch (NumberFormatException ex) {
                sendRest(resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                        "Query parameter 'page' must be a valid integer", "UserServlet::access", null);
                return;
            }
        }

        int perPage = 5;
        if (perPageParam != null && !perPageParam.trim().isEmpty()) {
            try {
                perPage = Integer.parseInt(perPageParam.trim());
                if (perPage < 1) {
                    sendRest(resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                            "Query parameter 'perPage' must be greater than 0", "UserServlet::access", null);
                    return;
                }
            } catch (NumberFormatException ex) {
                sendRest(resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.QueryParameterMalformed,
                        "Query parameter 'perPage' must be a valid integer", "UserServlet::access", null);
                return;
            }
        }

        if (userDao == null) {
            sendRest(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RestStatus.InternalServerError,
                    "UserDao is not available", "UserServlet::access", null);
            return;
        }

        try {
            int total = userDao.getUsersCount();
            List<UserAccessItem> users = userDao.getUsersAccess(page, perPage);
            RestPagination pagination = new RestPagination(page, perPage, total);
            sendRest(resp, HttpServletResponse.SC_OK, RestStatus.Ok, users, "UserServlet::access", pagination);
        } catch (SQLException ex) {
            sendRest(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RestStatus.DatabaseError,
                    ex.getMessage(), "UserServlet::access", null);
        } catch (Exception ex) {
            sendRest(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RestStatus.InternalServerError,
                    ex.getMessage(), "UserServlet::access", null);
        }
    }

    public void authenticate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            sendRest(resp, HttpServletResponse.SC_UNAUTHORIZED, RestStatus.HeaderRequired, "Missing 'Authorization' header");
            return;
        }

        if (!authHeader.startsWith("Basic ")) {
            sendRest(resp, HttpServletResponse.SC_UNAUTHORIZED, RestStatus.HeaderMalformed, "Authorization scheme must be 'Basic '");
            return;
        }

        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        byte[] credDecoded;
        try {
            credDecoded = Base64.getDecoder().decode(base64Credentials);
        } catch (IllegalArgumentException ex) {
            sendRest(resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.HeaderMalformed, "Invalid Base64 encoding in 'Authorization' header");
            return;
        }

        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);

        if (values.length != 2) {
            sendRest(resp, HttpServletResponse.SC_BAD_REQUEST, RestStatus.HeaderMalformed, "Credentials must be in format 'login:password'");
            return;
        }

        String login = values[0].trim();
        String password = values[1];

        if (login.isEmpty() || password.isEmpty()) {
            sendRest(resp, HttpServletResponse.SC_UNAUTHORIZED, RestStatus.Unauthorized, "Login and password must not be empty");
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", login);
        payload.put("name", login);
        payload.put("email", login.contains("@") ? login : login + "@example.com");
        payload.put("dob", "2026-01-01");
        payload.put("ava", "/img/user.jpg");

        String headerJson = "{\"alg\":\"none\",\"typ\":\"JWT\"}";
        String payloadJson = gson.toJson(payload);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8))
                + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8))
                + ".signature";

        sendRest(resp, HttpServletResponse.SC_OK, RestStatus.Ok, token);
    }

    private void sendRest(HttpServletResponse resp, int httpStatusCode, RestStatus status, Object data, String service, RestPagination pagination) throws IOException {
        resp.setStatus(httpStatusCode);
        RestResponse restResponse = new RestResponse(status, data);
        if (restResponse.getMeta() != null) {
            restResponse.getMeta().setService(service);
        }
        if (pagination != null) {
            restResponse.setPagination(pagination);
        }
        resp.getWriter().print(gson.toJson(restResponse));
    }

    private void sendRest(HttpServletResponse resp, int httpStatusCode, RestStatus status, Object data) throws IOException {
        sendRest(resp, httpStatusCode, status, data, "UserServlet::authenticate", null);
    }
}

