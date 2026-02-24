package com.nexgen.bankingsystem.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class UserEntityTest {

    private Validator validator;
    private Date validBirthDate;
    private Date futureBirthDate;

    @BeforeEach
    public void setUp() {
        // Initialize the validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Initialize valid and invalid birth dates
        Calendar calendar = Calendar.getInstance();
        calendar.set(1985, Calendar.JULY, 19);
        validBirthDate = calendar.getTime();

        calendar.set(2026, Calendar.JANUARY, 1);
        futureBirthDate = calendar.getTime();
    }

    @Test
    public void validatesUser() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("01639769764");
        user.setPassword("$2a$10$ss8P7V5PvKapGVK2stAGa.ewVNTTush.sBU6aaczzqtsgt2t.bsaW");
        user.setTaxNumber("MN12542BE22");
        user.setIdOrPassport("123456");
        user.setEnabled(false);
        user.setRoles(new HashSet<>(Set.of("ROLE_USER")));

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertTrue(violations.isEmpty(), "Valid user should have no validation errors");
    }

    @Test
    public void failsIfEmailNull() {
        // Arrange
        User user = new User();
        user.setEmail(null);
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("01639769764");
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for null email");
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Email cannot be null", violation.getMessage());
    }

    @Test
    public void failsIfEmailWrongFormat() {
        // Arrange
        User user = new User();
        user.setEmail("invalid-email");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("01639769764");
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for invalid email format");
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Invalid email format", violation.getMessage());
    }

    @Test
    public void failsIfFirstNameBlank() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("01639769764");
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for blank first name");
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("firstName", violation.getPropertyPath().toString());
        assertEquals("First name is required", violation.getMessage());
    }

    @Test
    public void failsIfLastNameBlank() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("01639769764");
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for blank last name");
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("lastName", violation.getPropertyPath().toString());
        assertEquals("Last name is required", violation.getMessage());
    }

    @Test
    public void failsIfAgeUnder18() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(17);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("01639769764");
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for age under 18");
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("age", violation.getPropertyPath().toString());
        assertEquals("Age must be at least 18", violation.getMessage());
    }

    @Test
    public void failsIfAddressBlank() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("01639769764");
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for blank address");
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("address", violation.getPropertyPath().toString());
        assertEquals("Address cannot be empty", violation.getMessage());
    }

    @Test
    public void failsIfBirthDateNull() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(null);
        user.setPhoneNumber("01639769764");
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for null birth date");
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("birthDate", violation.getPropertyPath().toString());
        assertEquals("Birth date cannot be null", violation.getMessage());
    }

    @Test
    public void failsIfBirthDateFuture() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(futureBirthDate);
        user.setPhoneNumber("01639769764");
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for future birth date");
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("birthDate", violation.getPropertyPath().toString());
        assertEquals("Birth date must be in the past", violation.getMessage());
    }

    @Test
    public void failsIfPhoneNumberBlank() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("");
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(2, violations.size(), "Should have two validation errors for blank phone number");
        Set<String> violationMessages = new HashSet<>();
        for (ConstraintViolation<User> violation : violations) {
            violationMessages.add(violation.getMessage());
            assertEquals("phoneNumber", violation.getPropertyPath().toString());
        }
        assertTrue(violationMessages.contains("Phone number is required"));
        assertTrue(violationMessages.contains("Invalid phone number format"));
    }

    @Test
    public void failsIfPhoneNumberWrongFormat() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("123");
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for invalid phone number format");
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("phoneNumber", violation.getPropertyPath().toString());
        assertEquals("Invalid phone number format", violation.getMessage());
    }

    @Test
    public void failsIfPasswordBlank() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("01639769764");
        user.setPassword("");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(2, violations.size(), "Should have two validation errors for blank password");
        Set<String> violationMessages = new HashSet<>();
        for (ConstraintViolation<User> violation : violations) {
            violationMessages.add(violation.getMessage());
            assertEquals("password", violation.getPropertyPath().toString());
        }
        assertTrue(violationMessages.contains("Password cannot be empty"));
        assertTrue(violationMessages.contains("Password must be at least 8 characters long"));
    }

    @Test
    public void failsIfPasswordTooShort() {
        // Arrange
        User user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(validBirthDate);
        user.setPhoneNumber("01639769764");
        user.setPassword("short");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for short password");
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals("Password must be at least 8 characters long", violation.getMessage());
    }

    @Test
    public void failsIfMultipleInvalidFields() {
        // Arrange
        User user = new User();
        user.setEmail("invalid-email");
        user.setFirstName("");
        user.setLastName("");
        user.setAge(17);
        user.setAddress("");
        user.setBirthDate(futureBirthDate);
        user.setPhoneNumber("123");
        user.setPassword("short");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertEquals(8, violations.size(), "Should have eight validation errors");
        Set<String> violationMessages = new HashSet<>();
        for (ConstraintViolation<User> violation : violations) {
            violationMessages.add(violation.getMessage());
        }
        assertTrue(violationMessages.contains("Invalid email format"));
        assertTrue(violationMessages.contains("First name is required"));
        assertTrue(violationMessages.contains("Last name is required"));
        assertTrue(violationMessages.contains("Age must be at least 18"));
        assertTrue(violationMessages.contains("Address cannot be empty"));
        assertTrue(violationMessages.contains("Birth date must be in the past"));
        assertTrue(violationMessages.contains("Invalid phone number format"));
        assertTrue(violationMessages.contains("Password must be at least 8 characters long"));
    }
}