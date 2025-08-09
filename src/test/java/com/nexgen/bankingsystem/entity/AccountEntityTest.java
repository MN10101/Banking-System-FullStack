package com.nexgen.bankingsystem.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class AccountEntityTest {

    private Validator validator;
    private User user;

    @BeforeEach
    public void setUp() {
        // Initialize the validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Initialize a sample user based on database data
        user = new User();
        user.setId(1L);
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(new Date(85, 6, 19));
        user.setPhoneNumber("01639769764");
        user.setPassword("$2a$10$ss8P7V5PvKapGVK2stAGa.ewVNTTush.sBU6aaczzqtsgt2t.bsaW");
        user.setTaxNumber("MN12542BE22");
        user.setIdOrPassport("123456");
        user.setEnabled(false);
        user.setRoles(new HashSet<>(Set.of("ROLE_USER")));
    }

    @Test
    public void validatesAccount() {
        // Arrange
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("1234567890");
        account.setIban("DE12345678901234567890");
        account.setBalance(1000.0);
        account.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertTrue(violations.isEmpty(), "Valid account should have no validation errors");
    }

    @Test
    public void failsIfUserNull() {
        // Arrange
        Account account = new Account();
        account.setUser(null);
        account.setAccountNumber("1234567890");
        account.setIban("DE12345678901234567890");
        account.setBalance(1000.0);
        account.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for null user");
        ConstraintViolation<Account> violation = violations.iterator().next();
        assertEquals("user", violation.getPropertyPath().toString());
        assertEquals("User cannot be null", violation.getMessage());
    }

    @Test
    public void failsIfAccountNumberBlank() {
        // Arrange
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("");
        account.setIban("DE12345678901234567890");
        account.setBalance(1000.0);
        account.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertEquals(2, violations.size(), "Should have two validation errors for blank account number");
        Set<String> violationMessages = new HashSet<>();
        for (ConstraintViolation<Account> violation : violations) {
            violationMessages.add(violation.getMessage());
            assertEquals("accountNumber", violation.getPropertyPath().toString());
        }
        assertTrue(violationMessages.contains("Account number cannot be blank"));
        assertTrue(violationMessages.contains("Account number must be between 10 and 12 characters"));
    }

    @Test
    public void failsIfAccountNumberTooShort() {
        // Arrange
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("123");
        account.setIban("DE12345678901234567890");
        account.setBalance(1000.0);
        account.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for short account number");
        ConstraintViolation<Account> violation = violations.iterator().next();
        assertEquals("accountNumber", violation.getPropertyPath().toString());
        assertEquals("Account number must be between 10 and 12 characters", violation.getMessage());
    }

    @Test
    public void failsIfAccountNumberTooLong() {
        // Arrange
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("1234567890123");
        account.setIban("DE12345678901234567890");
        account.setBalance(1000.0);
        account.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for long account number");
        ConstraintViolation<Account> violation = violations.iterator().next();
        assertEquals("accountNumber", violation.getPropertyPath().toString());
        assertEquals("Account number must be between 10 and 12 characters", violation.getMessage());
    }

    @Test
    public void failsIfIbanBlank() {
        // Arrange
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("1234567890");
        account.setIban("");
        account.setBalance(1000.0);
        account.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertEquals(2, violations.size(), "Should have two validation errors for blank IBAN");
        Set<String> violationMessages = new HashSet<>();
        for (ConstraintViolation<Account> violation : violations) {
            violationMessages.add(violation.getMessage());
            assertEquals("iban", violation.getPropertyPath().toString());
        }
        assertTrue(violationMessages.contains("IBAN cannot be blank"));
        assertTrue(violationMessages.contains("IBAN must be a valid German IBAN starting with 'DE' and 22 characters long"));
    }

    @Test
    public void failsIfIbanWrongFormat() {
        // Arrange
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("1234567890");
        account.setIban("US12345678901234567890");
        account.setBalance(1000.0);
        account.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for invalid IBAN format");
        ConstraintViolation<Account> violation = violations.iterator().next();
        assertEquals("iban", violation.getPropertyPath().toString());
        assertEquals("IBAN must be a valid German IBAN starting with 'DE' and 22 characters long", violation.getMessage());
    }

    @Test
    public void failsIfBalanceNegative() {
        // Arrange
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("1234567890");
        account.setIban("DE12345678901234567890");
        account.setBalance(-100.0);
        account.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for negative balance");
        ConstraintViolation<Account> violation = violations.iterator().next();
        assertEquals("balance", violation.getPropertyPath().toString());
        assertEquals("Balance cannot be negative", violation.getMessage());
    }

    @Test
    public void failsIfCurrencyBlank() {
        // Arrange
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("1234567890");
        account.setIban("DE12345678901234567890");
        account.setBalance(1000.0);
        account.setCurrency("");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertEquals(2, violations.size(), "Should have two validation errors for blank currency");
        Set<String> violationMessages = new HashSet<>();
        for (ConstraintViolation<Account> violation : violations) {
            violationMessages.add(violation.getMessage());
            assertEquals("currency", violation.getPropertyPath().toString());
        }
        assertTrue(violationMessages.contains("Currency cannot be blank"));
        assertTrue(violationMessages.contains("Currency must be a valid 3-letter ISO code"));
    }

    @Test
    public void failsIfCurrencyWrongFormat() {
        // Arrange
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("1234567890");
        account.setIban("DE12345678901234567890");
        account.setBalance(1000.0);
        account.setCurrency("EU");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for invalid currency format");
        ConstraintViolation<Account> violation = violations.iterator().next();
        assertEquals("currency", violation.getPropertyPath().toString());
        assertEquals("Currency must be a valid 3-letter ISO code", violation.getMessage());
    }

    @Test
    public void failsIfMultipleInvalidFields() {
        // Arrange
        Account account = new Account();
        account.setUser(null);
        account.setAccountNumber("123");
        account.setIban("INVALID");
        account.setBalance(-100.0);
        account.setCurrency("EU");

        // Act
        Set<ConstraintViolation<Account>> violations = validator.validate(account);

        // Assert
        assertEquals(5, violations.size(), "Should have five validation errors");
        Set<String> violationMessages = new HashSet<>();
        for (ConstraintViolation<Account> violation : violations) {
            violationMessages.add(violation.getMessage());
        }
        assertTrue(violationMessages.contains("User cannot be null"));
        assertTrue(violationMessages.contains("Account number must be between 10 and 12 characters"));
        assertTrue(violationMessages.contains("IBAN must be a valid German IBAN starting with 'DE' and 22 characters long"));
        assertTrue(violationMessages.contains("Balance cannot be negative"));
        assertTrue(violationMessages.contains("Currency must be a valid 3-letter ISO code"));
    }
}