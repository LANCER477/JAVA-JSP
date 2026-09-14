package step.learning.java231web.servlets;

import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Сервлет-контролер сторінки "~/about"
 * @author samoylenko_d
 */
// @WebServlet("/about")
@Singleton
public class AboutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("servlet", "About");
        
        req.getRequestDispatcher("index.jsp")
                .forward(req, resp);
    }
    
}
