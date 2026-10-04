<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    List<String> sections = (List<String>) request.getAttribute("sections");
    String formMessage = (String) request.getAttribute("formMessage");
    String formError = (String) request.getAttribute("formError");
    Map<String, String> formFields = (Map<String, String>) request.getAttribute("formFields");
    String savedFileName = (String) request.getAttribute("savedFileName");
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
    </div>
<% } %>

<form method="post" action="<%= request.getContextPath() %>/admin/product" enctype="multipart/form-data" style="max-width: 600px; display: flex; flex-direction: column; gap: 14px; background: #fafafa; border: 1px solid #e0e0e0; padding: 20px; border-radius: 8px;">
    <div>
        <label for="product-section-id"><b>Розділ товару (product-section-id):</b></label><br>
        <select id="product-section-id" name="product-section-id" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: 1px solid #ccc; border-radius: 4px; margin-top: 4px;">
            <option value="3a27db1d-d97e-406f-a36a-148b4f724a6e">Garminfull</option>
            <option value="5b38ec2e-e08f-4170-b47b-259c50835b7f">Телевізори</option>
            <option value="6c49fd3f-f190-4281-c58c-360d61946c8a">Консолі</option>
            <option value="7d5a0e4a-02a1-4392-d69d-471e72057d9b">Б/у Apple</option>
        </select>
    </div>

    <div>
        <label for="product-title"><b>Назва товару (product-title):</b></label><br>
        <input type="text" id="product-title" name="product-title" placeholder="Наприклад: Новітній Ґаджет 'Їжачок-1'" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: 1px solid #ccc; border-radius: 4px; margin-top: 4px;" required />
    </div>

    <div>
        <label for="product-description"><b>Опис товару (product-description):</b></label><br>
        <textarea id="product-description" name="product-description" rows="3" placeholder="Опис товару винятково українськими літерами (наприклад: Надійний український пристрій з підтримкою сучасних технологій)" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: 1px solid #ccc; border-radius: 4px; margin-top: 4px;" required></textarea>
    </div>

    <div>
        <label for="product-slug"><b>Слаг товару (product-slug):</b></label><br>
        <input type="text" id="product-slug" name="product-slug" placeholder="novitnii-gadget-yizhachok" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: 1px solid #ccc; border-radius: 4px; margin-top: 4px;" required />
    </div>

    <div style="display: flex; gap: 12px;">
        <div style="flex: 1;">
            <label for="product-price"><b>Ціна (product-price):</b></label><br>
            <input type="number" step="0.01" id="product-price" name="product-price" placeholder="15999.00" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: 1px solid #ccc; border-radius: 4px; margin-top: 4px;" required />
        </div>
        <div style="flex: 1;">
            <label for="product-stock"><b>Кількість (product-stock):</b></label><br>
            <input type="number" id="product-stock" name="product-stock" placeholder="25" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: 1px solid #ccc; border-radius: 4px; margin-top: 4px;" required />
        </div>
        <div style="flex: 1;">
            <label for="product-order"><b>Порядок (product-order):</b></label><br>
            <input type="number" id="product-order" name="product-order" placeholder="1" style="width: 100%; padding: 8px 10px; font-size: 1rem; border: 1px solid #ccc; border-radius: 4px; margin-top: 4px;" required />
        </div>
    </div>

    <div>
        <label for="product-image"><b>Зображення товару (product-image):</b></label><br>
        <input type="file" id="product-image" name="product-image" style="width: 100%; padding: 6px 0; margin-top: 4px;" required />
        <small style="color: #666;">Підтримуються кириличні назви файлів (наприклад, <code>Зображення_Ґаджета_Їжак.jpg</code>). Максимальний розмір: 10 МБ.</small>
    </div>

    <div style="margin-top: 8px;">
        <button type="submit" style="padding: 10px 24px; font-size: 1rem; font-weight: bold; background: #2980b9; color: #fff; border: none; border-radius: 4px; cursor: pointer;">Додати товар</button>
    </div>
</form>
