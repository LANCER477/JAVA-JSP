package step.learning.java231web.servlets;

import com.google.gson.Gson;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Робота з JSON. Бібліотека Gson
 * @author Lector
 */
// @WebServlet("/gson")
@Singleton
public class GsonServlet extends HttpServlet {
    private Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Data data = new Data()
                .setX(10)
                .setStr(null)
                .setB(true);
        // API режим - коли не переводимо до представлення, а 
        // формуємо дані на рівні контролера.
        resp.setContentType("application/json");
        gson.toJson(data, resp.getWriter());        
    }
    
    
    class Data {
        private int x;
        private String str;
        private boolean b;

        public int getX() {
            return x;
        }

        public Data setX(int x) {
            this.x = x;
            return this;
        }

        public String getStr() {
            return str;
        }

        public Data setStr(String str) {
            this.str = str;
            return this;
        }

        public boolean isB() {
            return b;
        }

        public Data setB(boolean b) {
            this.b = b;
            return this;
        }
        
    }
}
/*
Підключення додаткових бібліотек.
У Java бібліотеки класів мають розширення .jar (java archive)
і є аналогом .dll бібліотек у .NET
Додати бібліотеку до проєкту можна
- додаванням файлу .jar і долученням його до переліку компіляції
- додаванням посилання на файл (у репозиторії бібліотек) у pom.xml

JSON.
Для роботи з JSON рекомендується підключити спеціалізовані бібліотеки.
Наприклад, Gson від Google
*/