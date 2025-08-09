package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserServiceImplTest.class);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private UserServiceImpl userService;

    private ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> logAppender;
    private ch.qos.logback.classic.Logger logbackLogger;
    private Validator validator;
    private User user;

    @BeforeEach
    public void setUp() {
        // Initialize validator for User entity
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

        // Set up log appender
        logAppender = new ch.qos.logback.core.read.ListAppender<>();
        logAppender.start();
        logbackLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);
        logbackLogger.addAppender(logAppender);
    }

    @AfterEach
    public void tearDown() {
        reset(userRepository, passwordEncoder, accountService);
        logbackLogger.detachAppender(logAppender);
        logAppender.stop();
        logAppender.list.clear();
    }

    @Test
    void registersNewUserSuccessfully() {
        // Arrange
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(accountService.createAccount(any(User.class), isNull(), eq(0.0))).thenReturn(new Account());

        // Act
        User savedUser = userService.registerUser(user);

        // Assert
        assertNotNull(savedUser);
        assertEquals("mn.de@outlook.com", savedUser.getEmail());
        assertEquals("$2a$10$encodedPassword", savedUser.getPassword());
        assertThat(savedUser.getRoles()).containsExactly("ROLE_USER");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(user);
        verify(accountService).createAccount(user, null, 0.0);
        verifyNoMoreInteractions(userRepository, passwordEncoder, accountService);
        assertThat(logAppender.list)
                .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage)
                .contains("Registering user: mn.de@outlook.com", "User registered: mn.de@outlook.com");
        logger.info("Register user success test succeeded");
    }

    @Test
    void preservesExistingRolesWhenRegisteringUser() {
        // Arrange
        user.getRoles().add("ROLE_ADMIN");
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(accountService.createAccount(any(User.class), isNull(), eq(0.0))).thenReturn(new Account());

        // Act
        User savedUser = userService.registerUser(user);

        // Assert
        assertNotNull(savedUser);
        assertEquals("mn.de@outlook.com", savedUser.getEmail());
        assertEquals("$2a$10$encodedPassword", savedUser.getPassword());
        assertThat(savedUser.getRoles()).containsExactly("ROLE_ADMIN");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(user);
        verify(accountService).createAccount(user, null, 0.0);
        verifyNoMoreInteractions(userRepository, passwordEncoder, accountService);
        assertThat(logAppender.list)
                .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage)
                .contains("Registering user: mn.de@outlook.com", "User registered: mn.de@outlook.com");
        logger.info("Register user with existing roles test succeeded");
    }

    @Test
    void findsExistingUserByEmail() {
        // Arrange
        when(userRepository.findByEmail("mn.de@outlook.com")).thenReturn(user);

        // Act
        Optional<User> foundUser = userService.findUserByEmail("mn.de@outlook.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("mn.de@outlook.com", foundUser.get().getEmail());
        verify(userRepository).findByEmail("mn.de@outlook.com");
        verifyNoMoreInteractions(userRepository);
        logger.info("Find user by email (found) test succeeded");
    }

    @Test
    void returnsEmptyForNonexistentEmail() {
        // Arrange
        when(userRepository.findByEmail("unknown@outlook.com")).thenReturn(null);

        // Act
        Optional<User> foundUser = userService.findUserByEmail("unknown@outlook.com");

        // Assert
        assertFalse(foundUser.isPresent());
        verify(userRepository).findByEmail("unknown@outlook.com");
        verifyNoMoreInteractions(userRepository);
        assertThat(logAppender.list)
                .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage)
                .contains("User not found for email: unknown@outlook.com");
        assertThat(logAppender.list)
                .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getLevel)
                .contains(ch.qos.logback.classic.Level.WARN);
        logger.info("Find user by email (not found) test succeeded");
    }

    @Test
    void retrievesAllUsersSuccessfully() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("mnde757@gmail.com");
        user2.setFirstName("Jonathan");
        user2.setLastName("Davies");
        user2.setAge(39);
        user2.setAddress("39 Park Avenue London SE82 5EY");
        user2.setBirthDate(new Date(1985 - 1900, 11, 1));
        user2.setPhoneNumber("+44123456789");
        user2.setPassword("$2a$10$9LTPTr/soRiN7ynBMVl64eWzwuFahaJ5r2ZbkbGRkvHsR/pbAxupq");
        user2.setTaxNumber("B4582L251455");
        user2.setIdOrPassport("586974");
        user2.setLastKnownIP("84.164.246.196");
        user2.setEnabled(false);
        user2.setRoles(new HashSet<>());
        user2.setAccounts(new ArrayList<>());
        List<User> users = Arrays.asList(user, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.findAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertThat(result).extracting("email").contains("mn.de@outlook.com", "mnde757@gmail.com");
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
        assertThat(logAppender.list)
                .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage)
                .contains("Retrieved 2 users");
        logger.info("Find all users test succeeded");
    }

    @Test
    void returnsEmptyListWhenNoUsersExist() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userService.findAllUsers();

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
        assertThat(logAppender.list)
                .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage)
                .contains("Retrieved 0 users");
        logger.info("Find all users (empty) test succeeded");
    }

    @Test
    void deletesUserSuccessfully() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        boolean result = userService.deleteUserById(1L);

        // Assert
        assertTrue(result);
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
        verifyNoMoreInteractions(userRepository);
        assertThat(logAppender.list)
                .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage)
                .contains("User deleted: 1");
        logger.info("Delete user success test succeeded");
    }

    @Test
    void failsToDeleteNonexistentUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act
        boolean result = userService.deleteUserById(1L);

        // Assert
        assertFalse(result);
        verify(userRepository).existsById(1L);
        verifyNoMoreInteractions(userRepository);
        assertThat(logAppender.list)
                .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage)
                .contains("User not found for deletion: 1");
        assertThat(logAppender.list)
                .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getLevel)
                .contains(ch.qos.logback.classic.Level.WARN);
        logger.info("Delete user not found test succeeded");
    }

    @Test
    void savesUserSuccessfully() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User savedUser = userService.saveUser(user);

        // Assert
        assertNotNull(savedUser);
        assertEquals("mn.de@outlook.com", savedUser.getEmail());
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository);
        assertThat(logAppender.list)
                .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage)
                .contains("User saved: mn.de@outlook.com");
        logger.info("Save user test succeeded");
    }

    @Test
    void validatesCorrectUserData() {
        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isEmpty();
        logger.info("User validation (valid) test succeeded");
    }

    @Test
    void detectsInvalidEmailFormat() {
        // Arrange
        user.setEmail("invalid-email");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("Invalid email format");
        logger.info("User validation (invalid email) test succeeded");
    }

    @Test
    void detectsUnderageUser() {
        // Arrange
        user.setAge(17);

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("Age must be at least 18");
        logger.info("User validation (under age) test succeeded");
    }

    @Test
    void detectsMissingFirstName() {
        // Arrange
        user.setFirstName("");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("First name is required");
        logger.info("User validation (blank first name) test succeeded");
    }

    @Test
    void detectsFutureBirthDate() {
        // Arrange
        user.setBirthDate(new Date(2026 - 1900, 0, 1));

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("Birth date must be in the past");
        logger.info("User validation (future birth date) test succeeded");
    }

    @Test
    void detectsInvalidPhoneNumberFormat() {
        // Arrange
        user.setPhoneNumber("123");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("Invalid phone number format");
        logger.info("User validation (invalid phone number) test succeeded");
    }
}