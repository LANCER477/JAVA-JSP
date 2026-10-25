package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import step.learning.java231web.data.dao.TokenDao;
import step.learning.java231web.data.dto.UserToken;
import step.learning.java231web.services.hash.IHashService;
import step.learning.java231web.services.time.ITimeService;

/**
 * @author samoylenko_d
 */
@Singleton
public class HomeServlet extends HttpServlet {
    private final IHashService _hashService;
    private final ITimeService _timeService;
    private final TokenDao _tokenDao;

    @Inject
    public HomeServlet(IHashService hashService, ITimeService timeService, TokenDao tokenDao) {
        this._hashService = hashService;
        this._timeService = timeService;
        this._tokenDao = tokenDao;
    }

    public HomeServlet(IHashService hashService, ITimeService timeService) {
        this(hashService, timeService, null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("servlet", "Home");
        req.setAttribute("hash", _hashService != null ? _hashService.hexDigest("123") : "");
        req.setAttribute("timestamp10", _timeService != null ? _timeService.getTimestamp10() : "");
        req.setAttribute("timestamp13", _timeService != null ? _timeService.getTimestamp13() : "");

        // Д.З. Реалізувати можливість перевірки авторизації: до обробки домашньої сторінки додати параметр ?ua=...
        String ua = req.getParameter("ua");
        if (ua != null && !ua.trim().isEmpty()) {
            if (_tokenDao != null) {
                UserToken token = _tokenDao.getToken(ua.trim());
                if (token != null) {
                    req.setAttribute("auth", token);
                }
            } else if (TokenDao.HW_TOKEN_ID.equalsIgnoreCase(ua.trim()) || TokenDao.DEMO_TOKEN_ID.equalsIgnoreCase(ua.trim())) {
                req.setAttribute("auth", new UserToken(
                        ua.trim(),
                        TokenDao.DEMO_USER_ID,
                        TokenDao.DEMO_LOGIN,
                        TokenDao.DEMO_SALT,
                        TokenDao.DEMO_DK,
                        TokenDao.DEMO_ROLE,
                        TokenDao.DEMO_NAME
                ));
            }
        }

        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }
}
