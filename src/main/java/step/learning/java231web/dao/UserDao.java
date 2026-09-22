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
import step.learning.java231web.models.UserSignupFormModel;
import step.learning.java231web.services.db.IDbService;

@Singleton
public class UserDao {
    private final IDbService dbService;
    private final Logger logger;

    @Inject
    public UserDao(IDbService dbService) {
        this.dbService = dbService;
        this.logger = Logger.getLogger(UserDao.class.getName());
    }

    public UserDao(IDbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger;
    }

    public void install() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS user ("
                + "id CHAR(36) PRIMARY KEY, "
                + "name VARCHAR(128) NOT NULL, "
                + "email VARCHAR(128) NOT NULL, "
                + "login VARCHAR(64) NULL, "
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "deleted_at DATETIME NULL"
                + ")";
        try (Statement statement = dbService.getConnection().createStatement()) {
            statement.executeUpdate(sql);
        }
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
