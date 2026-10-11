<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    List<String> sections = (List<String>) request.getAttribute("sections");
    String formMessage = (String) request.getAttribute("formMessage");
    String formError = (String) request.getAttribute("formError");
    Map<String, String> formFields = (Map<String, String>) request.getAttribute("formFields");
    Map<String, String> validationErrors = (Map<String, String>) request.getAttribute("validationErrors");
    String savedFileName = (String) request.getAttribute("savedFileName");

    String parentIdVal = formFields != null ? formFields.get("section-parent-id") : null;
    String titleVal = formFields != null && formFields.get("section-title") != null ? formFields.get("section-title") : "";
    String descVal = formFields != null && formFields.get("section-description") != null ? formFields.get("section-description") : "";
    String slugVal = formFields != null && formFields.get("section-slug") != null ? formFields.get("section-slug") : "";
    String orderVal = formFields != null && formFields.get("section-order") != null ? formFields.get("section-order") : "";

    String parentIdErr = validationErrors != null ? validationErrors.get("section-parent-id") : null;
    String titleErr = validationErrors != null ? validationErrors.get("section-title") : null;
    String descErr = validationErrors != null ? validationErrors.get("section-description") : null;
    String slugErr = validationErrors != null ? validationErrors.get("section-slug") : null;
    String orderErr = validationErrors != null ? validationErrors.get("section-order") : null;
    String imageErr = validationErrors != null ? validationErrors.get("section-image") : null;
%>
<h3>Section</h3>
<p>Додавання нового розділу товарів з валідацією даних форми.</p>

<% if (formMessage != null) { %>
    <div style="background: #e8f8f5; border: 1px solid #2ecc71; color: #27ae60; padding: 12px; border-radius: 6px; margin-bottom: 15px;">
        <b><%= formMessage %></b>
        <% if (savedFileName != null) { %>
            <div>Збережено у сховищі: <code><%= savedFileName %></code></div>
        <% } %>
    </div>
<% } %>

<% if (formError != null) { %>
    <div style="background: #fdf2e9; border: 1px solid #e74c3c; color: #c0392b; padding: 12px; border-radius: 6px; margin-bottom: 15px;">
        <b>Помилка:</b> <%= formError %>
        <% if (validationErrors != null && !validationErrors.isEmpty()) { %>
            <ul style="margin: 8px 0 0 20px; padding: 0;">
                <% for (Map.Entry<String, String> err : validationErrors.entrySet()) { %>
                    <li><b><%= err.getKey() %></b>: <%= err.getValue() %></li>
                <% } %>
            </ul>
        <% } %>
    </div>
<% } %>

<form method="post" action="<%= request.getContextPath() %>/admin/section" enctype="multipart/form-data" novalidate style="max-width: 600px; display: flex; flex-direction: column; gap: 14px; background: #fafafa; border: 1px solid #e0e0e0; padding: 20px; border-radius: 8px;">
    <div>
        <label for="section-parent-id"><b>Батьківський розділ (section-parent-id, опціонально):</b></label><br>
        <select id="section-parent-id" name="section-parent-id" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= parentIdErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;">
            <option value="">-- Без батьківського розділу (кореневий) --</option>
            <% if (sections != null) { 
                for (String sec : sections) { %>
                    <option value="<%= sec %>" <%= sec.equals(parentIdVal) ? "selected" : "" %>><%= sec %></option>
            <%   }
               } %>
        </select>
        <% if (parentIdErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= parentIdErr %></div>
        <% } %>
    </div>

    <div>
        <label for="section-title"><b>Назва розділу (section-title):</b></label><br>
        <input type="text" id="section-title" name="section-title" value="<%= titleVal %>" placeholder="Наприклад: Побутова техніка" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= titleErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;" />
        <% if (titleErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= titleErr %></div>
        <% } %>
    </div>

    <div>
        <label for="section-description"><b>Опис розділу (section-description):</b></label><br>
        <textarea id="section-description" name="section-description" rows="3" placeholder="Опис розділу товарів українською мовою" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= descErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;"><%= descVal %></textarea>
        <% if (descErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= descErr %></div>
        <% } %>
    </div>

    <div>
        <label for="section-slug"><b>Слаг розділу (section-slug):</b></label><br>
        <input type="text" id="section-slug" name="section-slug" value="<%= slugVal %>" placeholder="home-appliances" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= slugErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;" />
        <% if (slugErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= slugErr %></div>
        <% } %>
    </div>

    <div>
        <label for="section-order"><b>Порядок сортування (section-order):</b></label><br>
        <input type="text" id="section-order" name="section-order" value="<%= orderVal %>" placeholder="10" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= orderErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;" />
        <% if (orderErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= orderErr %></div>
        <% } %>
    </div>

    <div>
        <label for="section-image"><b>Зображення розділу (section-image):</b></label><br>
        <input type="file" id="section-image" name="section-image" accept="image/*" style="width: 100%; padding: 6px; font-size: 0.95rem; border: <%= imageErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;" />
        <% if (imageErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= imageErr %></div>
        <% } %>
    </div>

    <div style="margin-top: 8px;">
        <button type="submit" style="padding: 10px 20px; font-size: 1rem; background-color: #27ae60; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold;">
            Додати розділ
        </button>
    </div>
</form>
