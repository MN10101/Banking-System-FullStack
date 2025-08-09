package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.dto.TransferRequest;
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
import org.slf4j.LoggerFactory;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TransferServiceTest.class);

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransferService transferService;

    private ListAppender<ILoggingEvent> logAppender;
    private Logger logbackLogger;
    private Validator validator;

    @BeforeEach
    public void setUp() {
        // Initialize validator for TransferRequest
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Set up log appender to capture logs
        logAppender = new ListAppender<>();
        logAppender.start();
        logbackLogger = (Logger) LoggerFactory.getLogger(TransferService.class);
        logbackLogger.addAppender(logAppender);
    }

    @AfterEach
    public void tearDown() {
        reset(accountService);
        logbackLogger.detachAppender(logAppender);
        logAppender.stop();
        logAppender.list.clear();
    }

    @Test
    void successfullyProcessesValidTransfer() {
        // Arrange
        String fromIban = "DE54500202220172720185";
        String toIban = "DE52500202220657984061";
        double amount = 1000.0;
        String currency = "EUR";
        when(accountService.transferMoney(fromIban, toIban, amount, currency)).thenReturn(true);

        // Act
        transferService.processTransfer(fromIban, toIban, amount, currency);

        // Assert
        verify(accountService).transferMoney(fromIban, toIban, amount, currency);
        verifyNoMoreInteractions(accountService);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Transfer of 1000.0 EUR completed from " + fromIban + " to " + toIban);
        logger.info("Transfer success test succeeded");
    }

    @Test
    void failsTransferWithInsufficientFunds() {
        // Arrange
        String fromIban = "DE54500202220172720185";
        String toIban = "DE52500202220657984061";
        double amount = 40000.0;
        String currency = "EUR";
        when(accountService.transferMoney(fromIban, toIban, amount, currency)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transferService.processTransfer(fromIban, toIban, amount, currency));
        assertEquals("Transfer failed due to insufficient funds or invalid IBAN.", exception.getMessage());
        verify(accountService).transferMoney(fromIban, toIban, amount, currency);
        verifyNoMoreInteractions(accountService);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Transfer failed from " + fromIban + " to " + toIban + ": insufficient funds or invalid IBAN");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        logger.info("Insufficient funds test succeeded: {}", exception.getMessage());
    }

    @Test
    void rejectsTransferWithInvalidFromIban() {
        // Arrange
        String fromIban = "INVALID_IBAN";
        String toIban = "DE52500202220657984061";
        double amount = 1000.0;
        String currency = "EUR";
        when(accountService.transferMoney(fromIban, toIban, amount, currency)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transferService.processTransfer(fromIban, toIban, amount, currency));
        assertEquals("Transfer failed due to insufficient funds or invalid IBAN.", exception.getMessage());
        verify(accountService).transferMoney(fromIban, toIban, amount, currency);
        verifyNoMoreInteractions(accountService);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Transfer failed from " + fromIban + " to " + toIban + ": insufficient funds or invalid IBAN");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        logger.info("Invalid from IBAN test succeeded: {}", exception.getMessage());
    }

    @Test
    void rejectsTransferWithInvalidToIban() {
        // Arrange
        String fromIban = "DE54500202220172720185";
        String toIban = "INVALID_IBAN";
        double amount = 1000.0;
        String currency = "EUR";
        when(accountService.transferMoney(fromIban, toIban, amount, currency)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transferService.processTransfer(fromIban, toIban, amount, currency));
        assertEquals("Transfer failed due to insufficient funds or invalid IBAN.", exception.getMessage());
        verify(accountService).transferMoney(fromIban, toIban, amount, currency);
        verifyNoMoreInteractions(accountService);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Transfer failed from " + fromIban + " to " + toIban + ": insufficient funds or invalid IBAN");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        logger.info("Invalid to IBAN test succeeded: {}", exception.getMessage());
    }

    @Test
    void rejectsTransferWithNegativeAmount() {
        // Arrange
        String fromIban = "DE54500202220172720185";
        String toIban = "DE52500202220657984061";
        double amount = -1000.0;
        String currency = "EUR";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transferService.processTransfer(fromIban, toIban, amount, currency));
        assertEquals("Amount must be positive", exception.getMessage());
        verifyNoInteractions(accountService);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid transfer amount: " + amount);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        logger.info("Negative amount test succeeded: {}", exception.getMessage());
    }

    @Test
    void rejectsTransferWithZeroAmount() {
        // Arrange
        String fromIban = "DE54500202220172720185";
        String toIban = "DE52500202220657984061";
        double amount = 0.0;
        String currency = "EUR";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transferService.processTransfer(fromIban, toIban, amount, currency));
        assertEquals("Amount must be positive", exception.getMessage());
        verifyNoInteractions(accountService);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid transfer amount: " + amount);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        logger.info("Zero amount test succeeded: {}", exception.getMessage());
    }

    @Test
    void rejectsTransferWithInvalidCurrency() {
        // Arrange
        String fromIban = "DE54500202220172720185";
        String toIban = "DE52500202220657984061";
        double amount = 1000.0;
        String currency = "INVALID";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transferService.processTransfer(fromIban, toIban, amount, currency));
        assertEquals("Currency must be a valid 3-letter ISO code", exception.getMessage());
        verifyNoInteractions(accountService);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid currency code: " + currency);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        logger.info("Invalid currency test succeeded: {}", exception.getMessage());
    }

    @Test
    void validatesCorrectTransferRequest() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromIban("DE54500202220172720185");
        request.setToIban("DE52500202220657984061");
        request.setAmount(1000.0);
        request.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
        logger.info("Valid TransferRequest test succeeded");
    }

    @Test
    void detectsBlankFromIbanInTransferRequest() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromIban("");
        request.setToIban("DE52500202220657984061");
        request.setAmount(1000.0);
        request.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("From IBAN must not be blank");
        logger.info("Invalid fromIban TransferRequest test succeeded");
    }

    @Test
    void detectsNegativeAmountInTransferRequest() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromIban("DE54500202220172720185");
        request.setToIban("DE52500202220657984061");
        request.setAmount(-1000.0);
        request.setCurrency("EUR");

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("Amount must be positive");
        logger.info("Negative amount TransferRequest test succeeded");
    }

    @Test
    void detectsBlankCurrencyInTransferRequest() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromIban("DE54500202220172720185");
        request.setToIban("DE52500202220657984061");
        request.setAmount(1000.0);
        request.setCurrency("");

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("Currency must not be blank");
        logger.info("Invalid currency TransferRequest test succeeded");
    }
}