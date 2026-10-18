package step.learning.java231web.models;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.fileupload2.core.FileItem;
import org.junit.jupiter.api.Test;
import step.learning.java231web.services.form.FormParseResult;

import static org.junit.jupiter.api.Assertions.*;

public class ProductVersionFormModelTest {

    private FileItem createMockFileItem(final String name, final long size) {
        return (FileItem) Proxy.newProxyInstance(
                FileItem.class.getClassLoader(),
                new Class<?>[]{FileItem.class},
                (proxy, method, args) -> {
                    if ("getName".equals(method.getName())) {
                        return name;
                    }
                    if ("getSize".equals(method.getName())) {
                        return size;
                    }
                    if ("getFieldName".equals(method.getName())) {
                        return "product-image";
                    }
                    if ("isFormField".equals(method.getName())) {
                        return false;
                    }
                    return null;
                }
        );
    }

    private ProductVersionFormModel createValidModel() {
        return new ProductVersionFormModel(
                "3a27db1d-d97e-406f-a36a-148b4f724a6e",
                "Cosmic Red / 256GB",
                "Особлива версія у кольорі Cosmic Red з подвійним об'ємом пам'яті",
                "cosmic-red-256gb",
                "16999.00",
                "10",
                "1",
                createMockFileItem("version_image.png", 2048)
        );
    }

    @Test
    public void testValidProductVersionFormModelPassesValidation() {
        ProductVersionFormModel model = createValidModel();
        assertTrue(model.isValid());
        assertTrue(model.getValidationErrors().isEmpty());
        assertDoesNotThrow(model::validate);
    }

    @Test
    public void testValidModelWithoutImagePasses() {
        ProductVersionFormModel model = createValidModel();
        model.setImageItem(null);
        assertTrue(model.isValid());
        assertTrue(model.getValidationErrors().isEmpty());
    }

    @Test
    public void testNullOrBlankProductIdFails() {
        ProductVersionFormModel model = createValidModel().setProductId(null);
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("product-id"));

        model.setProductId("   ");
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("product-id"));
    }

    @Test
    public void testNullBlankOrShortTitleFails() {
        ProductVersionFormModel model1 = createValidModel().setTitle(null);
        assertFalse(model1.isValid());
        assertTrue(model1.getValidationErrors().containsKey("product-title"));

        ProductVersionFormModel model2 = createValidModel().setTitle(" ");
        assertFalse(model2.isValid());
        assertTrue(model2.getValidationErrors().containsKey("product-title"));

        ProductVersionFormModel model3 = createValidModel().setTitle("A");
        assertFalse(model3.isValid());
        assertTrue(model3.getValidationErrors().containsKey("product-title"));
    }

    @Test
    public void testNullBlankOrShortDescriptionFails() {
        ProductVersionFormModel model1 = createValidModel().setDescription(null);
        assertFalse(model1.isValid());
        assertTrue(model1.getValidationErrors().containsKey("product-description"));

        ProductVersionFormModel model2 = createValidModel().setDescription("  ");
        assertFalse(model2.isValid());
        assertTrue(model2.getValidationErrors().containsKey("product-description"));

        ProductVersionFormModel model3 = createValidModel().setDescription("ab");
        assertFalse(model3.isValid());
        assertTrue(model3.getValidationErrors().containsKey("product-description"));
    }

    @Test
    public void testInvalidSlugFormatFails() {
        String[] invalidSlugs = {"", "   ", "Invalid Slug", "slug_with_space ", "СЛАГ", "slug@domain"};
        for (String invalidSlug : invalidSlugs) {
            ProductVersionFormModel model = createValidModel().setSlug(invalidSlug);
            assertFalse(model.isValid(), "Slug should be invalid: " + invalidSlug);
            assertTrue(model.getValidationErrors().containsKey("product-slug"));
        }
    }

    @Test
    public void testInvalidPriceFails() {
        String[] invalidPrices = {null, "", "   ", "abc", "-10", "0", "0.00", "-0.01"};
        for (String invalidPrice : invalidPrices) {
            ProductVersionFormModel model = createValidModel().setPrice(invalidPrice);
            assertFalse(model.isValid(), "Price should be invalid: " + invalidPrice);
            assertTrue(model.getValidationErrors().containsKey("product-price"));
        }
    }

    @Test
    public void testValidPricesWithCommaOrDot() {
        ProductVersionFormModel model = createValidModel().setPrice("199.99");
        assertTrue(model.isValid());

        model.setPrice("199,99");
        assertTrue(model.isValid());
    }

    @Test
    public void testInvalidStockFails() {
        String[] invalidStocks = {null, "", "   ", "abc", "-1", "-50", "12.5"};
        for (String invalidStock : invalidStocks) {
            ProductVersionFormModel model = createValidModel().setStock(invalidStock);
            assertFalse(model.isValid(), "Stock should be invalid: " + invalidStock);
            assertTrue(model.getValidationErrors().containsKey("product-stock"));
        }
    }

    @Test
    public void testZeroStockIsValid() {
        ProductVersionFormModel model = createValidModel().setStock("0");
        assertTrue(model.isValid());
    }

    @Test
    public void testInvalidOrderFails() {
        String[] invalidOrders = {null, "", "   ", "abc", "-1", "1.5"};
        for (String invalidOrder : invalidOrders) {
            ProductVersionFormModel model = createValidModel().setOrder(invalidOrder);
            assertFalse(model.isValid(), "Order should be invalid: " + invalidOrder);
            assertTrue(model.getValidationErrors().containsKey("product-order"));
        }
    }

    @Test
    public void testInvalidImageExtensionFails() {
        FileItem exeFile = createMockFileItem("malware.exe", 1024);
        ProductVersionFormModel model = createValidModel().setImageItem(exeFile);
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("product-image"));
    }

    @Test
    public void testFromFormParseResultAndToFieldsMap() {
        Map<String, String> fields = new HashMap<>();
        fields.put("product-id", "test-prod-uuid");
        fields.put("product-title", "Midnight Black");
        fields.put("product-description", "Deep black color finish");
        fields.put("product-slug", "midnight-black");
        fields.put("product-price", "999.00");
        fields.put("product-stock", "5");
        fields.put("product-order", "2");

        FormParseResult result = new FormParseResult() {
            @Override
            public Map<String, String> getFields() {
                return fields;
            }

            @Override
            public Map<String, FileItem> getFiles() {
                return java.util.Collections.emptyMap();
            }
        };

        ProductVersionFormModel model = ProductVersionFormModel.fromFormParseResult(result);
        assertEquals("test-prod-uuid", model.getProductId());
        assertEquals("Midnight Black", model.getTitle());
        assertEquals("Deep black color finish", model.getDescription());
        assertEquals("midnight-black", model.getSlug());
        assertEquals("999.00", model.getPrice());
        assertEquals("5", model.getStock());
        assertEquals("2", model.getOrder());

        Map<String, String> map = model.toFieldsMap();
        assertEquals("test-prod-uuid", map.get("product-id"));
        assertEquals("Midnight Black", map.get("product-title"));
    }
}
