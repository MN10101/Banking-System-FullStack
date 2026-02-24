package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.entity.VerificationToken;
import com.nexgen.bankingsystem.repository.VerificationTokenRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class VerificationServiceTest {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(VerificationServiceTest.class);

    @Mock
    private VerificationTokenRepository tokenRepository;

    @InjectMocks
    private VerificationService verificationService;

    private ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> logAppender;
    private ch.qos.logback.classic.Logger logbackLogger;
    private Validator validator;
    private User user;
    private VerificationToken token;

    @BeforeEach
    public void setUp() {
        // Initialize validator for VerificationToken entity
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Set up test data based on provided user data
        user = new User();
        user.setId(1L);
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(new Date(1985 - 1900, 6, 19));
        user.setPhoneNumber("01639769764");
        user.setPassword("password123");
        user.setTaxNumber("MN12542BE22");
        user.setIdOrPassport("123456");
        user.setLastKnownIP("84.164.246.196");
        user.setEnabled(false);
        user.setRoles(new HashSet<>());
        user.setAccounts(new ArrayList<>());

        // Set up verification token
        token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(Date.from(Instant.now().plusSeconds(86400)));

        // Set up log appender
        logAppender = new ch.qos.logback.core.read.ListAppender<>();
        logAppender.start();
        logbackLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(VerificationService.class);
        logbackLogger.addAppender(logAppender);
    }

    @AfterEach
    public void tearDown() {
        reset(tokenRepository);
        logbackLogger.detachAppender(logAppender);
        logAppender.stop();
        logAppender.list.clear();
    }

    @Test
    void createsVerificationTokenSuccessfully() {
        // Arrange
        when(tokenRepository.save(any(VerificationToken.class))).thenReturn(token);

        // Act
        VerificationToken createdToken = verificationService.createVerificationToken(user);

        // Assert
        assertNotNull(createdToken);
        assertNotNull(createdToken.getToken());
        assertEquals(user, createdToken.getUser());
        assertNotNull(createdToken.getExpiryDate());
        assertFalse(createdToken.isExpired());
        verify(tokenRepository).save(any(VerificationToken.class));
        verifyNoMoreInteractions(tokenRepository);
        logger.info("Create verification token success test succeeded");
    }

    @Test
    void rejectsTokenCreationForNullUser() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationService.createVerificationToken(null);
        });
        assertEquals("User cannot be null", exception.getMessage());
        verifyNoInteractions(tokenRepository);
        logger.info("Create verification token null user test succeeded");
    }

    @Test
    void findsExistingVerificationToken() {
        // Arrange
        String tokenString = token.getToken();
        when(tokenRepository.findByToken(tokenString)).thenReturn(token);

        // Act
        VerificationToken foundToken = verificationService.getVerificationToken(tokenString);

        // Assert
        assertNotNull(foundToken);
        assertEquals(tokenString, foundToken.getToken());
        assertEquals(user, foundToken.getUser());
        verify(tokenRepository).findByToken(tokenString);
        verifyNoMoreInteractions(tokenRepository);
        logger.info("Get verification token found test succeeded");
    }

    @Test
    void returnsNullForNonexistentToken() {
        // Arrange
        String invalidToken = "invalid-token";
        when(tokenRepository.findByToken(invalidToken)).thenReturn(null);

        // Act
        VerificationToken foundToken = verificationService.getVerificationToken(invalidToken);

        // Assert
        assertNull(foundToken);
        verify(tokenRepository).findByToken(invalidToken);
        verifyNoMoreInteractions(tokenRepository);
        logger.info("Get verification token not found test succeeded");
    }

    @Test
    void calculatesCorrectExpiryDate() {
        // Act
        Date expiryDate = verificationService.calculateExpiryDate();

        // Assert
        assertNotNull(expiryDate);
        long expiryMillis = expiryDate.getTime();
        long nowMillis = System.currentTimeMillis();
        long expectedMillis = nowMillis + 86400 * 1000;
        assertTrue(Math.abs(expiryMillis - expectedMillis) < 1000);
        logger.info("Calculate expiry date test succeeded");
    }

    @Test
    void validatesCorrectTokenData() {
        // Act
        Set<ConstraintViolation<VerificationToken>> violations = validator.validate(token);

        // Assert
        assertThat(violations).isEmpty();
        logger.info("Verification token validation valid test succeeded");
    }

    @Test
    void detectsMissingTokenValue() {
        // Arrange
        token.setToken(null);

        // Act
        Set<ConstraintViolation<VerificationToken>> violations = validator.validate(token);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("Token cannot be null");
        logger.info("Verification token validation null token test succeeded");
    }

    @Test
    void detectsMissingExpiryDate() {
        // Arrange
        token.setExpiryDate(null);

        // Act
        Set<ConstraintViolation<VerificationToken>> violations = validator.validate(token);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("Expiry date cannot be null");
        logger.info("Verification token validation null expiry date test succeeded");
    }
}