package step.learning.java231web.servlets;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import step.learning.java231web.dao.UserDao;
import step.learning.java231web.rest.RestResponse;
import step.learning.java231web.rest.RestStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserServletTest {

    private UserServlet userServlet;
    private Gson gson;

    @BeforeEach
    public void setUp() {
        UserDao dummyUserDao = new UserDao(null);
        userServlet = new UserServlet(dummyUserDao);
        gson = new Gson();
    }

    @Test
    public void testAuthenticateSuccessReturnsRestResponseOk() throws IOException {
        String login = "tester";
        String password = "secretPassword123";
        String encoded = Base64.getEncoder().encodeToString((login + ":" + password).getBytes(StandardCharsets.UTF_8));

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + encoded);

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(headers);
        HttpServletResponse response = createMockResponse(responseCtx);

        userServlet.authenticate(request, response);

        assertEquals(200, responseCtx.status);
        assertTrue(responseCtx.contentType.contains("application/json"));

        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertNotNull(restResponse.getStatus());
        assertTrue(restResponse.getStatus().isOk());
        assertEquals(200, restResponse.getStatus().getCode());
        assertEquals("OK", restResponse.getStatus().getMessage());

        assertNotNull(restResponse.getMeta());
        assertEquals("UserServlet::authenticate", restResponse.getMeta().getService());

        assertNotNull(restResponse.getData());
        String token = restResponse.getData().toString();
        assertEquals(3, token.split("\\.").length, "JWT token must have 3 parts separated by dots");
    }

    @Test
    public void testAuthenticateMissingHeaderReturnsRestResponseHeaderRequired() throws IOException {
        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(new HashMap<>());
        HttpServletResponse response = createMockResponse(responseCtx);

        userServlet.authenticate(request, response);

        assertEquals(401, responseCtx.status);
        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertFalse(restResponse.getStatus().isOk());
        assertEquals(RestStatus.HeaderRequired.getCode(), restResponse.getStatus().getCode());
    }

    @Test
    public void testAuthenticateInvalidSchemeReturnsRestResponseHeaderMalformed() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer some-token");

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(headers);
        HttpServletResponse response = createMockResponse(responseCtx);

        userServlet.authenticate(request, response);

        assertEquals(401, responseCtx.status);
        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertFalse(restResponse.getStatus().isOk());
        assertEquals(RestStatus.HeaderMalformed.getCode(), restResponse.getStatus().getCode());
    }

    @Test
    public void testAuthenticateInvalidBase64ReturnsRestResponseHeaderMalformed() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic ???not-base64???");

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(headers);
        HttpServletResponse response = createMockResponse(responseCtx);

        userServlet.authenticate(request, response);

        assertEquals(400, responseCtx.status);
        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertFalse(restResponse.getStatus().isOk());
        assertEquals(RestStatus.HeaderMalformed.getCode(), restResponse.getStatus().getCode());
    }

    @Test
    public void testAuthenticateCredentialsWithoutColonReturnsRestResponseHeaderMalformed() throws IOException {
        String encoded = Base64.getEncoder().encodeToString("loginWithoutColon".getBytes(StandardCharsets.UTF_8));
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + encoded);

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(headers);
        HttpServletResponse response = createMockResponse(responseCtx);

        userServlet.authenticate(request, response);

        assertEquals(400, responseCtx.status);
        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertFalse(restResponse.getStatus().isOk());
        assertEquals(RestStatus.HeaderMalformed.getCode(), restResponse.getStatus().getCode());
    }

    @Test
    public void testAuthenticateEmptyCredentialsReturnsRestResponseUnauthorized() throws IOException {
        String encoded = Base64.getEncoder().encodeToString(":".getBytes(StandardCharsets.UTF_8));
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + encoded);

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(headers);
        HttpServletResponse response = createMockResponse(responseCtx);

        userServlet.authenticate(request, response);

        assertEquals(401, responseCtx.status);
        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertFalse(restResponse.getStatus().isOk());
        assertEquals(RestStatus.Unauthorized.getCode(), restResponse.getStatus().getCode());
    }

    private static class TestResponseContext {
        int status;
        String contentType;
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        String getOutput() {
            printWriter.flush();
            return stringWriter.toString();
        }
    }

    private HttpServletRequest createMockRequest(Map<String, String> headers) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName())) {
                        return headers.get((String) args[0]);
                    }
                    return null;
                }
        );
    }

    private HttpServletResponse createMockResponse(TestResponseContext ctx) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName())) {
                        ctx.status = (Integer) args[0];
                        return null;
                    }
                    if ("setContentType".equals(method.getName())) {
                        ctx.contentType = (String) args[0];
                        return null;
                    }
                    if ("getWriter".equals(method.getName())) {
                        return ctx.printWriter;
                    }
                    return null;
                }
        );
    }
}
