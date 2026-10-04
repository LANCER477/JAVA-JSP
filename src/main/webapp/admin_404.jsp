<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String invalidRoute = (String) request.getAttribute("invalidRoute");
%>
<div class="admin-404" style="background: #fff3f3; border: 1px solid #e74c3c; border-radius: 6px; padding: 20px; margin-top: 15px;">
    <h3 style="color: #c0392b; margin-top: 0;">404 - Внутрішній маршрут не знайдено</h3>
    <p>Запитаний підрозділ адміністративної панелі <code><%= invalidRoute != null ? invalidRoute : "" %></code> не існує.</p>
    <p>Будь ласка, скористайтеся меню вище або поверніться на <a href="<%= request.getContextPath() %>/admin/">головну сторінку адмінки</a>.</p>
</div>
