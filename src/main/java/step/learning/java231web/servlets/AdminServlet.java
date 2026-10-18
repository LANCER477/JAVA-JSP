package step.learning.java231web.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload2.core.FileItem;
import step.learning.java231web.data.dao.ProductDao;
import step.learning.java231web.data.dao.ProductSectionDao;
import step.learning.java231web.data.dao.ProductVersionDao;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.data.dto.ProductVersion;
import step.learning.java231web.data.models.AdminProductViewModel;
import step.learning.java231web.models.ProductFormModel;
import step.learning.java231web.models.ProductVersionFormModel;
import step.learning.java231web.models.SectionFormModel;
import step.learning.java231web.services.db.IDbService;
import step.learning.java231web.services.form.FormParseResult;
import step.learning.java231web.services.form.IFormParseService;
import step.learning.java231web.services.storage.IStorageService;

/**
 * Сервлет адміністративної сторінки з підтримкою внутрішніх маршрутів
 * @author samoylenko_d
 */
@Singleton
public class AdminServlet extends HttpServlet {
    private final IDbService dbService;
    private final IFormParseService formParseService;
    private final IStorageService storageService;
    private final ProductSectionDao productSectionDao;
    private final ProductDao productDao;
    private final ProductVersionDao productVersionDao;
    private final Logger logger = Logger.getLogger(AdminServlet.class.getName());

    @Inject
    public AdminServlet(IDbService dbService, IFormParseService formParseService, IStorageService storageService,
                        ProductSectionDao productSectionDao, ProductDao productDao, ProductVersionDao productVersionDao) {
        this.dbService = dbService;
        this.formParseService = formParseService;
        this.storageService = storageService;
        this.productSectionDao = productSectionDao;
        this.productDao = productDao;
        this.productVersionDao = productVersionDao;
    }

    public AdminServlet(IDbService dbService, IFormParseService formParseService, IStorageService storageService, ProductSectionDao productSectionDao) {
        this(dbService, formParseService, storageService, productSectionDao, null, null);
    }

    public AdminServlet(IDbService dbService, IFormParseService formParseService, IStorageService storageService) {
        this(dbService, formParseService, storageService, null, null, null);
    }

    public AdminServlet(IDbService dbService) {
        this(dbService, null, null, null, null, null);
    }

