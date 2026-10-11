package step.learning.java231web.models;

import java.lang.reflect.Proxy;
import java.util.Map;
import org.apache.commons.fileupload2.core.FileItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductFormModelTest {

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

    private ProductFormModel createValidModel() {
        return new ProductFormModel(
                "3a27db1d-d97e-406f-a36a-148b4f724a6e",
                "Смарт-ґаджет \"Їжачок-1\"",
                "Надійний український пристрій з пам'яттю та швидким зв'язком",
                "smart-gadget-yizhachok",
                "15999.00",
                "25",
                "1",
                createMockFileItem("Зображення_товару.png", 2048)
        );
    }

    @Test
    public void testValidProductFormModelPassesValidation() {
        ProductFormModel model = createValidModel();
        assertTrue(model.isValid());
        assertTrue(model.getValidationErrors().isEmpty());
        assertDoesNotThrow(model::validate);
    }

    @Test
    public void testNullOrBlankSectionIdFails() {
        ProductFormModel model = createValidModel().setSectionId(null);
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("product-section-id"));

        model.setSectionId("   ");
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("product-section-id"));
    }

    @Test
    public void testNullBlankOrShortTitleFails() {
        ProductFormModel model1 = createValidModel().setTitle(null);
        assertFalse(model1.isValid());
        assertTrue(model1.getValidationErrors().containsKey("product-title"));

        ProductFormModel model2 = createValidModel().setTitle(" ");
        assertFalse(model2.isValid());
        assertTrue(model2.getValidationErrors().containsKey("product-title"));

        ProductFormModel model3 = createValidModel().setTitle("A");
        assertFalse(model3.isValid());
        assertTrue(model3.getValidationErrors().containsKey("product-title"));
    }

    @Test
    public void testNullBlankOrShortDescriptionFails() {
        ProductFormModel model1 = createValidModel().setDescription(null);
        assertFalse(model1.isValid());
        assertTrue(model1.getValidationErrors().containsKey("product-description"));

        ProductFormModel model2 = createValidModel().setDescription("1234");
        assertFalse(model2.isValid());
        assertTrue(model2.getValidationErrors().containsKey("product-description"));
    }

    @Test
    public void testInvalidSlugFormatFails() {
        String[] invalidSlugs = {"", "   ", "Invalid Slug", "slug_with_space ", "СЛАГ", "slug@domain"};
        for (String invalidSlug : invalidSlugs) {
            ProductFormModel model = createValidModel().setSlug(invalidSlug);
            assertFalse(model.isValid(), "Slug should be invalid: " + invalidSlug);
            assertTrue(model.getValidationErrors().containsKey("product-slug"));
        }
    }

    @Test
    public void testValidSlugFormatPasses() {
        String[] validSlugs = {"smart-gadget", "iphone-16-pro", "product123", "gadget-2026"};
        for (String validSlug : validSlugs) {
            ProductFormModel model = createValidModel().setSlug(validSlug);
            assertNull(model.getValidationErrors().get("product-slug"), "Slug should be valid: " + validSlug);
        }
    }

    @Test
    public void testInvalidPriceFails() {
        String[] invalidPrices = {null, "", "   ", "abc", "-10", "0", "-0.01"};
        for (String price : invalidPrices) {
            ProductFormModel model = createValidModel().setPrice(price);
            assertFalse(model.isValid(), "Price should be invalid: " + price);
            assertTrue(model.getValidationErrors().containsKey("product-price"));
        }
    }

    @Test
    public void testValidPricePasses() {
        String[] validPrices = {"100", "0.01", "15999.99", "12,50"};
        for (String price : validPrices) {
            ProductFormModel model = createValidModel().setPrice(price);
            assertNull(model.getValidationErrors().get("product-price"), "Price should be valid: " + price);
        }
    }

    @Test
    public void testInvalidStockFails() {
        String[] invalidStocks = {null, "", "   ", "abc", "-1", "10.5"};
        for (String stock : invalidStocks) {
            ProductFormModel model = createValidModel().setStock(stock);
            assertFalse(model.isValid(), "Stock should be invalid: " + stock);
            assertTrue(model.getValidationErrors().containsKey("product-stock"));
        }
    }

    @Test
    public void testValidStockPasses() {
        ProductFormModel model = createValidModel().setStock("0");
        assertNull(model.getValidationErrors().get("product-stock"));

        model.setStock("100");
        assertNull(model.getValidationErrors().get("product-stock"));
    }

    @Test
    public void testInvalidOrderFails() {
        String[] invalidOrders = {null, "", "   ", "xyz", "-5"};
        for (String order : invalidOrders) {
            ProductFormModel model = createValidModel().setOrder(order);
            assertFalse(model.isValid(), "Order should be invalid: " + order);
            assertTrue(model.getValidationErrors().containsKey("product-order"));
        }
    }

    @Test
    public void testMissingOrInvalidImageFails() {
        ProductFormModel modelNoImage = createValidModel().setImageItem(null);
        assertFalse(modelNoImage.isValid());
        assertTrue(modelNoImage.getValidationErrors().containsKey("product-image"));

        ProductFormModel modelEmptyImage = createValidModel().setImageItem(createMockFileItem("test.jpg", 0));
        assertFalse(modelEmptyImage.isValid());
        assertTrue(modelEmptyImage.getValidationErrors().containsKey("product-image"));

        ProductFormModel modelNoExt = createValidModel().setImageItem(createMockFileItem("image_without_extension", 100));
        assertFalse(modelNoExt.isValid());
        assertTrue(modelNoExt.getValidationErrors().containsKey("product-image"));

        ProductFormModel modelDisallowedExt = createValidModel().setImageItem(createMockFileItem("script.exe", 100));
        assertFalse(modelDisallowedExt.isValid());
        assertTrue(modelDisallowedExt.getValidationErrors().containsKey("product-image"));
    }

    @Test
    public void testValidImageExtensionsPass() {
        String[] validImages = {"photo.jpg", "image.jpeg", "picture.PNG", "banner.webp", "icon.gif"};
        for (String imageName : validImages) {
            ProductFormModel model = createValidModel().setImageItem(createMockFileItem(imageName, 1024));
            assertNull(model.getValidationErrors().get("product-image"), "Image should be valid: " + imageName);
        }
    }

    @Test
    public void testToFieldsMapPreservesInput() {
        ProductFormModel model = createValidModel();
        Map<String, String> map = model.toFieldsMap();
        assertEquals("Смарт-ґаджет \"Їжачок-1\"", map.get("product-title"));
        assertEquals("15999.00", map.get("product-price"));
        assertEquals("25", map.get("product-stock"));
    }
}
