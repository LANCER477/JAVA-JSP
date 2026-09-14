<%@page import="java.nio.charset.Charset"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String viewName = (String) request.getAttribute("servlet");
    if(viewName == null) {
        viewName = "home.jsp";
    }
    else {
        viewName = viewName.toLowerCase() + ".jsp";
    }
    Charset charset = (Charset) request.getAttribute("charset");
    String charsetName = charset == null ? "undefined" : charset.name();
    
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Java 231 Web</title>
        <link rel="stylesheet" href="<%= contextPath %>/css/style.css">
    </head>
    <body>
        <header>
            <a href="<%= contextPath %>/" class="header-brand">Java231Web</a>
            <nav>
                <a href="<%= contextPath %>/">Головна</a>
                <a href="<%= contextPath %>/about">About</a>
                <a href="<%= contextPath %>/gson">Gson API</a>
            </nav>
        </header>
        <main>
            <jsp:include page="<%= viewName %>" />
        </main>
        <footer>
            Java 231 Web &bull; Кодування: <b><%= charsetName %></b> &bull; Auth: <b><%= request.getAttribute("auth") %></b>
        </footer>
    </body>
</html>
