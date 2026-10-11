package step.learning.java231web.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.fileupload2.core.FileItem;
import step.learning.java231web.services.form.FormParseResult;

public class SectionFormModel {
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Collections.unmodifiableList(
            Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp")
    );

    private String parentId;
    private String title;
    private String description;
    private String slug;
    private String order;
    private FileItem imageItem;

    public SectionFormModel() {
    }

    public SectionFormModel(String parentId, String title, String description, String slug,
                            String order, FileItem imageItem) {
        this.parentId = parentId;
        this.title = title;
        this.description = description;
        this.slug = slug;
        this.order = order;
        this.imageItem = imageItem;
    }

    public static SectionFormModel fromFormParseResult(FormParseResult result) {
        if (result == null) {
            return new SectionFormModel();
        }
        Map<String, String> fields = result.getFields();
        Map<String, FileItem> files = result.getFiles();

        SectionFormModel model = new SectionFormModel();
        if (fields != null) {
            model.setParentId(fields.get("section-parent-id"));
            model.setTitle(fields.get("section-title"));
            model.setDescription(fields.get("section-description"));
            model.setSlug(fields.get("section-slug"));
            model.setOrder(fields.get("section-order"));
        }
        if (files != null) {
            model.setImageItem(files.get("section-image"));
        }
        return model;
    }

    public String getParentId() {
        return parentId;
    }

    public SectionFormModel setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public SectionFormModel setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SectionFormModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSlug() {
        return slug;
    }

    public SectionFormModel setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public SectionFormModel setOrder(String order) {
        this.order = order;
        return this;
    }

    public FileItem getImageItem() {
        return imageItem;
    }

    public SectionFormModel setImageItem(FileItem imageItem) {
        this.imageItem = imageItem;
        return this;
    }

    public Map<String, String> getValidationErrors() {
        Map<String, String> errors = new LinkedHashMap<>();

        // 1. section-title
        if (title == null || title.trim().isEmpty()) {
            errors.put("section-title", "Назва розділу не може бути порожньою");
        } else if (title.trim().length() < 2) {
            errors.put("section-title", "Назва розділу повинна містити щонайменше 2 символи");
        } else if (title.trim().length() > 128) {
            errors.put("section-title", "Назва розділу не може перевищувати 128 символів");
        }

        // 2. section-description
        if (description == null || description.trim().isEmpty()) {
            errors.put("section-description", "Опис розділу не може бути порожнім");
        } else if (description.trim().length() < 5) {
            errors.put("section-description", "Опис розділу повинен містити щонайменше 5 символів");
        }

        // 3. section-slug
        if (slug == null || slug.trim().isEmpty()) {
            errors.put("section-slug", "Слаг розділу не може бути порожнім");
        } else if (!SLUG_PATTERN.matcher(slug).matches()) {
            errors.put("section-slug", "Слаг може містити лише малі латинські літери, цифри та дефіси (наприклад: smart-gadgets)");
        }

        // 4. section-order
        if (order == null || order.trim().isEmpty()) {
            errors.put("section-order", "Порядок відображення не може бути порожнім");
        } else {
            try {
                int o = Integer.parseInt(order.trim());
                if (o < 0) {
                    errors.put("section-order", "Порядок відображення не може бути від'ємним");
                }
            } catch (NumberFormatException ex) {
                errors.put("section-order", "Порядок відображення повинен бути цілим числом");
            }
        }

        // 5. section-image
        if (imageItem == null || imageItem.getSize() == 0 || imageItem.getName() == null || imageItem.getName().trim().isEmpty()) {
            errors.put("section-image", "Необхідно обрати файл зображення розділу");
        } else {
            String name = imageItem.getName();
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex < 0) {
                errors.put("section-image", "Файл зображення повинен мати розширення (.jpg, .png тощо)");
            } else {
                String ext = name.substring(dotIndex).toLowerCase();
                if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
                    errors.put("section-image", "Непідтримуваний формат файлу. Дозволені: .jpg, .jpeg, .png, .webp, .gif, .bmp");
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
        if (parentId != null) map.put("section-parent-id", parentId);
        if (title != null) map.put("section-title", title);
        if (description != null) map.put("section-description", description);
        if (slug != null) map.put("section-slug", slug);
        if (order != null) map.put("section-order", order);
        return map;
    }
}
