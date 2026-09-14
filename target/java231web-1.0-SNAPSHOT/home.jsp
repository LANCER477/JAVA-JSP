<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String str = "The String " + request.getAttribute("hash");
    int x = 10;
    Object ts10 = request.getAttribute("timestamp10");
    Object ts13 = request.getAttribute("timestamp13");
%>

<div class="card timestamp-card">
    <h3>Сервіс часу (Timestamp Service)</h3>
    <p>Мітка часу у двох форматах:</p>
    <ul>
        <li>10 цифр (точність до секунди): <span class="timestamp-value"><%= ts10 != null ? ts10 : "N/A" %></span></li>
        <li>13 цифр (точність до мілісекунди): <span class="timestamp-value"><%= ts13 != null ? ts13 : "N/A" %></span></li>
    </ul>
</div>

<div class="card">
    <h2>Hello World &amp; Hash Service</h2>
    <p><b>Hash (MD5 з "123"):</b> <code><%= request.getAttribute("hash") %></code></p>
    <p><%= str %> | Результат x + 2 = <b><%= x + 2 %></b></p>
    <p>
        <% if(x % 2 == 0) { %>
            <b>Number <%= x %> is even</b>
        <% } else { %>
            <i>Number <%= x %> is odd</i>
        <% } %>
    </p>
    <p>Контроль проходження сервлету: <b><%= request.getAttribute("servlet") %></b></p>
</div>

<div class="card">
    <h3>Цикл:</h3>
    <ul>
        <% for(int i = 0; i < 5; ++i) { %>
            <li>i = <%=i%> never odd or even</li>
        <% } %>
    </ul>

    <jsp:include page="fragment1.jsp" />

    <jsp:include page="WEB-INF/fragment2.jsp">
        <jsp:param name="str" value="<%=str%>" />
    </jsp:include>
</div>
