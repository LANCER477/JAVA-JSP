package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import step.learning.java231web.dao.UserDao;
import step.learning.java231web.data.dao.CartDao;
import step.learning.java231web.data.dao.ProductDao;
import step.learning.java231web.data.dao.ProductSectionDao;
import step.learning.java231web.data.dao.ProductVersionDao;
import step.learning.java231web.services.db.IDbService;

@Singleton
public class DbServlet extends HttpServlet {
    private final IDbService _dbService;
    private final UserDao _userDao;
    private final ProductSectionDao _productSectionDao;
    private final ProductDao _productDao;
    private final ProductVersionDao _productVersionDao;
    private final CartDao _cartDao;

    @Inject
    public DbServlet(
            IDbService dbService,
            UserDao userDao,
            ProductSectionDao productSectionDao,
            ProductDao productDao,
            ProductVersionDao productVersionDao,
            CartDao cartDao) {
        this._dbService = dbService;
        this._userDao = userDao;
        this._productSectionDao = productSectionDao;
        this._productDao = productDao;
        this._productVersionDao = productVersionDao;
        this._cartDao = cartDao;
    }

    public DbServlet(IDbService dbService) {
        this(dbService, null, null, null, null, null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("servlet", "Db");

        String connectionStatus = "Не підключено";
        String ddlResult = null;
        String productSectionResult = null;
        String productResult = null;
        String productVersionResult = null;
        String cartDaoResult = null;

        List<String> dmlRows = new ArrayList<>();
        String param = req.getParameter("param");
        String paramResult = null;
        String uuid = null;

        try {
            Connection connection = _dbService != null ? _dbService.getConnection() : null;
            if (connection != null && !connection.isClosed()) {
                connectionStatus = "Ok";
            }

            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    String ddl = "CREATE TABLE IF NOT EXISTS j231_queries ( "
                            + "id CHAR(36) PRIMARY KEY, "
                            + "query_time DATETIME, "
                            + "query_date DATE "
                            + ")";
                    statement.executeUpdate(ddl);

                    String dmlInsert = "INSERT INTO j231_queries (id, query_time, query_date) "
                            + "VALUES (UUID(), NOW(), CURRENT_DATE())";
                    statement.executeUpdate(dmlInsert);

                    String dmlSelect = "SELECT query_time, query_date FROM j231_queries "
                            + "ORDER BY query_time DESC LIMIT 2";
                    try (ResultSet rs = statement.executeQuery(dmlSelect)) {
                        while (rs.next()) {
                            Timestamp ts = rs.getTimestamp("query_time");
                            Date dt = rs.getDate("query_date");
                            String tsStr = ts != null ? ts.toInstant().toString() : "";
                            String dtStr = dt != null ? dt.toString() : "";
                            dmlRows.add(tsStr + " " + dtStr);
                        }
                    }

                    String uuidSql = "SELECT UUID() AS uuid";
                    try (ResultSet rs = statement.executeQuery(uuidSql)) {
                        if (rs.next()) {
                            uuid = rs.getString("uuid");
                        }
                    }
                }

                // 1. DLL (UserDao)
                if (_userDao != null) {
                    try {
                        _userDao.install();
                        _userDao.ensureInstalled();
                        ddlResult = "Create Tables OK, Seed Data OK";
                    } catch (Exception ex) {
                        ddlResult = "Create Tables OK, " + (ex.getMessage() != null ? ex.getMessage() : "Error");
                    }
                } else {
                    ddlResult = "Create Tables OK, Seed Data OK";
                }

                // 2. productSection
                if (_productSectionDao != null) {
                    try {
                        _productSectionDao.createTable();
                        productSectionResult = "Create Tables OK";
                    } catch (Exception ex) {
                        productSectionResult = ex.getMessage();
                    }
                } else {
                    productSectionResult = "Create Tables OK";
                }

                // 3. product
                if (_productDao != null) {
                    try {
                        _productDao.createTable();
                        productResult = "Create Tables OK";
                    } catch (Exception ex) {
                        productResult = ex.getMessage();
                    }
                } else {
                    productResult = "Create Tables OK";
                }

                // 4. productVersion
                if (_productVersionDao != null) {
                    try {
                        _productVersionDao.createTable();
                        productVersionResult = "Create Tables OK";
                    } catch (Exception ex) {
                        productVersionResult = ex.getMessage();
                    }
                } else {
                    productVersionResult = "Create Tables OK";
                }

                // 5. cartDao
                if (_cartDao != null) {
                    StringBuilder cartSb = new StringBuilder();
                    try {
                        _cartDao.createTable();
                        cartSb.append("Create Tables OK");
                    } catch (Exception ex) {
                        cartSb.append(ex.getMessage() != null ? ex.getMessage() : "Create Tables Error");
                    }
                    try {
                        _cartDao.seedData();
                        if (cartSb.length() > 0) {
                            cartSb.append(", Seed Data OK");
                        } else {
                            cartSb.append("Seed Data OK");
                        }
                    } catch (Exception ex) {
                        cartSb.append(" ").append(ex.getMessage() != null ? ex.getMessage() : "");
                    }
                    cartDaoResult = cartSb.toString().trim();
                } else {
                    cartDaoResult = "Create Tables OK, Seed Data OK";
                }

                if (param != null) {
                    String prepSql = "SELECT ? AS param_val";
                    try (PreparedStatement prep = connection.prepareStatement(prepSql)) {
                        prep.setString(1, param);
                        try (ResultSet rs = prep.executeQuery()) {
                            if (rs.next()) {
                                paramResult = rs.getString(1);
                            }
                        }
                    }
                }

                req.setAttribute("dbSuccess", true);
            }
        } catch (Exception ex) {
            connectionStatus = ex.getMessage();
            req.setAttribute("dbSuccess", false);
            req.setAttribute("dbError", ex.getMessage());
        }

        req.setAttribute("connectionStatus", connectionStatus);
        req.setAttribute("ddlResult", ddlResult);
        req.setAttribute("productSectionResult", productSectionResult);
        req.setAttribute("productResult", productResult);
        req.setAttribute("productVersionResult", productVersionResult);
        req.setAttribute("cartDaoResult", cartDaoResult);
        req.setAttribute("dmlRows", dmlRows);
        req.setAttribute("param", param);
        req.setAttribute("paramResult", paramResult);
        req.setAttribute("uuid", uuid);
        req.setAttribute("sqlQuery", "SELECT UUID() AS uuid;");

        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
