package step.learning.java231web.servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import step.learning.java231web.data.dao.TokenDao;
import step.learning.java231web.data.dto.UserToken;
import step.learning.java231web.services.hash.Md5HashService;
import step.learning.java231web.services.time.SystemTimeService;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HomeServletTest {

    private static class TestContext {
        final Map<String, Object> attributes = new HashMap<>();
        String forwardedPath = null;
    }

    private HttpServletRequest createMockRequest(String uaParam, TestContext ctx) {
        RequestDispatcher mockDispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getParameter".equals(name) && args != null && args.length > 0) {
                        if ("ua".equals(args[0])) {
                            return uaParam;
                        }
                        return null;
                    }
                    if ("setAttribute".equals(name) && args != null && args.length == 2) {
                        ctx.attributes.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getAttribute".equals(name) && args != null && args.length == 1) {
                        return ctx.attributes.get(args[0]);
                    }
                    if ("getRequestDispatcher".equals(name) && args != null && args.length == 1) {
                        ctx.forwardedPath = (String) args[0];
                        return mockDispatcher;
                    }
                    return null;
                }
        );
    }

    private HttpServletResponse createMockResponse() {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> null
        );
    }

    @Test
    public void testDoGetWithoutUaParameterAuthIsNull() throws Exception {
        TokenDao tokenDao = new TokenDao(null);
        HomeServlet servlet = new HomeServlet(new Md5HashService(), new SystemTimeService(), tokenDao);

        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest(null, ctx);
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertEquals("Home", ctx.attributes.get("servlet"));
        assertNull(ctx.attributes.get("auth"));
        assertEquals("index.jsp", ctx.forwardedPath);
    }

    @Test
    public void testDoGetWithValidHomeworkUaParameterSetsAuthReportWithIdAndName() throws Exception {
        TokenDao tokenDao = new TokenDao(null);
        HomeServlet servlet = new HomeServlet(new Md5HashService(), new SystemTimeService(), tokenDao);

        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest("6aa5bcec-8f16-4d15-95ba-b67b9d63bdff", ctx);
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertNotNull(ctx.attributes.get("auth"));
        UserToken token = (UserToken) ctx.attributes.get("auth");

        assertEquals(TokenDao.DEMO_USER_ID, token.getUserId());
        assertNotNull(token.getName());
        assertEquals("User 6", token.getName());

        // Report contains user identifier * and user name
        String report = token.toString();
        assertTrue(report.contains(TokenDao.DEMO_USER_ID), "Report must contain user id");
        assertTrue(report.contains("*"), "Report must contain '*' separator");
        assertTrue(report.contains("User 6"), "Report must contain user name");
    }

    @Test
    public void testDoGetWithInvalidUaParameterAuthIsNull() throws Exception {
        TokenDao tokenDao = new TokenDao(null);
        HomeServlet servlet = new HomeServlet(new Md5HashService(), new SystemTimeService(), tokenDao);

        TestContext ctx = new TestContext();
        HttpServletRequest request = createMockRequest("invalid-uuid-token", ctx);
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertNull(ctx.attributes.get("auth"), "Auth must be null for invalid token");
    }
}
