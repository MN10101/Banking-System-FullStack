package com.nexgen.bankingsystem.entity;

import com.nexgen.bankingsystem.repository.VerificationTokenRepository;
import com.nexgen.bankingsystem.service.VerificationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenEntityTest {

    private Validator validator;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @InjectMocks
    private VerificationService verificationService;

    private User validUser;
    private Date validExpiryDate;
    private Date expiredDate;

    @BeforeEach
    public void setUp() {
        // Initialize the validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Initialize valid user based on database data
        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("mn.de@outlook.com");
        validUser.setFirstName("Mahmoud");
        validUser.setLastName("Najmeh");
        validUser.setAge(39);
        validUser.setAddress("Müllenhoffstr.16, 10967 Berlin");
        validUser.setBirthDate(new Date(85, Calendar.JULY, 19));
        validUser.setPhoneNumber("01639769764");
        validUser.setPassword("$2a$10$ss8P7V5PvKapGVK2stAGa.ewVNTTush.sBU6aaczzqtsgt2t.bsaW");
        validUser.setTaxNumber("MN12542BE22");
        validUser.setIdOrPassport("123456");
        validUser.setEnabled(false);
        validUser.setRoles(new HashSet<>(Set.of("ROLE_USER")));

        // Initialize valid and expired dates
        validExpiryDate = Date.from(Instant.now().plusSeconds(86400));
        expiredDate = Date.from(Instant.now().minusSeconds(86400));
    }

    // Entity Validation Tests
    @Test
    public void passWhenVerificationTokenIsValid() {
        // Arrange
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(validUser);
        token.setExpiryDate(validExpiryDate);

        // Act
        Set<ConstraintViolation<VerificationToken>> violations = validator.validate(token);

        // Assert
        assertTrue(violations.isEmpty(), "Valid verification token should have no validation errors");
    }

    @Test
    public void failWhenTokenIsNull() {
        // Arrange
        VerificationToken token = new VerificationToken();
        token.setToken(null);
        token.setUser(validUser);
        token.setExpiryDate(validExpiryDate);

        // Act
        Set<ConstraintViolation<VerificationToken>> violations = validator.validate(token);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for null token");
        ConstraintViolation<VerificationToken> violation = violations.iterator().next();
        assertEquals("token", violation.getPropertyPath().toString());
        assertEquals("Token cannot be null", violation.getMessage());
    }

    @Test
    public void allowNullUser() {
        // Arrange
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(null);
        token.setExpiryDate(validExpiryDate);

        // Act
        Set<ConstraintViolation<VerificationToken>> violations = validator.validate(token);

        // Assert
        assertTrue(violations.isEmpty(), "Null user should not trigger Bean Validation error");
    }

    @Test
    public void failWhenExpiryDateIsNull() {
        // Arrange
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(validUser);
        token.setExpiryDate(null);

        // Act
        Set<ConstraintViolation<VerificationToken>> violations = validator.validate(token);

        // Assert
        assertEquals(1, violations.size(), "Should have one validation error for null expiry date");
        ConstraintViolation<VerificationToken> violation = violations.iterator().next();
        assertEquals("expiryDate", violation.getPropertyPath().toString());
        assertEquals("Expiry date cannot be null", violation.getMessage());
    }

    @Test
    public void failWhenTokenAndExpiryDateAreInvalid() {
        // Arrange
        VerificationToken token = new VerificationToken();
        token.setToken(null);
        token.setUser(null);
        token.setExpiryDate(null);

        // Act
        Set<ConstraintViolation<VerificationToken>> violations = validator.validate(token);

        // Assert
        assertEquals(2, violations.size(), "Should have two validation errors for token and expiryDate");
        Set<String> violationMessages = new HashSet<>();
        for (ConstraintViolation<VerificationToken> violation : violations) {
            violationMessages.add(violation.getMessage());
        }
        assertTrue(violationMessages.contains("Token cannot be null"));
        assertTrue(violationMessages.contains("Expiry date cannot be null"));
    }

    // Service Layer Tests
    @Test
    public void createVerificationTokenForValidUser() {
        // Arrange
        String expectedToken = UUID.randomUUID().toString();
        VerificationToken expected = new VerificationToken();
        expected.setToken(expectedToken);
        expected.setUser(validUser);
        expected.setExpiryDate(validExpiryDate);

        when(tokenRepository.save(any(VerificationToken.class))).thenReturn(expected);

        // Act
        VerificationToken result = verificationService.createVerificationToken(validUser);

        // Assert
        assertNotNull(result, "Created token should not be null");
        assertEquals(expectedToken, result.getToken());
        assertEquals(validUser, result.getUser());
        assertEquals(validExpiryDate, result.getExpiryDate());
        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    public void throwExceptionWhenCreatingTokenWithNullUser() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationService.createVerificationToken(null);
        });
        assertEquals("User cannot be null", exception.getMessage());
        verify(tokenRepository, never()).save(any(VerificationToken.class));
    }

    @Test
    public void returnVerificationTokenWhenTokenExists() {
        // Arrange
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken expected = new VerificationToken();
        expected.setToken(tokenValue);
        expected.setUser(validUser);
        expected.setExpiryDate(validExpiryDate);

        when(tokenRepository.findByToken(tokenValue)).thenReturn(expected);

        // Act
        VerificationToken result = verificationService.getVerificationToken(tokenValue);

        // Assert
        assertNotNull(result, "Found token should not be null");
        assertEquals(tokenValue, result.getToken());
        assertEquals(validUser, result.getUser());
        assertEquals(validExpiryDate, result.getExpiryDate());
        verify(tokenRepository, times(1)).findByToken(tokenValue);
    }

    @Test
    public void returnNullWhenTokenDoesNotExist() {
        // Arrange
        String tokenValue = UUID.randomUUID().toString();
        when(tokenRepository.findByToken(tokenValue)).thenReturn(null);

        // Act
        VerificationToken result = verificationService.getVerificationToken(tokenValue);

        // Assert
        assertNull(result, "Non-existent token should return null");
        verify(tokenRepository, times(1)).findByToken(tokenValue);
    }

    @Test
    public void returnFalseWhenTokenIsNotExpired() {
        // Arrange
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(validUser);
        token.setExpiryDate(validExpiryDate);

        // Act
        boolean isExpired = token.isExpired();

        // Assert
        assertFalse(isExpired, "Token with future expiry date should not be expired");
    }

    @Test
    public void returnTrueWhenTokenIsExpired() {
        // Arrange
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(validUser);
        token.setExpiryDate(expiredDate);

        // Act
        boolean isExpired = token.isExpired();

        // Assert
        assertTrue(isExpired, "Token with past expiry date should be expired");
    }
}
