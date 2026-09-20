<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String sqlQuery = (String) request.getAttribute("sqlQuery");
    String uuid = (String) request.getAttribute("uuid");
    Boolean dbSuccess = (Boolean) request.getAttribute("dbSuccess");
    String dbError = (String) request.getAttribute("dbError");
%>

<div class="card timestamp-card">
    <h3>База даних (MySQL) - Формування UUID</h3>
    <p>SQL-запит для формування UUID:</p>
    <p><code><%= sqlQuery != null ? sqlQuery : "SELECT UUID();" %></code></p>

    <% if (Boolean.TRUE.equals(dbSuccess)) { %>
        <p style="margin-top: 12px;">Результат виконання запиту:</p>
        <div style="margin-top: 8px;">
            <b>Згенерований UUID:</b>
            <span class="timestamp-value"><%= uuid %></span>
        </div>
        <p style="margin-top: 10px; color: #166534; font-size: 0.9em;">
            З'єднання з базою даних успішно встановлено.
        </p>
    <% } else { %>
        <div style="margin-top: 12px; color: #b91c1c;">
            <b>Помилка підключення до БД:</b>
            <p style="margin-top: 6px; font-family: monospace; font-size: 0.9em; background: #fee2e2; padding: 8px; border-radius: 4px;">
                <%= dbError != null ? dbError : "Не вдалося отримати дані" %>
            </p>
        </div>
    <% } %>
</div>

<div class="card">
    <h2>Параметри підключення</h2>
    <ul>
        <li>СУБД: <b>MySQL</b></li>
        <li>База даних: <code>java_231</code></li>
        <li>Користувач: <code>user_231j</code></li>
        <li>SQL запит: <code>SELECT UUID();</code></li>
        <li>Контроль проходження сервлету: <b><%= request.getAttribute("servlet") %></b></li>
    </ul>
</div>
