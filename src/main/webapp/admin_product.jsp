<%@page import="step.learning.java231web.data.dto.ProductSection" %>
    <%@page import="step.learning.java231web.data.models.AdminProductViewModel" %>
        <%@page contentType="text/html" pageEncoding="UTF-8" %>
            <% AdminProductViewModel viewModel=(AdminProductViewModel) request.getAttribute("viewModel"); String
                formMessage=(String) request.getAttribute("formMessage"); String formError=(String)
                request.getAttribute("formError"); String savedFileName=(String) request.getAttribute("savedFileName");
                %>

                <% if (viewModel==null) { %>
                    <h1 style="color:maroon">Model transfer error</h1>
                    <% } else { %>

                        <style>
                            .admin-forms-table {
                                width: 100%;
                                border-collapse: separate;
                                border-spacing: 20px 0;
                                margin: 15px 0 25px 0;
                            }

                            .admin-form-col {
                                vertical-align: top;
                                width: 50%;
                                background: #ffffff;
                                border: 1px solid #e2e8f0;
                                padding: 24px;
                                border-radius: 10px;
                                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
                                box-sizing: border-box;
                            }

                            .admin-form-col h2 {
                                margin-top: 0;
                                margin-bottom: 16px;
                                color: #1e293b;
                                font-size: 1.4rem;
                                border-bottom: 2px solid #f1f5f9;
                                padding-bottom: 10px;
                            }

                            .admin-form {
                                display: flex;
                                flex-direction: column;
                                gap: 6px;
                                width: 100%;
                                box-sizing: border-box;
                            }

                            .admin-form-group {
                                display: flex;
                                flex-direction: column;
                                width: 100%;
                                box-sizing: border-box;
                                margin-bottom: 6px;
                            }

                            .admin-form label {
                                display: block !important;
                                font-weight: 600 !important;
                                font-size: 0.9rem !important;
                                color: #334155 !important;
                                margin-bottom: 3px !important;
                            }

                            .admin-form input,
                            .admin-form select,
                            .admin-form textarea {
                                width: 100% !important;
                                box-sizing: border-box !important;
                                display: block !important;
                                height: 38px;
                                padding: 6px 12px !important;
                                font-size: 0.95rem !important;
                                font-family: inherit !important;
                                color: #1e293b !important;
                                background-color: #ffffff !important;
                                border: 1px solid #cbd5e1 !important;
                                border-radius: 6px !important;
                                outline: none !important;
                                transition: border-color 0.15s ease, box-shadow 0.15s ease !important;
                            }

                            .admin-form input:focus,
                            .admin-form select:focus,
                            .admin-form textarea:focus {
                                border-color: #0284c7 !important;
                                box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.18) !important;
                            }

                            .admin-form input[type="file"] {
                                height: auto !important;
                                padding: 8px 10px !important;
                                background-color: #f8fafc !important;
                                border: 1px dashed #94a3b8 !important;
                                cursor: pointer !important;
                            }

                            .admin-form input::placeholder,
                            .admin-form textarea::placeholder {
                                color: #94a3b8;
                            }

                            .admin-form .row-3 {
                                display: grid !important;
                                grid-template-columns: repeat(3, minmax(0, 1fr)) !important;
                                gap: 12px !important;
                                width: 100% !important;
                                box-sizing: border-box !important;
                                margin-bottom: 6px;
                                align-items: end !important;
                            }

                            .admin-form .row-3>div {
                                display: flex !important;
                                flex-direction: column !important;
                                justify-content: flex-end !important;
                                min-width: 0 !important;
                            }

                            .admin-form .row-3 label {
                                min-height: 38px !important;
                                line-height: 1.25 !important;
                                display: flex !important;
                                align-items: flex-end !important;
                                margin-bottom: 4px !important;
                            }

                            .admin-form button {
                                align-self: flex-start !important;
                                margin-top: 10px !important;
                                padding: 10px 26px !important;
                                font-size: 0.95rem !important;
                                font-weight: 600 !important;
                                color: #ffffff !important;
                                background-color: #0284c7 !important;
                                border: none !important;
                                border-radius: 6px !important;
                                cursor: pointer !important;
                                box-shadow: 0 2px 4px rgba(2, 132, 199, 0.25) !important;
                                transition: background-color 0.15s ease, transform 0.1s ease !important;
                            }

                            .admin-form button:hover {
                                background-color: #0369a1 !important;
                            }

                            .admin-form button:active {
                                transform: translateY(1px);
                            }

                            .admin-status-bar {
                                margin-top: 20px;
                                padding: 14px 18px;
                                border-radius: 8px;
                                background: #ffffff;
                                border: 1px solid #e2e8f0;
                                font-family: Consolas, "Courier New", monospace;
                                font-size: 0.95rem;
                                line-height: 1.6;
                                box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
                            }

                            .admin-status-ok {
                                color: #16a34a;
                                font-weight: bold;
                            }

                            .admin-status-ex {
                                color: #dc2626;
                                font-weight: bold;
                            }
                        </style>

                        <% if (formMessage !=null || viewModel.getOkMessage() !=null) { %>
                            <div
                                style="background: #f0fdf4; border: 1px solid #86efac; color: #166534; padding: 12px 16px; border-radius: 8px; margin-bottom: 16px;">
                                <b>&#10004; <%= viewModel.getOkMessage() !=null ? viewModel.getOkMessage() : formMessage
                                        %></b>
                                <% if (savedFileName !=null) { %>
                                    <div style="margin-top: 4px; font-size: 0.9rem;">Збережено файл у сховищі:
                                        <code><%= savedFileName %></code>
                                    </div>
                                    <% } %>
                            </div>
                            <% } %>

                                <% if (formError !=null || viewModel.getExceptionMessage() !=null) { %>
                                    <div
                                        style="background: #fef2f2; border: 1px solid #fca5a5; color: #991b1b; padding: 12px 16px; border-radius: 8px; margin-bottom: 16px;">
                                        <b>&#9888; Помилка:</b>
                                        <%= viewModel.getExceptionMessage() !=null ? viewModel.getExceptionMessage() :
                                            formError %>
                                    </div>
                                    <% } %>

                                        <table class="admin-forms-table">
                                            <tbody>
                                                <tr>
                                                    <td class="admin-form-col">
                                                        <h2>Product</h2>
                                                        <form action="product" method="post"
                                                            enctype="multipart/form-data" class="admin-form">
                                                            <div class="admin-form-group">
                                                                <label for="product-section-id">Виберіть товарну
                                                                    групу:</label>
                                                                <select id="product-section-id"
                                                                    name="product-section-id">
                                                                    <% for(ProductSection ps :
                                                                        viewModel.getProductSections()) { %>
                                                                        <option value="<%= ps.getId() %>">
                                                                            <%= ps.getTitle() %>
                                                                        </option>
                                                                        <% } %>
                                                                </select>
                                                            </div>

                                                            <div class="admin-form-group">
                                                                <label for="product-title">Назва товару:</label>
                                                                <input type="text" id="product-title"
                                                                    name="product-title"
                                                                    placeholder="Наприклад: Новітній Ґаджет" />
                                                            </div>

                                                            <div class="admin-form-group">
                                                                <label for="product-description">Опис товару:</label>
                                                                <input type="text" id="product-description"
                                                                    name="product-description"
                                                                    placeholder="Детальний опис товару" />
                                                            </div>

                                                            <div class="admin-form-group">
                                                                <label for="product-slug">Slug товару:</label>
                                                                <input type="text" id="product-slug" name="product-slug"
                                                                    placeholder="novitnii-gadget" />
                                                            </div>

                                                            <div class="row-3">
                                                                <div>
                                                                    <label for="product-price">Ціна товару:</label>
                                                                    <input type="number" step="0.01" min="0.01"
                                                                        id="product-price" name="product-price"
                                                                        placeholder="15999.00" />
                                                                </div>
                                                                <div>
                                                                    <label for="product-stock">Кількість на
                                                                        складі:</label>
                                                                    <input type="number" step="1" id="product-stock"
                                                                        name="product-stock" placeholder="10" />
                                                                </div>
                                                                <div>
                                                                    <label for="product-order">Порядок у
                                                                        переліку:</label>
                                                                    <input type="number" step="1" id="product-order"
                                                                        name="product-order" placeholder="1" />
                                                                </div>
                                                            </div>

                                                            <div class="admin-form-group">
                                                                <label for="product-image">Головне зображення:</label>
                                                                <input type="file" id="product-image"
                                                                    name="product-image" />
                                                            </div>

                                                            <button type="submit">Додати</button>
                                                        </form>
                                                    </td>

                                                    <td class="admin-form-col">
                                                        <h2>Product Version</h2>
                                                        <form action="product-version" method="post"
                                                            enctype="multipart/form-data" class="admin-form">
                                                            <div class="admin-form-group">
                                                                <label for="version-product-id">Виберіть товар:</label>
                                                                <select id="version-product-id" name="product-id">
                                                                    <% for(ProductSection ps :
                                                                        viewModel.getProductSections()) { %>
                                                                        <option value="<%= ps.getId() %>">
                                                                            <%= ps.getTitle() %>
                                                                        </option>
                                                                        <% } %>
                                                                </select>
                                                            </div>

                                                            <div class="admin-form-group">
                                                                <label for="version-title">Доповнення до назви
                                                                    товару:</label>
                                                                <input type="text" id="version-title"
                                                                    name="product-title"
                                                                    placeholder="Наприклад: Cosmic Red / 256GB" />
                                                            </div>

                                                            <div class="admin-form-group">
                                                                <label for="version-description">Доповнення до опису
                                                                    товару:</label>
                                                                <input type="text" id="version-description"
                                                                    name="product-description"
                                                                    placeholder="Особливості цієї версії товару" />
                                                            </div>

                                                            <div class="admin-form-group">
                                                                <label for="version-slug">Доповнення до Slug
                                                                    товару:</label>
                                                                <input type="text" id="version-slug" name="product-slug"
                                                                    placeholder="cosmic-red" />
                                                            </div>

                                                            <div class="row-3">
                                                                <div>
                                                                    <label for="version-price">Ціна версії:</label>
                                                                    <input type="number" step="0.01" min="0.01"
                                                                        id="version-price" name="product-price"
                                                                        placeholder="16999.00" />
                                                                </div>
                                                                <div>
                                                                    <label for="version-stock">Кількість на
                                                                        складі:</label>
                                                                    <input type="number" step="1" id="version-stock"
                                                                        name="product-stock" placeholder="5" />
                                                                </div>
                                                                <div>
                                                                    <label for="version-order">Порядок у
                                                                        переліку:</label>
                                                                    <input type="number" step="1" id="version-order"
                                                                        name="product-order" placeholder="1" />
                                                                </div>
                                                            </div>

                                                            <div class="admin-form-group">
                                                                <label for="version-image">Зображення:</label>
                                                                <input type="file" id="version-image"
                                                                    name="product-image" />
                                                            </div>

                                                            <button type="submit">Додати</button>
                                                        </form>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>

                                        <div class="admin-status-bar">
                                            OK: <span class="admin-status-ok">
                                                <%= viewModel.getOkMessage() %>
                                            </span>
                                            <br />
                                            Ex: <span class="admin-status-ex">
                                                <%= viewModel.getExceptionMessage() %>
                                            </span>
                                        </div>

                                        <% } %>