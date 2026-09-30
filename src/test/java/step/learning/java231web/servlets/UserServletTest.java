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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import step.learning.java231web.models.UserAccessItem;

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

    @Test
    public void testUserAccessDefaultPaginationReturnsOkAndPagination() throws Exception {
        List<UserAccessItem> mockList = new ArrayList<>();
        mockList.add(new UserAccessItem("u1", "admin", "admin", "Admin", "admin@test.com"));
        mockList.add(new UserAccessItem("u2", "user2", "user", "User 2", "user2@test.com"));

        MockUserDao mockDao = new MockUserDao(10, mockList);
        UserServlet servlet = new UserServlet(mockDao);

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(Collections.emptyMap(), Collections.emptyMap(), "/access", "/user");
        HttpServletResponse response = createMockResponse(responseCtx);

        servlet.access(request, response);

        assertEquals(200, responseCtx.status);
        assertTrue(responseCtx.contentType.contains("application/json"));

        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertTrue(restResponse.getStatus().isOk());
        assertEquals(200, restResponse.getStatus().getCode());
        assertEquals("OK", restResponse.getStatus().getMessage());

        assertNotNull(restResponse.getMeta());
        assertEquals("UserServlet::access", restResponse.getMeta().getService());

        assertNotNull(restResponse.getPagination());
        assertEquals(1, restResponse.getPagination().getPage());
        assertEquals(5, restResponse.getPagination().getPerPage());
        assertEquals(10, restResponse.getPagination().getTotal());
        assertNotNull(restResponse.getData());
    }

    @Test
    public void testUserAccessCustomPagination() throws Exception {
        List<UserAccessItem> mockList = new ArrayList<>();
        mockList.add(new UserAccessItem("u3", "mod1", "moderator", "Mod", "mod@test.com"));

        MockUserDao mockDao = new MockUserDao(25, mockList);
        UserServlet servlet = new UserServlet(mockDao);

        Map<String, String> params = new HashMap<>();
        params.put("page", "3");
        params.put("perPage", "7");

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(Collections.emptyMap(), params, "/access", "/user");
        HttpServletResponse response = createMockResponse(responseCtx);

        servlet.access(request, response);

        assertEquals(200, responseCtx.status);
        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertNotNull(restResponse.getPagination());
        assertEquals(3, restResponse.getPagination().getPage());
        assertEquals(7, restResponse.getPagination().getPerPage());
        assertEquals(25, restResponse.getPagination().getTotal());
    }

    @Test
    public void testUserAccessInvalidPageReturnsQueryParameterMalformed() throws Exception {
        MockUserDao mockDao = new MockUserDao(5, Collections.emptyList());
        UserServlet servlet = new UserServlet(mockDao);

        Map<String, String> params = new HashMap<>();
        params.put("page", "0");

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(Collections.emptyMap(), params, "/access", "/user");
        HttpServletResponse response = createMockResponse(responseCtx);

        servlet.access(request, response);

        assertEquals(400, responseCtx.status);
        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertFalse(restResponse.getStatus().isOk());
        assertEquals(RestStatus.QueryParameterMalformed.getCode(), restResponse.getStatus().getCode());
    }

    @Test
    public void testUserAccessInvalidPerPageReturnsQueryParameterMalformed() throws Exception {
        MockUserDao mockDao = new MockUserDao(5, Collections.emptyList());
        UserServlet servlet = new UserServlet(mockDao);

        Map<String, String> params = new HashMap<>();
        params.put("perPage", "invalid_num");

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(Collections.emptyMap(), params, "/access", "/user");
        HttpServletResponse response = createMockResponse(responseCtx);

        servlet.access(request, response);

        assertEquals(400, responseCtx.status);
        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertFalse(restResponse.getStatus().isOk());
        assertEquals(RestStatus.QueryParameterMalformed.getCode(), restResponse.getStatus().getCode());
    }

    @Test
    public void testUserAccessDatabaseExceptionReturnsDatabaseError() throws Exception {
        MockUserDao mockDao = new MockUserDao(true);
        UserServlet servlet = new UserServlet(mockDao);

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(Collections.emptyMap(), Collections.emptyMap(), "/access", "/user");
        HttpServletResponse response = createMockResponse(responseCtx);

        servlet.access(request, response);

        assertEquals(500, responseCtx.status);
        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertFalse(restResponse.getStatus().isOk());
        assertEquals(RestStatus.DatabaseError.getCode(), restResponse.getStatus().getCode());
    }

    @Test
    public void testDoGetRoutesToAccessWhenPathInfoIsAccess() throws Exception {
        List<UserAccessItem> mockList = new ArrayList<>();
        mockList.add(new UserAccessItem("u1", "admin", "admin", "Admin", "admin@test.com"));

        MockUserDao mockDao = new MockUserDao(1, mockList);
        UserServlet servlet = new UserServlet(mockDao);

        TestResponseContext responseCtx = new TestResponseContext();
        HttpServletRequest request = createMockRequest(Collections.emptyMap(), Collections.emptyMap(), "/access", "/user");
        HttpServletResponse response = createMockResponse(responseCtx);

        servlet.doGet(request, response);

        assertEquals(200, responseCtx.status);
        RestResponse restResponse = gson.fromJson(responseCtx.getOutput(), RestResponse.class);
        assertNotNull(restResponse);
        assertEquals("UserServlet::access", restResponse.getMeta().getService());
    }

    private static class MockUserDao extends UserDao {
        private final int totalCount;
        private final List<UserAccessItem> items;
        private final boolean throwSqlException;

        public MockUserDao(int totalCount, List<UserAccessItem> items) {
            super(null);
            this.totalCount = totalCount;
            this.items = items;
            this.throwSqlException = false;
        }

        public MockUserDao(boolean throwSqlException) {
            super(null);
            this.totalCount = 0;
            this.items = Collections.emptyList();
            this.throwSqlException = throwSqlException;
        }

        @Override
        public int getUsersCount() throws SQLException {
            if (throwSqlException) throw new SQLException("Simulated database failure");
            return totalCount;
        }

        @Override
        public List<UserAccessItem> getUsersAccess(int page, int perPage) throws SQLException {
            if (throwSqlException) throw new SQLException("Simulated database failure");
            return items;
        }
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

    private HttpServletRequest createMockRequest(Map<String, String> headers, Map<String, String> params, String pathInfo, String servletPath) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName())) {
                        return headers != null ? headers.get((String) args[0]) : null;
                    }
                    if ("getParameter".equals(method.getName())) {
                        return params != null ? params.get((String) args[0]) : null;
                    }
                    if ("getPathInfo".equals(method.getName())) {
                        return pathInfo;
                    }
                    if ("getServletPath".equals(method.getName())) {
                        return servletPath != null ? servletPath : "/user";
                    }
                    return null;
                }
        );
    }

    private HttpServletRequest createMockRequest(Map<String, String> headers) {
        return createMockRequest(headers, Collections.emptyMap(), null, "/user");
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
