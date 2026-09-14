<%-- 
    Document   : index
    Created on : 10 вер. 2026 р., 09:39:02
    Author     : samoylenko_d
--%>

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
    
    String contextPath = request.getContextPath() ;   // частина адреси,
    // що відповідає за контекст - назва проєкту. Починається з "/",
    // завершується без "/". В ASP для цього спецсимвол "~"
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <header>
            The header
            <nav>
                <a href="<%= contextPath %>/gson" >Gson</a>
            </nav>
        </header>
        <main>
            <jsp:include page="<%= viewName %>" />
        </main>
        <footer>The footer. Shown in '<%= charsetName %>'. 
            Auth: <%= request.getAttribute("auth") %>
        </footer>
    </body>
</html>
