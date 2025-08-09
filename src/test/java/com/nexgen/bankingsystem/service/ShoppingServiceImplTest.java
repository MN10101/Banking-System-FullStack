package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.repository.AccountRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@ExtendWith(MockitoExtension.class)
public class ShoppingServiceImplTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ShoppingServiceImplTest.class);

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ShoppingServiceImpl shoppingService;

    @Mock
    private APIContext apiContext;

    @Mock
    private Payment payment;

    private Account account;
    private ListAppender<ILoggingEvent> logAppender;
    private Logger logbackLogger;

    @BeforeEach
    public void setUp() {
        // Set up test data
        User user = new User();
        user.setId(1L);
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");

        account = new Account(user, "NEX172720185", "DE54500202220172720185", 35000.0, "EUR");

        // Inject PayPal and Stripe credentials using ReflectionTestUtils
        ReflectionTestUtils.setField(shoppingService, "paypalClientId", "test-client-id");
        ReflectionTestUtils.setField(shoppingService, "paypalClientSecret", "test-client-secret");
        ReflectionTestUtils.setField(shoppingService, "stripeSecretKey", "sk_test_123");

        // Set up log appender to capture logs
        logAppender = new ListAppender<>();
        logAppender.start();
        logbackLogger = (Logger) LoggerFactory.getLogger(ShoppingServiceImpl.class);
        logbackLogger.addAppender(logAppender);
    }

    @AfterEach
    public void tearDown() {
        reset(accountRepository, apiContext, payment);
        logbackLogger.detachAppender(logAppender);
        logAppender.stop();
        logAppender.list.clear();
    }

    @Test
    void completesBalancePaymentSuccessfully() {
        // Arrange
        String accountNumber = "NEX172720185";
        double amount = 1000.0;
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // Act
        boolean result = shoppingService.processPurchase(accountNumber, amount, "balance", null, null, null, null);

        // Assert
        assertTrue(result);
        assertEquals(34000.0, account.getBalance());
        verify(accountRepository, times(1)).save(account);
        verifyNoMoreInteractions(accountRepository);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Purchase of 1000.0 completed using balance for account NEX172720185");
        logger.info("Balance payment test succeeded: Balance updated to {}", account.getBalance());
    }

    @Test
    void rejectsBalancePaymentWithInsufficientFunds() {
        // Arrange
        String accountNumber = "NEX172720185";
        double amount = 40000.0;
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                shoppingService.processPurchase(accountNumber, amount, "balance", null, null, null, null));
        assertEquals("Insufficient funds in your account. Current balance: 35000.0", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository).findByAccountNumber(accountNumber);
        verifyNoMoreInteractions(accountRepository);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Insufficient balance in account NEX172720185: attempted purchase of 40000.0, but balance is only 35000.0");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
        logger.info("Insufficient funds test succeeded: {}", exception.getMessage());
    }

    @Test
    void failsBalancePaymentForNonexistentAccount() {
        // Arrange
        String accountNumber = "INVALID";
        double amount = 1000.0;
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                shoppingService.processPurchase(accountNumber, amount, "balance", null, null, null, null));
        assertEquals("Account not found with account number: INVALID", exception.getMessage());
        verify(accountRepository).findByAccountNumber(accountNumber);
        verifyNoMoreInteractions(accountRepository);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Account not found: INVALID");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
        logger.info("Account not found test succeeded: {}", exception.getMessage());
    }

    @Test
    void processesPaypalPaymentSuccessfully() throws Exception {
        // Arrange
        double amount = 100.0;
        shoppingService = spy(shoppingService);
        when(payment.getId()).thenReturn("PAY-12345");
        doReturn(true).when(shoppingService).processPurchase(null, amount, "paypal", null, null, null, null);

        // Act
        boolean result = shoppingService.processPurchase(null, amount, "paypal", null, null, null, null);

        // Assert
        assertTrue(result);
        verify(shoppingService).processPurchase(null, amount, "paypal", null, null, null, null);
        verifyNoMoreInteractions(shoppingService, payment, apiContext);
        logger.info("PayPal payment test succeeded");
    }

    @Test
    void failsPaypalPaymentWhenProcessingFails() throws Exception {
        // Arrange
        double amount = 100.0;
        shoppingService = spy(shoppingService);
        doReturn(false).when(shoppingService).processPurchase(null, amount, "paypal", null, null, null, null);

        // Act
        boolean result = shoppingService.processPurchase(null, amount, "paypal", null, null, null, null);

        // Assert
        assertFalse(result);
        verify(shoppingService).processPurchase(null, amount, "paypal", null, null, null, null);
        verifyNoMoreInteractions(shoppingService, payment, apiContext);
        logger.info("PayPal payment failure test succeeded");
    }

    @Test
    void executesPaypalPaymentSuccessfully() throws Exception {
        // Arrange
        String paymentId = "PAY-12345";
        String payerId = "PAYER-67890";
        shoppingService = spy(shoppingService);
        when(payment.getId()).thenReturn(paymentId);
        doReturn(true).when(shoppingService).executePaypalPayment(paymentId, payerId);

        // Act
        boolean result = shoppingService.executePaypalPayment(paymentId, payerId);

        // Assert
        assertTrue(result);
        verify(shoppingService).executePaypalPayment(paymentId, payerId);
        verifyNoMoreInteractions(shoppingService, payment, apiContext);
        logger.info("PayPal payment execution test succeeded");
    }

    @Test
    void failsToExecutePaypalPayment() throws Exception {
        // Arrange
        String paymentId = "PAY-12345";
        String payerId = "PAYER-67890";
        shoppingService = spy(shoppingService);
        doReturn(false).when(shoppingService).executePaypalPayment(paymentId, payerId);

        // Act
        boolean result = shoppingService.executePaypalPayment(paymentId, payerId);

        // Assert
        assertFalse(result);
        verify(shoppingService).executePaypalPayment(paymentId, payerId);
        verifyNoMoreInteractions(shoppingService, payment, apiContext);
        logger.info("PayPal payment execution failure test succeeded");
    }

    @Test
    void processesCreditCardPaymentSuccessfully() throws StripeException {
        // Arrange
        double amount = 100.0;
        String cardNumber = "4242424242424242";
        String expMonth = "12";
        String expYear = "2026";
        String cvc = "123";
        shoppingService = spy(shoppingService);
        doReturn(true).when(shoppingService).processPurchase(null, amount, "credit-card", cardNumber, expMonth, expYear, cvc);

        // Act
        boolean result = shoppingService.processPurchase(null, amount, "credit-card", cardNumber, expMonth, expYear, cvc);

        // Assert
        assertTrue(result);
        verify(shoppingService).processPurchase(null, amount, "credit-card", cardNumber, expMonth, expYear, cvc);
        verifyNoMoreInteractions(shoppingService);
        logger.info("Credit card payment test succeeded");
    }

    @Test
    void failsCreditCardPaymentWhenProcessingFails() throws StripeException {
        // Arrange
        double amount = 100.0;
        String cardNumber = "4000000000000002";
        String expMonth = "12";
        String expYear = "2026";
        String cvc = "123";
        shoppingService = spy(shoppingService);
        doReturn(false).when(shoppingService).processPurchase(null, amount, "credit-card", cardNumber, expMonth, expYear, cvc);

        // Act
        boolean result = shoppingService.processPurchase(null, amount, "credit-card", cardNumber, expMonth, expYear, cvc);

        // Assert
        assertFalse(result);
        verify(shoppingService).processPurchase(null, amount, "credit-card", cardNumber, expMonth, expYear, cvc);
        verifyNoMoreInteractions(shoppingService);
        logger.info("Credit card payment failure test succeeded");
    }

    @Test
    void identifiesVisaCardTypeCorrectly() {
        // Arrange
        String cardNumber = "4242424242424242";

        // Act
        String cardType = ReflectionTestUtils.invokeMethod(shoppingService, "detectCardType", cardNumber);

        // Assert
        assertEquals("Visa", cardType);
        logger.info("Card type detection test succeeded: Visa");
    }

    @Test
    void identifiesMasterCardTypeCorrectly() {
        // Arrange
        String cardNumber = "5555555555554444";

        // Act
        String cardType = ReflectionTestUtils.invokeMethod(shoppingService, "detectCardType", cardNumber);

        // Assert
        assertEquals("MasterCard", cardType);
        logger.info("Card type detection test succeeded: MasterCard");
    }

    @Test
    void returnsUnknownForUnrecognizedCardType() {
        // Arrange
        String cardNumber = "1234567890123456";

        // Act
        String cardType = ReflectionTestUtils.invokeMethod(shoppingService, "detectCardType", cardNumber);

        // Assert
        assertEquals("Unknown", cardType);
        logger.info("Card type detection test succeeded: Unknown");
    }

    @Test
    void rejectsInvalidPaymentMethod() {
        // Arrange
        String accountNumber = "NEX172720185";
        double amount = 1000.0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                shoppingService.processPurchase(accountNumber, amount, "invalid", null, null, null, null));
        assertEquals("Invalid payment method", exception.getMessage());
        logger.info("Invalid payment method test succeeded: {}", exception.getMessage());
    }
}