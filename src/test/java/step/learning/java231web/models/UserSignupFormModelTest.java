package step.learning.java231web.models;

import org.junit.jupiter.api.Test;
import step.learning.java231web.dao.UserDao;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserSignupFormModelTest {

    @Test
    public void testValidModelPassesValidation() {
        UserSignupFormModel model = new UserSignupFormModel(
                "Иван Иванов",
                "ivan.ivanov@example.com",
                "Password123",
                "Password123",
                true
        );

        assertTrue(model.isValid());
        assertTrue(model.getValidationErrors().isEmpty());
        assertDoesNotThrow(model::validate);
    }

    @Test
    public void testNullNameThrowsException() {
        UserSignupFormModel model = new UserSignupFormModel(null, "ivan@example.com");

        assertFalse(model.isValid());
        Map<String, String> errors = model.getValidationErrors();
        assertTrue(errors.containsKey("name"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testEmptyAndBlankNameFails() {
        UserSignupFormModel model1 = new UserSignupFormModel("", "ivan@example.com");
        UserSignupFormModel model2 = new UserSignupFormModel("   ", "ivan@example.com");

        assertFalse(model1.isValid());
        assertFalse(model2.isValid());
        assertTrue(model1.getValidationErrors().containsKey("name"));
        assertTrue(model2.getValidationErrors().containsKey("name"));
    }

    @Test
    public void testShortNameFails() {
        UserSignupFormModel model = new UserSignupFormModel("A", "ivan@example.com");

        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("name"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testNullEmailFails() {
        UserSignupFormModel model = new UserSignupFormModel("Олександр", null);

        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("email"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testInvalidEmailFormatFails() {
        String[] invalidEmails = {"notanemail", "user@", "@domain.com", "user@domain", "user@.com"};

        for (String invalidEmail : invalidEmails) {
            UserSignupFormModel model = new UserSignupFormModel("Олександр", invalidEmail);
            assertFalse(model.isValid(), "Email should be invalid: " + invalidEmail);
            assertTrue(model.getValidationErrors().containsKey("email"));
            assertThrows(IllegalArgumentException.class, model::validate);
        }
    }

    @Test
    public void testPasswordMismatchFails() {
        UserSignupFormModel model = new UserSignupFormModel(
                "Олександр",
                "oleksandr@example.com",
                "secret123",
                "secret321",
                true
        );

        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("repeatPassword"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testShortPasswordFails() {
        UserSignupFormModel model = new UserSignupFormModel(
                "Олександр",
                "oleksandr@example.com",
                "123",
                "123",
                true
        );

        assertFalse(model.isValid());
        assertTrue(model.getValidationErrors().containsKey("password"));
        assertThrows(IllegalArgumentException.class, model::validate);
    }

    @Test
    public void testUserDaoRejectsNullAndInvalidModelBeforeDatabaseCall() {
        UserDao userDao = new UserDao(null);

        assertThrows(IllegalArgumentException.class, () -> userDao.signupUser(null));

        UserSignupFormModel invalidModel = new UserSignupFormModel("", "invalid-email");
        assertThrows(IllegalArgumentException.class, () -> userDao.signupUser(invalidModel));
    }
}
