package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import step.learning.java231web.services.hash.IHashService;

/**
 *
 * @author samoylenko_d
 */
@Singleton
public class HomeServlet extends HttpServlet {
    private final IHashService _hashService;

    @Inject   // необхідна анотація для інжекційного конструктора
    public HomeServlet(IHashService hashService) {
        this._hashService = hashService;
    }
    /*
    Д.З. Впровадити у проєкт сервіс часу (timestamp),
    який буде формувати мітку часу в двох форматах:
    10 цифр з точністю до секунди
    13 - до мілісекунди
    Додати скріншоти
    */
    
    

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // закладаємо до req додатковий атрибут, який дасть можливість
        // контролю факту проходження запиту через сервлет
        req.setAttribute("servlet", "Home");
        req.setAttribute("hash", _hashService.hexDigest("123"));
        
        // передаємо управління на представлення (~return View("index.jsp"))
        req.getRequestDispatcher("index.jsp")
                .forward(req, resp);
    }
    
}
