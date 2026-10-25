package step.learning.java231web.data.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import step.learning.java231web.data.dto.UserToken;
import step.learning.java231web.services.db.IDbService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class TokenDao {
    public static final String DEMO_TOKEN_ID = "024ad494-bafd-11f1-9d1a-84ba59d7b864";
    public static final String HW_TOKEN_ID = "6aa5bcec-8f16-4d15-95ba-b67b9d63bdff";
    public static final String DEMO_USER_ID = "ee3822a2-6504-41d8-95d5-544bc625961e";
    public static final String DEMO_LOGIN = "user6";
    public static final String DEMO_NAME = "User 6";
    public static final String DEMO_SALT = "7ff8e35c22d04efa";
    public static final String DEMO_DK = "66bd532cf48ca2ad32ace9d900dc0d82";
    public static final String DEMO_ROLE = "guest";

    private final IDbService dbService;
    private final Logger logger;
    private boolean isInstalled = false;

    @Inject
    public TokenDao(IDbService dbService) {
        this.dbService = dbService;
        this.logger = Logger.getLogger(TokenDao.class.getName());
    }

    public TokenDao(IDbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger != null ? logger : Logger.getLogger(TokenDao.class.getName());
    }

    public synchronized boolean ensureInstalled() {
        if (isInstalled) {
            return true;
        }
        if (dbService == null) {
            return false;
        }
        try {
            boolean installed = install();
            seedDemoData();
            isInstalled = installed;
            return isInstalled;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "TokenDao auto-install failed", ex);
            return false;
        }
    }

    public boolean install() {
        if (dbService == null) {
            return false;
        }
        try {
            Connection conn = dbService.getConnection();
            if (conn == null) {
                return false;
            }

            try (Statement st = conn.createStatement()) {
                try {
                    st.executeUpdate("ALTER TABLE user ADD COLUMN salt VARCHAR(64) NULL");
                } catch (SQLException ignored) {
                }
                try {
                    st.executeUpdate("ALTER TABLE user ADD COLUMN dk VARCHAR(64) NULL");
                } catch (SQLException ignored) {
                }

                String sql = "CREATE TABLE IF NOT EXISTS tokens ("
                        + "id CHAR(36) PRIMARY KEY, "
                        + "user_id CHAR(36) NOT NULL, "
                        + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
                        + "expires_at DATETIME NULL"
                        + ")";
                st.executeUpdate(sql);
                return true;
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error creating tokens table", ex);
            return false;
        }
    }

    private void seedDemoData() {
        if (dbService == null) {
            return;
        }
        try {
            Connection conn = dbService.getConnection();
            if (conn == null) {
                return;
            }

            // Ensure demo user user6 exists in user table
            String checkUserSql = "SELECT COUNT(*) FROM user WHERE id = ? OR login = ?";
            boolean userExists = false;
            try (PreparedStatement prep = conn.prepareStatement(checkUserSql)) {
                prep.setString(1, DEMO_USER_ID);
                prep.setString(2, DEMO_LOGIN);
                try (ResultSet rs = prep.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        userExists = true;
                    }
                }
            }

            if (!userExists) {
                String insertUserSql = "INSERT INTO user (id, name, email, login, salt, dk, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement prep = conn.prepareStatement(insertUserSql)) {
                    prep.setString(1, DEMO_USER_ID);
                    prep.setString(2, DEMO_NAME);
                    prep.setString(3, "user6@example.com");
                    prep.setString(4, DEMO_LOGIN);
                    prep.setString(5, DEMO_SALT);
                    prep.setString(6, DEMO_DK);
                    prep.setString(7, DEMO_ROLE);
                    prep.executeUpdate();
                }
            } else {
                String updateSql = "UPDATE user SET name = COALESCE(name, ?), salt = ?, dk = ?, role = ? WHERE id = ? OR login = ?";
                try (PreparedStatement prep = conn.prepareStatement(updateSql)) {
                    prep.setString(1, DEMO_NAME);
                    prep.setString(2, DEMO_SALT);
                    prep.setString(3, DEMO_DK);
                    prep.setString(4, DEMO_ROLE);
                    prep.setString(5, DEMO_USER_ID);
                    prep.setString(6, DEMO_LOGIN);
                    prep.executeUpdate();
                }
            }

            // Seed demo token (024ad494...) and homework token (6aa5bcec...)
            insertTokenIfMissing(conn, DEMO_TOKEN_ID, DEMO_USER_ID);
            insertTokenIfMissing(conn, HW_TOKEN_ID, DEMO_USER_ID);
        } catch (Exception ex) {
            logger.log(Level.INFO, "Skipping token demo seed: " + ex.getMessage());
        }
    }

    private void insertTokenIfMissing(Connection conn, String tokenId, String userId) {
        String checkTokenSql = "SELECT COUNT(*) FROM tokens WHERE id = ?";
        try (PreparedStatement prep = conn.prepareStatement(checkTokenSql)) {
            prep.setString(1, tokenId);
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        } catch (SQLException ignored) {
            return;
        }

        String insertTokenSql = "INSERT INTO tokens (id, user_id) VALUES (?, ?)";
        try (PreparedStatement prep = conn.prepareStatement(insertTokenSql)) {
            prep.setString(1, tokenId);
            prep.setString(2, userId);
            prep.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public UserToken getToken(String tokenId) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            return null;
        }
        String cleanId = tokenId.trim();

        if (dbService != null) {
            ensureInstalled();
            String sql = "SELECT t.id, t.user_id, u.login, u.salt, u.dk, u.role, u.name "
                    + "FROM tokens t "
                    + "LEFT JOIN user u ON t.user_id = u.id "
                    + "WHERE t.id = ?";
            try {
                Connection conn = dbService.getConnection();
                if (conn != null) {
                    try (PreparedStatement prep = conn.prepareStatement(sql)) {
                        prep.setString(1, cleanId);
                        try (ResultSet rs = prep.executeQuery()) {
                            if (rs.next()) {
                                String id = rs.getString(1);
                                String userId = rs.getString(2);
                                String login = rs.getString(3);
                                String salt = rs.getString(4);
                                String dk = rs.getString(5);
                                String role = rs.getString(6);
                                String name = rs.getString(7);
                                return new UserToken(id, userId, login, salt, dk, role, name != null && !name.trim().isEmpty() ? name : login);
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "Error finding token " + cleanId, ex);
            }
        }

        // Demo fallback for instructor's test tokens
        if (DEMO_TOKEN_ID.equalsIgnoreCase(cleanId) || HW_TOKEN_ID.equalsIgnoreCase(cleanId)) {
            return getDemoToken(cleanId);
        }

        return null;
    }

    public UserToken getDemoToken(String tokenId) {
        String id = tokenId != null ? tokenId : DEMO_TOKEN_ID;
        return new UserToken(id, DEMO_USER_ID, DEMO_LOGIN, DEMO_SALT, DEMO_DK, DEMO_ROLE, DEMO_NAME);
    }
}
