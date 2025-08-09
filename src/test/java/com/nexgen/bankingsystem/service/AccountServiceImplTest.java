package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.repository.AccountRepository;
import com.nexgen.bankingsystem.security.AccountBalanceWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrencyConversionService currencyConversionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AccountBalanceWebSocketHandler webSocketHandler;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        // Create a test user
        user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(new Date(85, 6, 19));
        user.setPhoneNumber("01639769764");
        user.setPassword("password123");
        user.setTaxNumber("MN12542BE22");
        user.setIdOrPassport("123456");
        user.setLastKnownIP("178.0.238.173");
        user.setEnabled(true);
        user.setRoles(new HashSet<>());
        user.getRoles().add("ROLE_USER");

        // Create a test account
        account = new Account();
        account.setAccountNumber("NEX172720185");
        account.setIban("DE54500202220172720185");
        account.setBalance(35000.0);
        account.setCurrency("EUR");
        account.setUser(user);

        // Mock self-referential AccountService to avoid NullPointerException
        lenient().when(accountService.findAccountByIban(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void createsAccountWithValidIban() throws Exception {
        // Given
        Account newAccount = new Account();
        newAccount.setAccountNumber("NEX123456789");
        newAccount.setIban("DE54500202220123456789");
        newAccount.setBalance(1000.0);
        newAccount.setCurrency("EUR");
        newAccount.setUser(user);
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);

        // When
        Account createdAccount = accountService.createAccount(user, null, 1000.0);

        // Then
        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getAccountNumber()).startsWith("NEX");
        assertThat(createdAccount.getIban()).startsWith("DE");
        assertThat(createdAccount.getBalance()).isEqualTo(1000.0);
        assertThat(createdAccount.getCurrency()).isEqualTo("EUR");
        assertThat(createdAccount.getUser()).isEqualTo(user);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void throwsExceptionWhenCreatingAccountWithNegativeBalance() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(user, null, -100.0));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void findsAccountByExistingAccountNumber() {
        // Given
        String accountNumber = "NEX172720185";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // When
        Optional<Account> foundAccount = accountService.findAccountByAccountNumber(accountNumber);

        // Then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getAccountNumber()).isEqualTo(accountNumber);
        assertThat(foundAccount.get().getIban()).isEqualTo("DE54500202220172720185");
        assertThat(foundAccount.get().getBalance()).isEqualTo(35000.0);
        verify(accountRepository).findByAccountNumber(accountNumber);
    }

    @Test
    void returnsEmptyForNonExistentAccountNumber() {
        // Given
        String accountNumber = "NEX999999999";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // When
        Optional<Account> foundAccount = accountService.findAccountByAccountNumber(accountNumber);

        // Then
        assertThat(foundAccount).isNotPresent();
        verify(accountRepository).findByAccountNumber(accountNumber);
    }

    @Test
    void findsAccountByExistingIban() {
        // Given
        String iban = "DE54500202220172720185";
        when(accountRepository.findByIban(iban)).thenReturn(Optional.of(account));

        // When
        Optional<Account> foundAccount = accountService.findAccountByIban(iban);

        // Then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getIban()).isEqualTo(iban);
        assertThat(foundAccount.get().getAccountNumber()).isEqualTo("NEX172720185");
        assertThat(foundAccount.get().getBalance()).isEqualTo(35000.0);
        verify(accountRepository).findByIban(iban);
    }

    @Test
    void returnsEmptyForNonExistentIban() {
        // Given
        String iban = "DE99999999999999999999";
        when(accountRepository.findByIban(iban)).thenReturn(Optional.empty());

        // When
        Optional<Account> foundAccount = accountService.findAccountByIban(iban);

        // Then
        assertThat(foundAccount).isNotPresent();
        verify(accountRepository).findByIban(iban);
    }

    @Test
    void findsIbanForExistingAccountNumber() {
        // Given
        String accountNumber = "NEX172720185";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // When
        Optional<String> foundIban = accountService.findIbanByAccountNumber(accountNumber);

        // Then
        assertThat(foundIban).isPresent();
        assertThat(foundIban.get()).isEqualTo("DE54500202220172720185");
        verify(accountRepository).findByAccountNumber(accountNumber);
    }

    @Test
    void returnsEmptyIbanForNonExistentAccountNumber() {
        // Given
        String accountNumber = "NEX999999999";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // When
        Optional<String> foundIban = accountService.findIbanByAccountNumber(accountNumber);

        // Then
        assertThat(foundIban).isNotPresent();
        verify(accountRepository).findByAccountNumber(accountNumber);
    }

    @Test
    void updatesBalanceWhenDepositingToExistingAccount() throws Exception {
        // Given
        String accountNumber = "NEX172720185";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        doNothing().when(webSocketHandler).sendBalanceUpdate(anyString());

        // When
        boolean result = accountService.deposit(accountNumber, 500.0);

        // Then
        assertThat(result).isTrue();
        assertThat(account.getBalance()).isEqualTo(35500.0);
        verify(accountRepository).save(account);
        verify(webSocketHandler).sendBalanceUpdate("35500.0");
    }

    @Test
    void failsToDepositToNonExistentAccount() throws Exception {
        // Given
        String accountNumber = "NEX999999999";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // When
        boolean result = accountService.deposit(accountNumber, 500.0);

        // Then
        assertThat(result).isFalse();
        verify(accountRepository, never()).save(any(Account.class));
        verify(webSocketHandler, never()).sendBalanceUpdate(anyString());
    }

    @Test
    void updatesBalanceWhenWithdrawingWithSufficientFunds() throws Exception {
        // Given
        String accountNumber = "NEX172720185";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        doNothing().when(webSocketHandler).sendBalanceUpdate(anyString());
        doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString());

        // When
        boolean result = accountService.withdraw(accountNumber, 1000.0);

        // Then
        assertThat(result).isTrue();
        assertThat(account.getBalance()).isEqualTo(34000.0);
        verify(accountRepository).save(account);
        verify(webSocketHandler).sendBalanceUpdate("34000.0");
        verify(notificationService).sendNotification(
                eq("mn.de@outlook.com"),
                eq("Withdrawal Notification"),
                eq("A withdrawal of 1000.0 was made from your account NEX172720185")
        );
    }

    @Test
    void failsToWithdrawWithInsufficientFunds() throws Exception {
        // Given
        String accountNumber = "NEX172720185";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // When
        boolean result = accountService.withdraw(accountNumber, 50000.0);

        // Then
        assertThat(result).isFalse();
        verify(accountRepository, never()).save(any(Account.class));
        verify(webSocketHandler, never()).sendBalanceUpdate(anyString());
        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    void failsToWithdrawFromNonExistentAccount() throws Exception {
        // Given
        String accountNumber = "NEX999999999";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // When
        boolean result = accountService.withdraw(accountNumber, 1000.0);

        // Then
        assertThat(result).isFalse();
        verify(accountRepository, never()).save(any(Account.class));
        verify(webSocketHandler, never()).sendBalanceUpdate(anyString());
        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString());
    }
}