package step.learning.java231web.filters;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import step.learning.java231web.data.dao.TokenDao;
import step.learning.java231web.data.dto.UserToken;

@Singleton
public class AuthFilter implements Filter {
    private final TokenDao tokenDao;

    @Inject
    public AuthFilter(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    public AuthFilter() {
        this.tokenDao = null;
    }

    @Override
    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sr;
        String authHeader = req.getHeader("Authorization");

        UserToken userToken = null;
        if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
            String tokenId = authHeader.substring(7).trim();
            if (tokenDao != null) {
                userToken = tokenDao.getToken(tokenId);
            }
            if (userToken == null && TokenDao.DEMO_TOKEN_ID.equalsIgnoreCase(tokenId)) {
                userToken = new UserToken(
                        TokenDao.DEMO_TOKEN_ID,
                        TokenDao.DEMO_USER_ID,
                        TokenDao.DEMO_LOGIN,
                        TokenDao.DEMO_SALT,
                        TokenDao.DEMO_DK,
                        TokenDao.DEMO_ROLE
                );
            }
        }

        sr.setAttribute("auth", userToken);
        fc.doFilter(sr, sr1);
    }
}
