package step.learning.java231web.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import step.learning.java231web.models.UserAccessItem;
import step.learning.java231web.models.UserSignupFormModel;
import step.learning.java231web.services.db.IDbService;

@Singleton
public class UserDao {
    private final IDbService dbService;
    private final Logger logger;
    private boolean isInstalled = false;

    @Inject
    public UserDao(IDbService dbService) {
        this.dbService = dbService;
        this.logger = Logger.getLogger(UserDao.class.getName());
    }

    public UserDao(IDbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger;
    }

    public synchronized void ensureInstalled() {
        if (isInstalled || dbService == null) {
            return;
        }
        try {
            install();
            isInstalled = true;
            seedIfEmpty();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Auto install or seed error", ex);
        }
    }

    public void install() throws SQLException {
        if (dbService == null) {
            return;
        }
        String sql = "CREATE TABLE IF NOT EXISTS user ("
                + "id CHAR(36) PRIMARY KEY, "
                + "name VARCHAR(128) NOT NULL, "
                + "email VARCHAR(128) NOT NULL, "
                + "login VARCHAR(64) NULL, "
                + "role VARCHAR(32) DEFAULT 'user', "
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "deleted_at DATETIME NULL"
                + ")";
        try (Statement statement = dbService.getConnection().createStatement()) {
            statement.executeUpdate(sql);
            try {
                statement.executeUpdate("ALTER TABLE user ADD COLUMN login VARCHAR(64) NULL");
            } catch (SQLException ignored) {}
            try {
                statement.executeUpdate("ALTER TABLE user ADD COLUMN role VARCHAR(32) DEFAULT 'user'");
            } catch (SQLException ignored) {}
            try {
                statement.executeUpdate("ALTER TABLE user ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP");
            } catch (SQLException ignored) {}
            try {
                statement.executeUpdate("ALTER TABLE user ADD COLUMN deleted_at DATETIME NULL");
            } catch (SQLException ignored) {}
        }
    }

    private void seedIfEmpty() {
        try {
            if (getUsersCount() == 0) {
                String insertSql = "INSERT INTO user (id, name, email, login, role) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement prep = dbService.getConnection().prepareStatement(insertSql)) {
                    String[][] initialUsers = {
                            {"Administrator", "admin@example.com", "admin", "admin"},
                            {"Moderator", "mod@example.com", "moderator", "moderator"},
                            {"Іван Петренко", "ivan@example.com", "ivan", "user"},
                            {"Олена Коваль", "olena@example.com", "olena", "user"},
                            {"Максим Шевченко", "maks@example.com", "maksim", "user"},
                            {"Гість Системи", "guest@example.com", "guest", "guest"}
                    };
                    for (String[] u : initialUsers) {
                        prep.setString(1, UUID.randomUUID().toString());
                        prep.setString(2, u[0]);
                        prep.setString(3, u[1]);
                        prep.setString(4, u[2]);
                        prep.setString(5, u[3]);
                        prep.executeUpdate();
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "Skipping seed: " + ex.getMessage());
        }
    }

    public int getUsersCount() throws SQLException {
        if (dbService == null) {
            return 0;
        }
        ensureInstalled();
        String sql = "SELECT COUNT(*) FROM user WHERE deleted_at IS NULL";
        try (Statement statement = dbService.getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<UserAccessItem> getUsersAccess(int page, int perPage) throws SQLException {
        if (dbService == null) {
            return Collections.emptyList();
        }
        ensureInstalled();
        if (page < 1) {
            page = 1;
        }
        if (perPage < 1) {
            perPage = 5;
        }
        int offset = (page - 1) * perPage;

        String sql = "SELECT id, COALESCE(login, name) AS login, COALESCE(role, 'user') AS role, name, email "
                + "FROM user WHERE deleted_at IS NULL "
                + "ORDER BY created_at DESC "
                + "LIMIT ? OFFSET ?";

        List<UserAccessItem> list = new ArrayList<>();
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setInt(1, perPage);
            prep.setInt(2, offset);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    list.add(new UserAccessItem(
                            rs.getString("id"),
                            rs.getString("login"),
                            rs.getString("role"),
                            rs.getString("name"),
                            rs.getString("email")
                    ));
                }
            }
        }
        return list;
    }

    public boolean isLoginAvailable(String login) throws SQLException {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }
        String trimmed = login.trim();
        String sql = "SELECT COUNT(*) FROM user WHERE (email = ? OR name = ?) AND (deleted_at IS NULL)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, trimmed);
            prep.setString(2, trimmed);
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    public boolean isEmailAvailable(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String trimmed = email.trim();
        String sql = "SELECT COUNT(*) FROM user WHERE email = ? AND (deleted_at IS NULL)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, trimmed);
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    public void signupUser(UserSignupFormModel formModel) throws SQLException {
        if (formModel == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        formModel.validate();

        String checkLogin = formModel.getLogin() != null && !formModel.getLogin().trim().isEmpty()
                ? formModel.getLogin().trim()
                : formModel.getName().trim();

        if (!isLoginAvailable(checkLogin)) {
            throw new IllegalArgumentException("Логін '" + checkLogin + "' вже зайнятий");
        }

        if (!isEmailAvailable(formModel.getEmail())) {
            throw new IllegalArgumentException("Email '" + formModel.getEmail() + "' вже зайнятий");
        }

        String sql = "INSERT INTO user(id, name, email) VALUES (?, ?, ?)";
        String userId = UUID.randomUUID().toString();
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, userId);
            prep.setString(2, formModel.getName());
            prep.setString(3, formModel.getEmail());
            prep.executeUpdate();
        }
        catch (SQLException ex) {
            logger.log(Level.WARNING, sql, ex);
            throw ex;
        }
    }
}
