package step.learning.java231web.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

/**
 * Робота з JSON. Бібліотека Gson
 * @author Lector
 */
// @WebServlet("/gson")
@Singleton
public class GsonServlet extends HttpServlet {
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fileContent;
        try ( BufferedReader rdr = new BufferedReader(
                new InputStreamReader(
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("sections.json")))
        ) {
            fileContent = rdr.lines().collect(java.util.stream.Collectors.joining("\n"));
        }
        catch(Exception ex) {
            fileContent = "\"Error\"";
        }
        resp.setContentType("application/json");
        resp.getWriter().print(fileContent);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String body = req.getReader().lines().collect(java.util.stream.Collectors.joining("\n"));
        Data data = gson.fromJson( body, Data.class );
        resp.setContentType("application/json");
        gson.toJson(data, resp.getWriter());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Data data = new Data()
                .setX(10)
                .setStr(null)
                .setCyr("Ґранд PUT")
                .setB(true)
                .setDate(new Date());
        resp.setContentType("application/json");
        gson.toJson(data, resp.getWriter());
    }



    class Data {
        private int x;
        private String str;
        private String cyr;
        private boolean b;
        private Date date;

        public Date getDate() {
            return date;
        }

        public Data setDate(Date date) {
            this.date = date;
            return this;
        }


        public String getCyr() {
            return cyr;
        }

        public Data setCyr(String cyr) {
            this.cyr = cyr;
            return this;
        }

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