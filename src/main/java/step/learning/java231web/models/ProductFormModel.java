package step.learning.java231web.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.fileupload2.core.FileItem;
import step.learning.java231web.services.form.FormParseResult;

public class ProductFormModel {
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Collections.unmodifiableList(
            Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp")
    );

    private String sectionId;
    private String title;
    private String description;
    private String slug;
    private String price;
    private String stock;
    private String order;
    private FileItem imageItem;

    public ProductFormModel() {
    }

    public ProductFormModel(String sectionId, String title, String description, String slug,
                            String price, String stock, String order, FileItem imageItem) {
        this.sectionId = sectionId;
        this.title = title;
        this.description = description;
        this.slug = slug;
        this.price = price;
        this.stock = stock;
        this.order = order;
        this.imageItem = imageItem;
    }

    public static ProductFormModel fromFormParseResult(FormParseResult result) {
        if (result == null) {
            return new ProductFormModel();
        }
        Map<String, String> fields = result.getFields();
        Map<String, FileItem> files = result.getFiles();

        ProductFormModel model = new ProductFormModel();
        if (fields != null) {
            model.setSectionId(fields.get("product-section-id"));
            model.setTitle(fields.get("product-title"));
            model.setDescription(fields.get("product-description"));
            model.setSlug(fields.get("product-slug"));
            model.setPrice(fields.get("product-price"));
            model.setStock(fields.get("product-stock"));
            model.setOrder(fields.get("product-order"));
        }
        if (files != null) {
            model.setImageItem(files.get("product-image"));
        }
        return model;
    }

    public String getSectionId() {
        return sectionId;
    }

    public ProductFormModel setSectionId(String sectionId) {
        this.sectionId = sectionId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ProductFormModel setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ProductFormModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSlug() {
        return slug;
    }

    public ProductFormModel setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public ProductFormModel setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getStock() {
        return stock;
    }

    public ProductFormModel setStock(String stock) {
        this.stock = stock;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public ProductFormModel setOrder(String order) {
        this.order = order;
        return this;
    }

    public FileItem getImageItem() {
        return imageItem;
    }

    public ProductFormModel setImageItem(FileItem imageItem) {
        this.imageItem = imageItem;
        return this;
    }

    public Map<String, String> getValidationErrors() {
        Map<String, String> errors = new LinkedHashMap<>();

        // 1. product-section-id
        if (sectionId == null || sectionId.trim().isEmpty()) {
            errors.put("product-section-id", "Оберіть розділ товару");
        }

        // 2. product-title
        if (title == null || title.trim().isEmpty()) {
            errors.put("product-title", "Назва товару не може бути порожньою");
        } else if (title.trim().length() < 2) {
            errors.put("product-title", "Назва товару повинна містити щонайменше 2 символи");
        } else if (title.trim().length() > 255) {
            errors.put("product-title", "Назва товару не може перевищувати 255 символів");
        }

        // 3. product-description
        if (description == null || description.trim().isEmpty()) {
            errors.put("product-description", "Опис товару не може бути порожнім");
        } else if (description.trim().length() < 5) {
            errors.put("product-description", "Опис товару повинен містити щонайменше 5 символів");
        }

        // 4. product-slug
        if (slug == null || slug.trim().isEmpty()) {
            errors.put("product-slug", "Слаг товару не може бути порожнім");
        } else if (!SLUG_PATTERN.matcher(slug).matches()) {
            errors.put("product-slug", "Слаг може містити лише малі латинські літери, цифри та дефіси (наприклад: smart-gadget)");
        }

        // 5. product-price
        if (price == null || price.trim().isEmpty()) {
            errors.put("product-price", "Ціна товару не може бути порожньою");
        } else {
            try {
                double p = Double.parseDouble(price.trim().replace(',', '.'));
                if (p <= 0.0) {
                    errors.put("product-price", "Ціна повинна бути більшою за 0");
                }
            } catch (NumberFormatException ex) {
                errors.put("product-price", "Некоректний формат ціни (очікується число, наприклад: 15999.00)");
            }
        }

        // 6. product-stock
        if (stock == null || stock.trim().isEmpty()) {
            errors.put("product-stock", "Кількість товару не може бути порожньою");
        } else {
            try {
                int s = Integer.parseInt(stock.trim());
                if (s < 0) {
                    errors.put("product-stock", "Кількість на складі не може бути від'ємною");
                }
            } catch (NumberFormatException ex) {
                errors.put("product-stock", "Кількість повинна бути цілим числом");
            }
        }

        // 7. product-order
        if (order == null || order.trim().isEmpty()) {
            errors.put("product-order", "Порядок відображення не може бути порожнім");
        } else {
            try {
                int o = Integer.parseInt(order.trim());
                if (o < 0) {
                    errors.put("product-order", "Порядок відображення не може бути від'ємним");
                }
            } catch (NumberFormatException ex) {
                errors.put("product-order", "Порядок відображення повинен бути цілим числом");
            }
        }

        // 8. product-image
        if (imageItem == null || imageItem.getSize() == 0 || imageItem.getName() == null || imageItem.getName().trim().isEmpty()) {
            errors.put("product-image", "Необхідно обрати файл зображення товару");
        } else {
            String name = imageItem.getName();
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex < 0) {
                errors.put("product-image", "Файл зображення повинен мати розширення (.jpg, .png тощо)");
            } else {
                String ext = name.substring(dotIndex).toLowerCase();
                if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
                    errors.put("product-image", "Непідтримуваний формат файлу. Дозволені: .jpg, .jpeg, .png, .webp, .gif, .bmp");
                }
            }
        }

        return Collections.unmodifiableMap(errors);
    }

    public boolean isValid() {
        return getValidationErrors().isEmpty();
    }

    public void validate() throws IllegalArgumentException {
        Map<String, String> errors = getValidationErrors();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors.values()));
        }
    }

    public Map<String, String> toFieldsMap() {
        Map<String, String> map = new LinkedHashMap<>();
        if (sectionId != null) map.put("product-section-id", sectionId);
        if (title != null) map.put("product-title", title);
        if (description != null) map.put("product-description", description);
        if (slug != null) map.put("product-slug", slug);
        if (price != null) map.put("product-price", price);
        if (stock != null) map.put("product-stock", stock);
        if (order != null) map.put("product-order", order);
        return map;
    }
}
