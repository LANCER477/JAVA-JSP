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
import java.util.logging.Level;
import java.util.logging.Logger;
import step.learning.java231web.services.db.IDbService;

import step.learning.java231web.services.form.FormParseResult;
import step.learning.java231web.services.form.IFormParseService;
import step.learning.java231web.services.storage.IStorageService;
import step.learning.java231web.models.ProductFormModel;
import step.learning.java231web.models.SectionFormModel;
import step.learning.java231web.data.dao.ProductSectionDao;
import step.learning.java231web.data.dto.ProductSection;
import java.util.UUID;
import org.apache.commons.fileupload2.core.FileItem;

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
    private final Logger logger = Logger.getLogger(AdminServlet.class.getName());

    @Inject
    public AdminServlet(IDbService dbService, IFormParseService formParseService, IStorageService storageService, ProductSectionDao productSectionDao) {
        this.dbService = dbService;
        this.formParseService = formParseService;
        this.storageService = storageService;
        this.productSectionDao = productSectionDao;
    }

    public AdminServlet(IDbService dbService, IFormParseService formParseService, IStorageService storageService) {
        this(dbService, formParseService, storageService, null);
    }

    public AdminServlet(IDbService dbService) {
        this(dbService, null, null, null);
    }

    public AdminServlet() {
        this(null, null, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && (pathInfo.equalsIgnoreCase("/product") || pathInfo.equalsIgnoreCase("/product/"))) {
            if (formParseService != null) {
                try {
                    FormParseResult form = formParseService.parse(req);
                    Map<String, String> fields = form.getFields();
                    Map<String, FileItem> files = form.getFiles();

                    ProductFormModel model = ProductFormModel.fromFormParseResult(form);
                    Map<String, String> validationErrors = model.getValidationErrors();

                    if (!validationErrors.isEmpty()) {
                        req.setAttribute("validationErrors", validationErrors);
                        req.setAttribute("formFields", fields);
                        req.setAttribute("formError", "Виявлено помилки валідації! Будь ласка, виправте виділені поля форми.");
                    } else {
                        FileItem image = model.getImageItem();
                        String savedFileName = null;
                        if (image != null && storageService != null) {
                            savedFileName = storageService.save(image);
                        }

                        req.setAttribute("formMessage", "Дані товару успішно передано, провалідовано та збережено! "
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
                        if (image != null && storageService != null) {
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
        else if (pathInfo.equalsIgnoreCase("/product") || pathInfo.equalsIgnoreCase("/product/")) {
            adminPage = "product";
            adminView = "/admin_product.jsp";
            req.setAttribute("sections", getProductSections());
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
