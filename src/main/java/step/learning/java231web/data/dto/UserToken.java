package step.learning.java231web.data.dto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserToken {
    private String id;
    private String userId;
    private String login;
    private String name;
    private String salt;
    private String dk;
    private String role;

    public UserToken() {
    }

    public UserToken(String id, String userId, String login, String salt, String dk, String role) {
        this(id, userId, login, salt, dk, role, null);
    }

    public UserToken(String id, String userId, String login, String salt, String dk, String role, String name) {
        this.id = id;
        this.userId = userId;
        this.login = login;
        this.salt = salt;
        this.dk = dk;
        this.role = role;
        this.name = name != null && !name.trim().isEmpty() ? name : login;
    }

    public static UserToken fromResultSet(ResultSet rs) {
        try {
            UserToken token = new UserToken();
            token.setId(rs.getString("id"));
            try {
                token.setUserId(rs.getString("userId"));
            } catch (SQLException ignored) {
                token.setUserId(rs.getString("user_id"));
            }
            token.setLogin(rs.getString("login"));
            token.setSalt(rs.getString("salt"));
            token.setDk(rs.getString("dk"));
            token.setRole(rs.getString("role"));
            try {
                token.setName(rs.getString("name"));
            } catch (SQLException ignored) {}
            return token;
        } catch (SQLException ex) {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name != null && !name.trim().isEmpty() ? name : login;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getDk() {
        return dk;
    }

    public void setDk(String dk) {
        this.dk = dk;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        String displayName = (name != null && !name.trim().isEmpty()) ? name : (login != null ? login : "Anonymous");
        return (userId != null ? userId : "") + " * " + displayName;
    }
}
