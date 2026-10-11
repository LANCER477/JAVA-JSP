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

    String sectionIdVal = formFields != null ? formFields.get("product-section-id") : null;
    String titleVal = formFields != null && formFields.get("product-title") != null ? formFields.get("product-title") : "";
    String descVal = formFields != null && formFields.get("product-description") != null ? formFields.get("product-description") : "";
    String slugVal = formFields != null && formFields.get("product-slug") != null ? formFields.get("product-slug") : "";
    String priceVal = formFields != null && formFields.get("product-price") != null ? formFields.get("product-price") : "";
    String stockVal = formFields != null && formFields.get("product-stock") != null ? formFields.get("product-stock") : "";
    String orderVal = formFields != null && formFields.get("product-order") != null ? formFields.get("product-order") : "";

    String sectionErr = validationErrors != null ? validationErrors.get("product-section-id") : null;
    String titleErr = validationErrors != null ? validationErrors.get("product-title") : null;
    String descErr = validationErrors != null ? validationErrors.get("product-description") : null;
    String slugErr = validationErrors != null ? validationErrors.get("product-slug") : null;
    String priceErr = validationErrors != null ? validationErrors.get("product-price") : null;
    String stockErr = validationErrors != null ? validationErrors.get("product-stock") : null;
    String orderErr = validationErrors != null ? validationErrors.get("product-order") : null;
    String imageErr = validationErrors != null ? validationErrors.get("product-image") : null;
%>
<h3>Product</h3>

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

<form method="post" action="<%= request.getContextPath() %>/admin/product" enctype="multipart/form-data" novalidate style="max-width: 600px; display: flex; flex-direction: column; gap: 14px; background: #fafafa; border: 1px solid #e0e0e0; padding: 20px; border-radius: 8px;">
    <div>
        <label for="product-section-id"><b>Розділ товару (product-section-id):</b></label><br>
        <select id="product-section-id" name="product-section-id" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= sectionErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;">
            <option value="">-- Оберіть розділ товару --</option>
            <option value="3a27db1d-d97e-406f-a36a-148b4f724a6e" <%= "3a27db1d-d97e-406f-a36a-148b4f724a6e".equals(sectionIdVal) ? "selected" : "" %>>Garminfull</option>
            <option value="5b38ec2e-e08f-4170-b47b-259c50835b7f" <%= "5b38ec2e-e08f-4170-b47b-259c50835b7f".equals(sectionIdVal) ? "selected" : "" %>>Телевізори</option>
            <option value="6c49fd3f-f190-4281-c58c-360d61946c8a" <%= "6c49fd3f-f190-4281-c58c-360d61946c8a".equals(sectionIdVal) ? "selected" : "" %>>Консолі</option>
            <option value="7d5a0e4a-02a1-4392-d69d-471e72057d9b" <%= "7d5a0e4a-02a1-4392-d69d-471e72057d9b".equals(sectionIdVal) ? "selected" : "" %>>Б/у Apple</option>
        </select>
        <% if (sectionErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= sectionErr %></div>
        <% } %>
    </div>

    <div>
        <label for="product-title"><b>Назва товару (product-title):</b></label><br>
        <input type="text" id="product-title" name="product-title" value="<%= titleVal %>" placeholder="Наприклад: Новітній Ґаджет 'Їжачок-1'" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= titleErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;" />
        <% if (titleErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= titleErr %></div>
        <% } %>
    </div>

    <div>
        <label for="product-description"><b>Опис товару (product-description):</b></label><br>
        <textarea id="product-description" name="product-description" rows="3" placeholder="Опис товару винятково українськими літерами" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= descErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;"><%= descVal %></textarea>
        <% if (descErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= descErr %></div>
        <% } %>
    </div>

    <div>
        <label for="product-slug"><b>Слаг товару (product-slug):</b></label><br>
        <input type="text" id="product-slug" name="product-slug" value="<%= slugVal %>" placeholder="novitnii-gadget-yizhachok" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= slugErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;" />
        <% if (slugErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= slugErr %></div>
        <% } %>
    </div>

    <div style="display: flex; gap: 12px;">
        <div style="flex: 1;">
            <label for="product-price"><b>Ціна (product-price):</b></label><br>
            <input type="text" id="product-price" name="product-price" value="<%= priceVal %>" placeholder="15999.00" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= priceErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;" />
            <% if (priceErr != null) { %>
                <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= priceErr %></div>
            <% } %>
        </div>
        <div style="flex: 1;">
            <label for="product-stock"><b>Кількість (product-stock):</b></label><br>
            <input type="text" id="product-stock" name="product-stock" value="<%= stockVal %>" placeholder="25" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= stockErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;" />
            <% if (stockErr != null) { %>
                <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= stockErr %></div>
            <% } %>
        </div>
        <div style="flex: 1;">
            <label for="product-order"><b>Порядок (product-order):</b></label><br>
            <input type="text" id="product-order" name="product-order" value="<%= orderVal %>" placeholder="1" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: <%= orderErr != null ? "1.5px solid #e74c3c; background-color: #fff6f6;" : "1px solid #ccc;" %> border-radius: 4px; margin-top: 4px;" />
            <% if (orderErr != null) { %>
                <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= orderErr %></div>
            <% } %>
        </div>
    </div>

    <div>
        <label for="product-image"><b>Зображення товару (product-image):</b></label><br>
        <input type="file" id="product-image" name="product-image" style="width: 100%; padding: 6px 0; margin-top: 4px; <%= imageErr != null ? "border: 1.5px solid #e74c3c; padding: 6px; background-color: #fff6f6; border-radius: 4px;" : "" %>" />
        <small style="color: #666;">Підтримуються кириличні назви файлів (наприклад, <code>Зображення_Ґаджета_Їжак.jpg</code>). Максимальний розмір: 10 МБ.</small>
        <% if (imageErr != null) { %>
            <div style="color: #c0392b; font-size: 0.85rem; margin-top: 4px; font-weight: 500;">&#9888; <%= imageErr %></div>
        <% } %>
    </div>

    <div style="margin-top: 8px;">
        <button type="submit" style="padding: 10px 24px; font-size: 1rem; font-weight: bold; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer;">Додати товар</button>
    </div>
</form>
