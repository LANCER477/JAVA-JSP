<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String str = "The String " + request.getAttribute("hash");
    int x = 10;
%>
<h1>Hello World!</h1>
<p>
    <%= str %>
    <%= x + 2 %>
</p>
<p>
    <% if(x % 2 == 0) { %>
        <b>Number <%= x %> is even</b>
    <% } else { %>
        <i>Number <%= x %> is odd</i>
    <% } %>
</p>
<ul>
    <% for(int i = 0; i < 10; ++i) { %>
        <li>i = <%=i%> never odd or even</li>
    <% } %>
</ul>
<%-- Статичне підключення файлу --%>
<jsp:include page="fragment1.jsp" />

<%-- Підключення файлу з передачею параметрів --%>
<jsp:include page="WEB-INF/fragment2.jsp">
    <jsp:param name="str" value="<%=str%>" />
</jsp:include>            

<p>
    Контроль проходження сервлету: <%= request.getAttribute("servlet") %>
</p>
