package step.learning.java231web.models;

import java.util.Objects;

public class UserAccessItem {
    private String userId;
    private String login;
    private String role;
    private String name;
    private String email;

    public UserAccessItem() {
    }

    public UserAccessItem(String userId, String login, String role, String name, String email) {
        this.userId = userId;
        this.login = login;
        this.role = role;
        this.name = name;
        this.email = email;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccessItem that = (UserAccessItem) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(login, that.login) &&
                Objects.equals(role, that.role) &&
                Objects.equals(name, that.name) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, login, role, name, email);
    }

    @Override
    public String toString() {
        return "UserAccessItem{" +
                "userId='" + userId + '\'' +
                ", login='" + login + '\'' +
                ", role='" + role + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
