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
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import step.learning.java231web.dao.UserDao;
import step.learning.java231web.models.UserSignupFormModel;
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
        resp.setContentType("application/json; charset=UTF-8");

        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            String[] values = credentials.split(":", 2);

            if (values.length == 2) {
                String login = values[0];
                String password = values[1];

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

                Map<String, Object> authData = new LinkedHashMap<>();
                authData.put("token", token);
                authData.put("user", payload);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(gson.toJson(new RestResponse(RestStatus.Ok, token)));
                return;
            }
        }

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().print(gson.toJson(new RestResponse(RestStatus.Unauthorized, "Credentials required")));
    }
}