    public AdminServlet() {
        this(null, null, null, null, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && (pathInfo.equalsIgnoreCase("/product") || pathInfo.equalsIgnoreCase("/product/")
                || pathInfo.equalsIgnoreCase("/product-version") || pathInfo.equalsIgnoreCase("/product-version/"))) {
            AdminProductViewModel viewModel = new AdminProductViewModel();
            if (productSectionDao != null) {
                viewModel.setProductSections(productSectionDao.getAllSections());
            }
            if (productDao != null) {
                viewModel.setProducts(productDao.getAllProducts());
            }

            if (formParseService != null) {
                try {
                    FormParseResult form = formParseService.parse(req);
                    Map<String, String> fields = form.getFields();
                    Map<String, FileItem> files = form.getFiles();

                    boolean isVersion = pathInfo.toLowerCase().contains("version")
                            || fields.containsKey("product-id");

                    if (isVersion) {
                        req.setAttribute("formType", "version");
                        ProductVersionFormModel model = ProductVersionFormModel.fromFormParseResult(form);
                        Map<String, String> validationErrors = model.getValidationErrors();

                        if (!validationErrors.isEmpty()) {
                            req.setAttribute("validationErrors", validationErrors);
                            req.setAttribute("formFields", fields);
                            req.setAttribute("formError", "Виявлено помилки валідації! Будь ласка, виправте виділені поля форми.");
                            viewModel.setExceptionMessage("Виявлено помилки валідації: " + String.join(", ", validationErrors.keySet()));
                        } else {
                            FileItem image = model.getImageItem();
                            String savedFileName = null;
                            if (image != null && storageService != null && image.getSize() > 0) {
                                savedFileName = storageService.save(image);
                            }

                            ProductVersion version = new ProductVersion();
                            version.setId(UUID.randomUUID().toString());
                            version.setProductId(model.getProductId());
                            version.setTitle(model.getTitle());
                            version.setDescription(model.getDescription());
                            version.setSlug(model.getSlug());
                            try {
                                String priceStr = model.getPrice();
                                if (priceStr != null && !priceStr.trim().isEmpty()) {
                                    version.setPrice(Double.parseDouble(priceStr.trim().replace(',', '.')));
                                }
                            } catch (Exception ignored) {}
                            try {
                                String stockStr = model.getStock();
                                if (stockStr != null && !stockStr.trim().isEmpty()) {
                                    version.setStock(Integer.parseInt(stockStr.trim()));
                                }
                            } catch (Exception ignored) {}
                            try {
                                String orderStr = model.getOrder();
                                if (orderStr != null && !orderStr.trim().isEmpty()) {
                                    version.setOrder(Integer.parseInt(orderStr.trim()));
                                }
                            } catch (Exception ignored) {}
                            version.setImageUrl(savedFileName);

                            if (productVersionDao != null) {
                                productVersionDao.addProductVersion(version);
                            }

                            req.setAttribute("formMessage", "Дані версії товару успішно передано, провалідовано та збережено! "
                                    + (image != null && image.getSize() > 0 ? "Файл: " + image.getName() + " (" + image.getSize() + " байт)" : ""));
                            req.setAttribute("formFields", fields);
                            if (savedFileName != null) {
                                req.setAttribute("savedFileName", savedFileName);
                            }
                            viewModel.setOkMessage("Версію товару успішно додано: " + (version.getTitle() != null ? version.getTitle() : ""));
                        }
                    } else {
                        req.setAttribute("formType", "product");
                        ProductFormModel model = ProductFormModel.fromFormParseResult(form);
                        Map<String, String> validationErrors = model.getValidationErrors();

                        if (!validationErrors.isEmpty()) {
                            req.setAttribute("validationErrors", validationErrors);
                            req.setAttribute("formFields", fields);
                            req.setAttribute("formError", "Виявлено помилки валідації! Будь ласка, виправте виділені поля форми.");
                            viewModel.setExceptionMessage("Виявлено помилки валідації: " + String.join(", ", validationErrors.keySet()));
                        } else {
                            FileItem image = model.getImageItem();
                            String savedFileName = null;
                            if (image != null && storageService != null && image.getSize() > 0) {
                                savedFileName = storageService.save(image);
                            }

                            if (productDao != null) {
                                Product product = new Product();
                                product.setId(UUID.randomUUID().toString());
                                product.setSectionId(model.getSectionId());
                                product.setTitle(model.getTitle());
                                product.setDescription(model.getDescription());
                                product.setSlug(model.getSlug());
                                try {
                                    product.setPrice(Double.parseDouble(model.getPrice().trim()));
                                } catch (Exception ignored) {}
                                try {
                                    product.setStock(Integer.parseInt(model.getStock().trim()));
                                } catch (Exception ignored) {}
                                try {
                                    product.setOrder(Integer.parseInt(model.getOrder().trim()));
                                } catch (Exception ignored) {}
                                product.setImageUrl(savedFileName);
                                productDao.addProduct(product);
                            }

                            req.setAttribute("formMessage", "Дані товару успішно передано, провалідовано та збережено! "
                                    + (image != null ? "Файл: " + image.getName() + " (" + image.getSize() + " байт)" : ""));
                            req.setAttribute("formFields", fields);
                            req.setAttribute("savedFileName", savedFileName);
                            viewModel.setOkMessage("Товар успішно додано: " + model.getTitle());
                        }
                    }
                } catch (Exception ex) {
                    req.setAttribute("formError", ex.getMessage());
                    viewModel.setExceptionMessage(ex.getMessage());
                }
            }

            req.setAttribute("viewModel", viewModel);
            doGet(req, resp);
            return;
        }

        if (pathInfo != null && (pathInfo.equalsIgnoreCase("/section") || pathInfo.equalsIgnoreCase("/section/"))) {
            if (formParseService != null) {
                try {
                    FormParseResult form = formParseService.parse(req);
                    Map<String, String> fields = form.getFields();
                    Map<String, FileItem> files = form.getFiles();

                    SectionFormModel model = SectionFormModel.fromFormParseResult(form);
                    Map<String, String> validationErrors = model.getValidationErrors();

                    if (!validationErrors.isEmpty()) {
                        req.setAttribute("validationErrors", validationErrors);
                        req.setAttribute("formFields", fields);
                        req.setAttribute("formError", "Виявлено помилки валідації! Будь ласка, виправте виділені поля форми.");
                    } else {
                        FileItem image = model.getImageItem();
                        String savedFileName = null;
                        if (image != null && storageService != null && image.getSize() > 0) {
                            savedFileName = storageService.save(image);
                        }

                        if (productSectionDao != null) {
                            ProductSection section = new ProductSection();
                            section.setId(UUID.randomUUID().toString());
                            section.setParentId(model.getParentId() != null && !model.getParentId().trim().isEmpty() ? model.getParentId() : null);
                            section.setTitle(model.getTitle());
                            section.setDescription(model.getDescription());
                            section.setSlug(model.getSlug());
                            section.setImageUrl(savedFileName);
                            try {
                                section.setOrder(Integer.parseInt(model.getOrder().trim()));
                            } catch (Exception ignored) {}
                            productSectionDao.addProductSection(section);
                        }

                        req.setAttribute("formMessage", "Дані розділу успішно передано, провалідовано та збережено! "
                                + (image != null ? "Файл: " + image.getName() + " (" + image.getSize() + " байт)" : ""));
                        req.setAttribute("formFields", fields);
                        req.setAttribute("savedFileName", savedFileName);
                    }
                } catch (Exception ex) {
                    req.setAttribute("formError", ex.getMessage());
                }
            }
            doGet(req, resp);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        doGet(req, resp);
    }

    /*
    Д.З. Реалізувати сторінку 404 для внутрішніх маршрутів адміністративної сторінки.
    */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("servlet", "Admin");

        String pathInfo = req.getPathInfo();
        String adminPage;
        String adminView;

        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/") || pathInfo.equalsIgnoreCase("/home") || pathInfo.equalsIgnoreCase("/home/")) {
            adminPage = "home";
            adminView = "/admin_home.jsp";
        }
        else if (pathInfo.equalsIgnoreCase("/product") || pathInfo.equalsIgnoreCase("/product/")
                || pathInfo.equalsIgnoreCase("/product-version") || pathInfo.equalsIgnoreCase("/product-version/")) {
            adminPage = "product";
            adminView = "/admin_product.jsp";
            req.setAttribute("sections", getProductSections());

            AdminProductViewModel viewModel = (AdminProductViewModel) req.getAttribute("viewModel");
            if (viewModel == null) {
                viewModel = new AdminProductViewModel();
            }
            if (productSectionDao != null) {
                viewModel.setProductSections(productSectionDao.getAllSections());
            }
            if (productDao != null) {
                viewModel.setProducts(productDao.getAllProducts());
            }
            req.setAttribute("viewModel", viewModel);
        }
        else if (pathInfo.equalsIgnoreCase("/section") || pathInfo.equalsIgnoreCase("/section/")) {
            adminPage = "section";
            adminView = "/admin_section.jsp";
            req.setAttribute("sections", getProductSections());
        }
        else {
            // Внутрішній маршрут адміністративної сторінки не знайдено -> 404
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            adminPage = "404";
            adminView = "/admin_404.jsp";
            req.setAttribute("invalidRoute", pathInfo);
        }

