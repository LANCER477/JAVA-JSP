<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String adminPage = (String) request.getAttribute("adminPage");
    if (adminPage == null) {
        adminPage = "home";
    }
    String adminView = (String) request.getAttribute("adminView");
    if (adminView == null) {
        adminView = "/admin_home.jsp";
    }
    if (!adminView.startsWith("/")) {
        adminView = "/" + adminView;
    }
    String contextPath = request.getContextPath();
%>
<div class="admin-panel">
    <h2>Admin Page <%= adminPage %></h2>
    <div class="admin-menu" style="margin-bottom: 15px;">
        <a href="<%= contextPath %>/admin/">Home</a> &nbsp;
        <a href="<%= contextPath %>/admin/product">Product</a> &nbsp;
        <a href="<%= contextPath %>/admin/section">Section</a>
    </div>
    <div class="admin-content">
        <jsp:include page="<%= adminView %>" />
    </div>
</div>
