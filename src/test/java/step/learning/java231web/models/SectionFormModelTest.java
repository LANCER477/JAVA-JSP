package step.learning.java231web.models;

import java.lang.reflect.Proxy;
import java.util.Map;
import org.apache.commons.fileupload2.core.FileItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SectionFormModelTest {

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
                        return "section-image";
                    }
                    if ("isFormField".equals(method.getName())) {
                        return false;
                    }
                    return null;
                }
        );
    }

    private SectionFormModel createValidModel() {
        return new SectionFormModel(
                null,
                "Побутова техніка",
                "Розділ надійної побутової техніки для дому",
                "home-appliances",
                "10",
                createMockFileItem("техніка.webp", 2048)
        );
    }

    @Test
    public void testValidSectionFormModelPassesValidation() {
        SectionFormModel model = createValidModel();
        assertTrue(model.isValid());
        assertTrue(model.getValidationErrors().isEmpty());
        assertDoesNotThrow(model::validate);
    }

    @Test
    public void testNullOrBlankTitleFails() {
        SectionFormModel model = createValidModel().setTitle(null);
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-title"));

        model.setTitle("   ");
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-title"));
    }

    @Test
    public void testShortTitleFails() {
        SectionFormModel model = createValidModel().setTitle("А");
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-title"));
    }

    @Test
    public void testNullOrBlankDescriptionFails() {
        SectionFormModel model = createValidModel().setDescription(null);
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-description"));

        model.setDescription("   ");
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-description"));
    }

    @Test
    public void testShortDescriptionFails() {
        SectionFormModel model = createValidModel().setDescription("Дім");
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-description"));
    }

    @Test
    public void testNullOrBlankSlugFails() {
        SectionFormModel model = createValidModel().setSlug(null);
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-slug"));

        model.setSlug("   ");
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-slug"));
    }

    @Test
    public void testInvalidSlugFormatFails() {
        String[] invalidSlugs = {
                "Home-Appliances", // великі літери
                "home appliances", // пробіли
                "home_appliances", // підкреслення
                "-home",           // дефіс спочатку
                "home-",           // дефіс в кінці
                "побутова-техніка" // кирилиця в слагу
        };
        for (String invalidSlug : invalidSlugs) {
            SectionFormModel model = createValidModel().setSlug(invalidSlug);
            assertFalse(model.isValid(), "Slug '" + invalidSlug + "' should be invalid");
            assertTrue(model.getValidationErrors().containsKey("section-slug"));
        }
    }

    @Test
    public void testValidSlugsPass() {
        String[] validSlugs = {
                "appliances",
                "home-appliances",
                "smart-tv-4k",
                "section123"
        };
        for (String validSlug : validSlugs) {
            SectionFormModel model = createValidModel().setSlug(validSlug);
            assertFalse(model.getValidationErrors().containsKey("section-slug"),
                    "Slug '" + validSlug + "' should be valid");
        }
    }

    @Test
    public void testNullOrBlankOrderFails() {
        SectionFormModel model = createValidModel().setOrder(null);
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-order"));

        model.setOrder("   ");
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-order"));
    }

    @Test
    public void testNegativeOrNonNumericOrderFails() {
        SectionFormModel model = createValidModel().setOrder("-5");
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-order"));

        model.setOrder("first");
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-order"));
    }

    @Test
    public void testMissingOrEmptyImageFails() {
        SectionFormModel model = createValidModel().setImageItem(null);
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-image"));

        model.setImageItem(createMockFileItem("empty.png", 0));
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-image"));
    }

    @Test
    public void testUnsupportedImageExtensionFails() {
        SectionFormModel model = createValidModel().setImageItem(createMockFileItem("document.pdf", 1024));
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-image"));

        model.setImageItem(createMockFileItem("script.exe", 2048));
        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("section-image"));
    }

    @Test
    public void testSupportedImageExtensionsPass() {
        String[] validExtensions = {"photo.jpg", "photo.jpeg", "photo.png", "photo.webp", "photo.gif", "photo.bmp"};
        for (String filename : validExtensions) {
            SectionFormModel model = createValidModel().setImageItem(createMockFileItem(filename, 1024));
            assertFalse(model.getValidationErrors().containsKey("section-image"),
                    "Extension of '" + filename + "' should be valid");
        }
    }

    @Test
    public void testValidateThrowsWhenInvalid() {
        SectionFormModel model = new SectionFormModel();
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testToFieldsMap() {
        SectionFormModel model = createValidModel().setParentId("parent-123");
        Map<String, String> map = model.toFieldsMap();
        assertEquals("parent-123", map.get("section-parent-id"));
        assertEquals("Побутова техніка", map.get("section-title"));
        assertEquals("home-appliances", map.get("section-slug"));
    }
}