        req.setAttribute("adminPage", adminPage);
        req.setAttribute("adminView", adminView);

        jakarta.servlet.RequestDispatcher dispatcher = req.getServletContext() != null
                ? req.getServletContext().getRequestDispatcher("/index.jsp")
                : null;
        if (dispatcher == null) {
            dispatcher = req.getRequestDispatcher("/index.jsp");
        }

        jakarta.servlet.ServletRequest unwrapped = req;
        while (unwrapped instanceof jakarta.servlet.ServletRequestWrapper) {
            unwrapped = ((jakarta.servlet.ServletRequestWrapper) unwrapped).getRequest();
        }

        dispatcher.forward(unwrapped != null ? unwrapped : req, resp);
    }

    private List<String> getProductSections() {
        List<String> defaultSections = Arrays.asList("Garminfull", "Телевізори", "Консолі", "Б/у Apple");
        if (dbService == null) {
            return defaultSections;
        }

        List<String> list = new ArrayList<>();
        try {
            Connection connection = dbService.getConnection();
            if (connection != null && !connection.isClosed()) {
                String sql = "SELECT product_section_slug FROM product_sections ORDER BY product_section_order, product_section_slug";
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        String sectionName = rs.getString(1);
                        if (sectionName != null && !sectionName.trim().isEmpty()) {
                            list.add(sectionName.trim());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "Could not load sections from DB, using fallback: " + ex.getMessage());
        }

        return list.isEmpty() ? defaultSections : list;
    }
}
