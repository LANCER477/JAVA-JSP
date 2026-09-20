package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import step.learning.java231web.services.db.IDbService;

@Singleton
public class DbServlet extends HttpServlet {
    private final IDbService _dbService;

    @Inject
    public DbServlet(IDbService dbService) {
        this._dbService = dbService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("servlet", "Db");
        String sqlQuery = "SELECT UUID() AS uuid";
        req.setAttribute("sqlQuery", sqlQuery);

        try {
            Connection connection = _dbService.getConnection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sqlQuery)) {
                if (resultSet.next()) {
                    String uuid = resultSet.getString("uuid");
                    req.setAttribute("uuid", uuid);
                    req.setAttribute("dbSuccess", true);
                }
            }
        } catch (Exception ex) {
            req.setAttribute("dbSuccess", false);
            req.setAttribute("dbError", ex.getMessage());
        }

        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }
}
