<%@page import="java.util.Map" %>
    <%@page import="step.learning.java231web.data.dto.Product" %>
        <%@page import="step.learning.java231web.data.dto.ProductSection" %>
            <%@page import="step.learning.java231web.data.models.AdminProductViewModel" %>
                <%@page contentType="text/html" pageEncoding="UTF-8" %>
                    <% AdminProductViewModel viewModel=(AdminProductViewModel) request.getAttribute("viewModel"); String
                        formMessage=(String) request.getAttribute("formMessage"); String formError=(String)
                        request.getAttribute("formError"); String savedFileName=(String)
                        request.getAttribute("savedFileName"); String formType=(String)
                        request.getAttribute("formType"); @SuppressWarnings("unchecked") Map<String, String> formFields
                        = (Map<String, String>) request.getAttribute("formFields");
                            @SuppressWarnings("unchecked")
                            Map<String, String> validationErrors = (Map<String, String>)
                                    request.getAttribute("validationErrors");

                                    boolean isVersionForm = "version".equalsIgnoreCase(formType);
                                    boolean isProductForm = "product".equalsIgnoreCase(formType) || (!isVersionForm &&
                                    formFields != null && formFields.containsKey("product-section-id"));

                                    // Version form values
                                    String verProductId = (isVersionForm && formFields != null) ?
                                    formFields.get("product-id") : null;
                                    String verTitle = (isVersionForm && formFields != null &&
                                    formFields.get("product-title") != null) ? formFields.get("product-title") : "";
                                    String verDesc = (isVersionForm && formFields != null &&
                                    formFields.get("product-description") != null) ?
                                    formFields.get("product-description") : "";
                                    String verSlug = (isVersionForm && formFields != null &&
                                    formFields.get("product-slug") != null) ? formFields.get("product-slug") : "";
                                    String verPrice = (isVersionForm && formFields != null &&
                                    formFields.get("product-price") != null) ? formFields.get("product-price") : "";
                                    String verStock = (isVersionForm && formFields != null &&
                                    formFields.get("product-stock") != null) ? formFields.get("product-stock") : "";
                                    String verOrder = (isVersionForm && formFields != null &&
                                    formFields.get("product-order") != null) ? formFields.get("product-order") : "";

                                    // Version form errors
                                    String verProductIdErr = (isVersionForm && validationErrors != null) ?
                                    validationErrors.get("product-id") : null;
                                    String verTitleErr = (isVersionForm && validationErrors != null) ?
                                    validationErrors.get("product-title") : null;
                                    String verDescErr = (isVersionForm && validationErrors != null) ?
                                    validationErrors.get("product-description") : null;
                                    String verSlugErr = (isVersionForm && validationErrors != null) ?
                                    validationErrors.get("product-slug") : null;
                                    String verPriceErr = (isVersionForm && validationErrors != null) ?
                                    validationErrors.get("product-price") : null;
                                    String verStockErr = (isVersionForm && validationErrors != null) ?
                                    validationErrors.get("product-stock") : null;
                                    String verOrderErr = (isVersionForm && validationErrors != null) ?
                                    validationErrors.get("product-order") : null;
                                    String verImageErr = (isVersionForm && validationErrors != null) ?
                                    validationErrors.get("product-image") : null;

                                    // Product form values
                                    String prodSectionId = (isProductForm && formFields != null) ?
                                    formFields.get("product-section-id") : null;
                                    String prodTitle = (isProductForm && formFields != null &&
                                    formFields.get("product-title") != null) ? formFields.get("product-title") : "";
                                    String prodDesc = (isProductForm && formFields != null &&
                                    formFields.get("product-description") != null) ?
                                    formFields.get("product-description") : "";
                                    String prodSlug = (isProductForm && formFields != null &&
                                    formFields.get("product-slug") != null) ? formFields.get("product-slug") : "";
                                    String prodPrice = (isProductForm && formFields != null &&
                                    formFields.get("product-price") != null) ? formFields.get("product-price") : "";
                                    String prodStock = (isProductForm && formFields != null &&
                                    formFields.get("product-stock") != null) ? formFields.get("product-stock") : "";
                                    String prodOrder = (isProductForm && formFields != null &&
                                    formFields.get("product-order") != null) ? formFields.get("product-order") : "";

                                    // Product form errors
                                    String prodSectionIdErr = (isProductForm && validationErrors != null) ?
                                    validationErrors.get("product-section-id") : null;
                                    String prodTitleErr = (isProductForm && validationErrors != null) ?
                                    validationErrors.get("product-title") : null;
                                    String prodDescErr = (isProductForm && validationErrors != null) ?
                                    validationErrors.get("product-description") : null;
                                    String prodSlugErr = (isProductForm && validationErrors != null) ?
                                    validationErrors.get("product-slug") : null;
                                    String prodPriceErr = (isProductForm && validationErrors != null) ?
                                    validationErrors.get("product-price") : null;
                                    String prodStockErr = (isProductForm && validationErrors != null) ?
                                    validationErrors.get("product-stock") : null;
                                    String prodOrderErr = (isProductForm && validationErrors != null) ?
                                    validationErrors.get("product-order") : null;
                                    String prodImageErr = (isProductForm && validationErrors != null) ?
                                    validationErrors.get("product-image") : null;
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

                                                .admin-form .input-error {
                                                    border-color: #ef4444 !important;
                                                    background-color: #fff5f5 !important;
                                                }

                                                .admin-form .input-error:focus {
                                                    border-color: #dc2626 !important;
                                                    box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.2) !important;
                                                }

                                                .field-error-msg {
                                                    color: #dc2626;
                                                    font-size: 0.8rem;
                                                    font-weight: 500;
                                                    margin-top: 3px;
                                                    line-height: 1.25;
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
                                                    <b>&#10004; <%= viewModel.getOkMessage() !=null ?
                                                            viewModel.getOkMessage() : formMessage %></b>
                                                    <% if (savedFileName !=null) { %>
                                                        <div style="margin-top: 4px; font-size: 0.9rem;">Збережено файл
                                                            у сховищі:
                                                            <code><%= savedFileName %></code>
                                                        </div>
                                                        <% } %>
                                                </div>
                                                <% } %>

                                                    <% if (formError !=null || viewModel.getExceptionMessage() !=null) {
                                                        %>
                                                        <div
                                                            style="background: #fef2f2; border: 1px solid #fca5a5; color: #991b1b; padding: 12px 16px; border-radius: 8px; margin-bottom: 16px;">
                                                            <b>&#9888; Помилка:</b>
                                                            <%= viewModel.getExceptionMessage() !=null ?
                                                                viewModel.getExceptionMessage() : formError %>
                                                                <% if (validationErrors !=null &&
                                                                    !validationErrors.isEmpty()) { %>
                                                                    <ul
                                                                        style="margin: 8px 0 0 20px; padding: 0; font-size: 0.9rem;">
                                                                        <% for (Map.Entry<String, String> err :
                                                                            validationErrors.entrySet()) { %>
                                                                            <li><b>
                                                                                    <%= err.getKey() %>
                                                                                </b>: <%= err.getValue() %>
                                                                            </li>
                                                                            <% } %>
                                                                    </ul>
                                                                    <% } %>
                                                        </div>
                                                        <% } %>

                                                            <table class="admin-forms-table">
                                                                <tbody>
                                                                    <tr>
                                                                        <!-- Левая колонка: Product -->
                                                                        <td class="admin-form-col">
                                                                            <h2>Product</h2>
                                                                            <form action="product" method="post"
                                                                                enctype="multipart/form-data"
                                                                                class="admin-form" novalidate>
                                                                                <div class="admin-form-group">
                                                                                    <label
                                                                                        for="product-section-id">Виберіть
                                                                                        товарну групу:</label>
                                                                                    <select id="product-section-id"
                                                                                        name="product-section-id"
                                                                                        class="<%= prodSectionIdErr != null ? "
                                                                                        input-error" : "" %>">
                                                                                        <option value="">-- Оберіть
                                                                                            товарну групу --</option>
                                                                                        <% for(ProductSection ps :
                                                                                            viewModel.getProductSections())
                                                                                            { %>
                                                                                            <option
                                                                                                value="<%= ps.getId() %>"
                                                                                                <%=ps.getId().equals(prodSectionId)
                                                                                                ? "selected" : "" %>>
                                                                                                <%= ps.getTitle() %>
                                                                                            </option>
                                                                                            <% } %>
                                                                                    </select>
                                                                                    <% if (prodSectionIdErr !=null) { %>
                                                                                        <div class="field-error-msg">
                                                                                            &#9888; <%= prodSectionIdErr
                                                                                                %>
                                                                                        </div>
                                                                                        <% } %>
                                                                                </div>

                                                                                <div class="admin-form-group">
                                                                                    <label for="product-title">Назва
                                                                                        товару:</label>
                                                                                    <input type="text"
                                                                                        id="product-title"
                                                                                        name="product-title"
                                                                                        value="<%= prodTitle %>"
                                                                                        class="<%= prodTitleErr != null ? "
                                                                                        input-error" : "" %>"
                                                                                    placeholder="Наприклад: Новітній
                                                                                    Ґаджет" />
                                                                                    <% if (prodTitleErr !=null) { %>
                                                                                        <div class="field-error-msg">
                                                                                            &#9888; <%= prodTitleErr %>
                                                                                        </div>
                                                                                        <% } %>
                                                                                </div>

                                                                                <div class="admin-form-group">
                                                                                    <label
                                                                                        for="product-description">Опис
                                                                                        товару:</label>
                                                                                    <input type="text"
                                                                                        id="product-description"
                                                                                        name="product-description"
                                                                                        value="<%= prodDesc %>"
                                                                                        class="<%= prodDescErr != null ? "
                                                                                        input-error" : "" %>"
                                                                                    placeholder="Детальний опис товару"
                                                                                    />
                                                                                    <% if (prodDescErr !=null) { %>
                                                                                        <div class="field-error-msg">
                                                                                            &#9888; <%= prodDescErr %>
                                                                                        </div>
                                                                                        <% } %>
                                                                                </div>

                                                                                <div class="admin-form-group">
                                                                                    <label for="product-slug">Slug
                                                                                        товару:</label>
                                                                                    <input type="text" id="product-slug"
                                                                                        name="product-slug"
                                                                                        value="<%= prodSlug %>"
                                                                                        class="<%= prodSlugErr != null ? "
                                                                                        input-error" : "" %>"
                                                                                    placeholder="novitnii-gadget" />
                                                                                    <% if (prodSlugErr !=null) { %>
                                                                                        <div class="field-error-msg">
                                                                                            &#9888; <%= prodSlugErr %>
                                                                                        </div>
                                                                                        <% } %>
                                                                                </div>

                                                                                <div class="row-3">
                                                                                    <div>
                                                                                        <label for="product-price">Ціна
                                                                                            товару:</label>
                                                                                        <input type="number" step="0.01"
                                                                                            min="0.01"
                                                                                            id="product-price"
                                                                                            name="product-price"
                                                                                            value="<%= prodPrice %>"
                                                                                            class="<%= prodPriceErr != null ? "
                                                                                            input-error" : "" %>"
                                                                                        placeholder="15999.00" />
                                                                                        <% if (prodPriceErr !=null) { %>
                                                                                            <div
                                                                                                class="field-error-msg">
                                                                                                &#9888; <%= prodPriceErr
                                                                                                    %>
                                                                                            </div>
                                                                                            <% } %>
                                                                                    </div>
                                                                                    <div>
                                                                                        <label
                                                                                            for="product-stock">Кількість
                                                                                            на складі:</label>
                                                                                        <input type="number" step="1"
                                                                                            id="product-stock"
                                                                                            name="product-stock"
                                                                                            value="<%= prodStock %>"
                                                                                            class="<%= prodStockErr != null ? "
                                                                                            input-error" : "" %>"
                                                                                        placeholder="10" />
                                                                                        <% if (prodStockErr !=null) { %>
                                                                                            <div
                                                                                                class="field-error-msg">
                                                                                                &#9888; <%= prodStockErr
                                                                                                    %>
                                                                                            </div>
                                                                                            <% } %>
                                                                                    </div>
                                                                                    <div>
                                                                                        <label
                                                                                            for="product-order">Порядок
                                                                                            у переліку:</label>
                                                                                        <input type="number" step="1"
                                                                                            id="product-order"
                                                                                            name="product-order"
                                                                                            value="<%= prodOrder %>"
                                                                                            class="<%= prodOrderErr != null ? "
                                                                                            input-error" : "" %>"
                                                                                        placeholder="1" />
                                                                                        <% if (prodOrderErr !=null) { %>
                                                                                            <div
                                                                                                class="field-error-msg">
                                                                                                &#9888; <%= prodOrderErr
                                                                                                    %>
                                                                                            </div>
                                                                                            <% } %>
                                                                                    </div>
                                                                                </div>

                                                                                <div class="admin-form-group">
                                                                                    <label for="product-image">Головне
                                                                                        зображення:</label>
                                                                                    <input type="file"
                                                                                        id="product-image"
                                                                                        name="product-image"
                                                                                        class="<%= prodImageErr != null ? "
                                                                                        input-error" : "" %>" />
                                                                                    <% if (prodImageErr !=null) { %>
                                                                                        <div class="field-error-msg">
                                                                                            &#9888; <%= prodImageErr %>
                                                                                        </div>
                                                                                        <% } %>
                                                                                </div>

                                                                                <button type="submit">Додати</button>
                                                                            </form>
                                                                        </td>

                                                                        <!-- Правая колонка: Product Version -->
                                                                        <td class="admin-form-col">
                                                                            <h2>Product Version</h2>
                                                                            <form action="product-version" method="post"
                                                                                enctype="multipart/form-data"
                                                                                class="admin-form" novalidate>
                                                                                <div class="admin-form-group">
                                                                                    <label
                                                                                        for="version-product-id">Виберіть
                                                                                        товар:</label>
                                                                                    <select id="version-product-id"
                                                                                        name="product-id"
                                                                                        class="<%= verProductIdErr != null ? "
                                                                                        input-error" : "" %>">
                                                                                        <option value="">-- Оберіть
                                                                                            товар зі списку --</option>
                                                                                        <% if (viewModel.getProducts()
                                                                                            !=null &&
                                                                                            !viewModel.getProducts().isEmpty())
                                                                                            { for(Product p :
                                                                                            viewModel.getProducts()) {
                                                                                            %>
                                                                                            <option
                                                                                                value="<%= p.getId() %>"
                                                                                                <%=p.getId().equals(verProductId)
                                                                                                ? "selected" : "" %>>
                                                                                                <%= p.getTitle() %>
                                                                                            </option>
                                                                                            <% } } else if
                                                                                                (viewModel.getProductSections()
                                                                                                !=null) {
                                                                                                for(ProductSection ps :
                                                                                                viewModel.getProductSections())
                                                                                                { %>
                                                                                                <option
                                                                                                    value="<%= ps.getId() %>"
                                                                                                    <%=ps.getId().equals(verProductId)
                                                                                                    ? "selected" : "" %>
                                                                                                    >
                                                                                                    <%= ps.getTitle() %>
                                                                                                </option>
                                                                                                <% } } %>
                                                                                    </select>
                                                                                    <% if (verProductIdErr !=null) { %>
                                                                                        <div class="field-error-msg">
                                                                                            &#9888; <%= verProductIdErr
                                                                                                %>
                                                                                        </div>
                                                                                        <% } %>
                                                                                </div>

                                                                                <div class="admin-form-group">
                                                                                    <label
                                                                                        for="version-title">Доповнення
                                                                                        до назви товару:</label>
                                                                                    <input type="text"
                                                                                        id="version-title"
                                                                                        name="product-title"
                                                                                        value="<%= verTitle %>"
                                                                                        class="<%= verTitleErr != null ? "
                                                                                        input-error" : "" %>"
                                                                                    placeholder="Наприклад: Cosmic Red /
                                                                                    256GB" />
                                                                                    <% if (verTitleErr !=null) { %>
                                                                                        <div class="field-error-msg">
                                                                                            &#9888; <%= verTitleErr %>
                                                                                        </div>
                                                                                        <% } %>
                                                                                </div>

                                                                                <div class="admin-form-group">
                                                                                    <label
                                                                                        for="version-description">Доповнення
                                                                                        до опису товару:</label>
                                                                                    <input type="text"
                                                                                        id="version-description"
                                                                                        name="product-description"
                                                                                        value="<%= verDesc %>"
                                                                                        class="<%= verDescErr != null ? "
                                                                                        input-error" : "" %>"
                                                                                    placeholder="Особливості цієї версії
                                                                                    товару" />
                                                                                    <% if (verDescErr !=null) { %>
                                                                                        <div class="field-error-msg">
                                                                                            &#9888; <%= verDescErr %>
                                                                                        </div>
                                                                                        <% } %>
                                                                                </div>

                                                                                <div class="admin-form-group">
                                                                                    <label for="version-slug">Доповнення
                                                                                        до Slug товару:</label>
                                                                                    <input type="text" id="version-slug"
                                                                                        name="product-slug"
                                                                                        value="<%= verSlug %>"
                                                                                        class="<%= verSlugErr != null ? "
                                                                                        input-error" : "" %>"
                                                                                    placeholder="cosmic-red" />
                                                                                    <% if (verSlugErr !=null) { %>
                                                                                        <div class="field-error-msg">
                                                                                            &#9888; <%= verSlugErr %>
                                                                                        </div>
                                                                                        <% } %>
                                                                                </div>

                                                                                <div class="row-3">
                                                                                    <div>
                                                                                        <label for="version-price">Ціна
                                                                                            версії:</label>
                                                                                        <input type="number" step="0.01"
                                                                                            min="0.01"
                                                                                            id="version-price"
                                                                                            name="product-price"
                                                                                            value="<%= verPrice %>"
                                                                                            class="<%= verPriceErr != null ? "
                                                                                            input-error" : "" %>"
                                                                                        placeholder="16999.00" />
                                                                                        <% if (verPriceErr !=null) { %>
                                                                                            <div
                                                                                                class="field-error-msg">
                                                                                                &#9888; <%= verPriceErr
                                                                                                    %>
                                                                                            </div>
                                                                                            <% } %>
                                                                                    </div>
                                                                                    <div>
                                                                                        <label
                                                                                            for="version-stock">Кількість
                                                                                            на складі:</label>
                                                                                        <input type="number" step="1"
                                                                                            id="version-stock"
                                                                                            name="product-stock"
                                                                                            value="<%= verStock %>"
                                                                                            class="<%= verStockErr != null ? "
                                                                                            input-error" : "" %>"
                                                                                        placeholder="5" />
                                                                                        <% if (verStockErr !=null) { %>
                                                                                            <div
                                                                                                class="field-error-msg">
                                                                                                &#9888; <%= verStockErr
                                                                                                    %>
                                                                                            </div>
                                                                                            <% } %>
                                                                                    </div>
                                                                                    <div>
                                                                                        <label
                                                                                            for="version-order">Порядок
                                                                                            у переліку:</label>
                                                                                        <input type="number" step="1"
                                                                                            id="version-order"
                                                                                            name="product-order"
                                                                                            value="<%= verOrder %>"
                                                                                            class="<%= verOrderErr != null ? "
                                                                                            input-error" : "" %>"
                                                                                        placeholder="1" />
                                                                                        <% if (verOrderErr !=null) { %>
                                                                                            <div
                                                                                                class="field-error-msg">
                                                                                                &#9888; <%= verOrderErr
                                                                                                    %>
                                                                                            </div>
                                                                                            <% } %>
                                                                                    </div>
                                                                                </div>

                                                                                <div class="admin-form-group">
                                                                                    <label
                                                                                        for="version-image">Зображення:</label>
                                                                                    <input type="file"
                                                                                        id="version-image"
                                                                                        name="product-image"
                                                                                        class="<%= verImageErr != null ? "
                                                                                        input-error" : "" %>" />
                                                                                    <% if (verImageErr !=null) { %>
                                                                                        <div class="field-error-msg">
                                                                                            &#9888; <%= verImageErr %>
                                                                                        </div>
                                                                                        <% } %>
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