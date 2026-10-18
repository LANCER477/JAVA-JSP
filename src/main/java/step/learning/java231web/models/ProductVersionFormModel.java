package step.learning.java231web.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.fileupload2.core.FileItem;
import step.learning.java231web.services.form.FormParseResult;

public class ProductVersionFormModel {
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Collections.unmodifiableList(
            Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp")
    );

    private String productId;
    private String title;
    private String description;
    private String slug;
    private String price;
    private String stock;
    private String order;
    private FileItem imageItem;

    public ProductVersionFormModel() {
    }

    public ProductVersionFormModel(String productId, String title, String description, String slug,
                                   String price, String stock, String order, FileItem imageItem) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.slug = slug;
        this.price = price;
        this.stock = stock;
        this.order = order;
        this.imageItem = imageItem;
    }

    public static ProductVersionFormModel fromFormParseResult(FormParseResult result) {
        if (result == null) {
            return new ProductVersionFormModel();
        }
        Map<String, String> fields = result.getFields();
        Map<String, FileItem> files = result.getFiles();

        ProductVersionFormModel model = new ProductVersionFormModel();
        if (fields != null) {
            model.setProductId(fields.get("product-id"));
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

    public String getProductId() {
        return productId;
    }

    public ProductVersionFormModel setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ProductVersionFormModel setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ProductVersionFormModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSlug() {
        return slug;
    }

    public ProductVersionFormModel setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public ProductVersionFormModel setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getStock() {
        return stock;
    }

    public ProductVersionFormModel setStock(String stock) {
        this.stock = stock;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public ProductVersionFormModel setOrder(String order) {
        this.order = order;
        return this;
    }

    public FileItem getImageItem() {
        return imageItem;
    }

    public ProductVersionFormModel setImageItem(FileItem imageItem) {
        this.imageItem = imageItem;
        return this;
    }

    public Map<String, String> getValidationErrors() {
        Map<String, String> errors = new LinkedHashMap<>();

        // 1. product-id
        if (productId == null || productId.trim().isEmpty()) {
            errors.put("product-id", "Оберіть товар для додавання версії");
        }

        // 2. product-title
        if (title == null || title.trim().isEmpty()) {
            errors.put("product-title", "Доповнення до назви не може бути порожнім");
        } else if (title.trim().length() < 2) {
            errors.put("product-title", "Доповнення до назви повинно містити щонайменше 2 символи");
        } else if (title.trim().length() > 255) {
            errors.put("product-title", "Доповнення до назви не може перевищувати 255 символів");
        }

        // 3. product-description
        if (description == null || description.trim().isEmpty()) {
            errors.put("product-description", "Доповнення до опису не може бути порожнім");
        } else if (description.trim().length() < 3) {
            errors.put("product-description", "Доповнення до опису повинно містити щонайменше 3 символи");
        }

        // 4. product-slug
        if (slug == null || slug.trim().isEmpty()) {
            errors.put("product-slug", "Доповнення до слагу не може бути порожнім");
        } else if (!SLUG_PATTERN.matcher(slug.trim()).matches()) {
            errors.put("product-slug", "Слаг може містити лише малі латинські літери, цифри та дефіси (наприклад: cosmic-red)");
        }

        // 5. product-price
        if (price == null || price.trim().isEmpty()) {
            errors.put("product-price", "Ціна версії не може бути порожньою");
        } else {
            try {
                double p = Double.parseDouble(price.trim().replace(',', '.'));
                if (p <= 0.0) {
                    errors.put("product-price", "Ціна повинна бути більшою за 0");
                }
            } catch (NumberFormatException ex) {
                errors.put("product-price", "Некоректний формат ціни (очікується число, наприклад: 16999.00)");
            }
        }

        // 6. product-stock
        if (stock == null || stock.trim().isEmpty()) {
            errors.put("product-stock", "Кількість на складі не може бути порожньою");
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
            errors.put("product-order", "Порядок у переліку не може бути порожнім");
        } else {
            try {
                int o = Integer.parseInt(order.trim());
                if (o < 0) {
                    errors.put("product-order", "Порядок не може бути від'ємним");
                }
            } catch (NumberFormatException ex) {
                errors.put("product-order", "Порядок повинен бути цілим числом");
            }
        }

        // 8. product-image (optional, but if provided, validate extension)
        if (imageItem != null && imageItem.getSize() > 0 && imageItem.getName() != null && !imageItem.getName().trim().isEmpty()) {
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
        if (productId != null) map.put("product-id", productId);
        if (title != null) map.put("product-title", title);
        if (description != null) map.put("product-description", description);
        if (slug != null) map.put("product-slug", slug);
        if (price != null) map.put("product-price", price);
        if (stock != null) map.put("product-stock", stock);
        if (order != null) map.put("product-order", order);
        return map;
    }
}
