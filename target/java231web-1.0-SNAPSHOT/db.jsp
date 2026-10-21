<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String connectionStatus = (String) request.getAttribute("connectionStatus");
    String ddlResult = (String) request.getAttribute("ddlResult");
    String productSectionResult = (String) request.getAttribute("productSectionResult");
    String productResult = (String) request.getAttribute("productResult");
    String productVersionResult = (String) request.getAttribute("productVersionResult");
    String cartDaoResult = (String) request.getAttribute("cartDaoResult");
    List<String> dmlRows = (List<String>) request.getAttribute("dmlRows");
    String param = (String) request.getAttribute("param");
    String paramResult = (String) request.getAttribute("paramResult");
    String sqlQuery = (String) request.getAttribute("sqlQuery");
    String uuid = (String) request.getAttribute("uuid");
    Boolean dbSuccess = (Boolean) request.getAttribute("dbSuccess");
    String dbError = (String) request.getAttribute("dbError");
%>

<div class="card">
    <h2>Робота з базами даних</h2>
    <p>JDBC - набір інструментів для взаємодії з СУБД (аналог ADO.NET). Послідовність роботи:</p>
    <ul>
        <li>Підключення до СУБД/БД. Основа - java.sql.Connection: <b><%= connectionStatus != null ? connectionStatus : "Ok" %></b></li>
        <li>Виконання запитів. Розрізняються запити з результатами та без них, а також параметричні (підготовлені) запити.</li>
    </ul>

    <p style="line-height: 1.6; margin: 12px 0;">
        <% if (ddlResult != null) { %>DLL: <%= ddlResult %><br/><% } %>
        <% if (productSectionResult != null) { %>productSection: <%= productSectionResult %><br/><% } %>
        <% if (productResult != null) { %>product: <%= productResult %><br/><% } %>
        <% if (productVersionResult != null) { %>productVersion: <%= productVersionResult %><br/><% } %>
        <% if (cartDaoResult != null) { %>cartDao: <%= cartDaoResult %><% } %>
    </p>

    <% if (dmlRows != null && !dmlRows.isEmpty()) { %>
        <p><b>DML</b> 
        <% for (int i = 0; i < dmlRows.size(); i++) { %>
            <%= (i > 0 ? "<br/>" : "") + dmlRows.get(i) %>
        <% } %>
        </p>
    <% } %>

    <h3>Параметричний запит:</h3>
    <form method="get" action="db" style="display: flex; gap: 8px; align-items: center; margin-bottom: 8px;">
        <input type="text" name="param" value="<%= param != null ? param : "" %>" style="padding: 6px 10px; border: 1px solid #ccc; border-radius: 4px;" />
        <button type="submit" style="padding: 6px 14px; cursor: pointer;">Hello</button>
    </form>
    <p><code><%= paramResult %></code></p>

    <p style="margin-top: 14px;">Random UUID: <%= uuid != null ? uuid : "" %></p>
</div>

<div class="card timestamp-card" style="margin-top: 16px;">
    <h3>Робота з БД (MySQL) - генерування UUID (с.р.)</h3>
    <p>SQL-запит для генерування UUID:</p>
    <p><code><%= sqlQuery != null ? sqlQuery : "SELECT UUID();" %></code></p>

    <% if (Boolean.TRUE.equals(dbSuccess)) { %>
        <p style="margin-top: 12px;">Результат виконання запиту:</p>
        <div style="margin-top: 8px;">
            <b>Згенерований UUID:</b>
            <span class="timestamp-value"><%= uuid != null ? uuid : "" %></span>
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

<div class="card" style="margin-top: 16px;">
    <h2>Параметри підключення</h2>
    <ul>
        <li>СУБД: <b>MySQL</b></li>
        <li>База даних: <code>java_231</code></li>
        <li>Користувач: <code>user_231j</code></li>
        <li>SQL запит: <code>SELECT UUID();</code></li>
        <li>Контролер вихідного сервлету: <b><%= request.getAttribute("servlet") %></b></li>
    </ul>
</div>
