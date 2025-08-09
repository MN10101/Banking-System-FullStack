package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.service.AccountService;
import com.nexgen.bankingsystem.service.IPDetectionService;
import com.nexgen.bankingsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AccountControllerTest {

    @InjectMocks
    private AccountController accountController;

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @Mock
    private IPDetectionService ipDetectionService;

    @Mock
    private HttpServletRequest request;

    private Account account;
    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock Account and User
        account = new Account();
        account.setAccountNumber("NEX324939864");
        account.setIban("DE43500202220324939864");
        account.setBalance(20140.11);
        account.setCurrency("EUR");

        user = new User();
        user.setEmail("mn.de@outlook.com");

        // Mock external calls
        when(ipDetectionService.getClientIP(any(HttpServletRequest.class))).thenReturn("192.168.1.1");
        when(ipDetectionService.getLocationFromIP(anyString())).thenReturn("Berlin, Germany");
    }

    @Test
    public void createsAccount() {
        // Mock UserService to return an existing user
        when(userService.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(accountService.createAccount(any(User.class), anyString(), anyDouble())).thenReturn(account);

        ResponseEntity<?> response = accountController.createAccount(account, "mn.de@outlook.com", request);

        assert(response.getStatusCodeValue() == 200);
        assert(response.getBody().equals("Account created successfully: NEX324939864"));
    }

    @Test
    public void failsToCreateAccountIfUserMissing() {
        // Mock UserService to return an empty optional (user not found)
        when(userService.findUserByEmail(anyString())).thenReturn(Optional.empty());

        ResponseEntity<?> response = accountController.createAccount(account, "mn.de@outlook.com", request);

        assert(response.getStatusCodeValue() == 400);
        assert(response.getBody().equals("User not found."));
    }

    @Test
    public void depositsAmount() {
        // Mock AccountService to simulate deposit success
        when(accountService.deposit(anyString(), anyDouble())).thenReturn(true);

        ResponseEntity<?> response = accountController.deposit("NEX324939864", 500);

        assert(response.getStatusCodeValue() == 200);
        assert(response.getBody().equals("Amount deposited successfully."));
    }

    @Test
    public void failsToDepositIfAccountMissing() {
        // Mock AccountService to simulate deposit failure
        when(accountService.deposit(anyString(), anyDouble())).thenReturn(false);

        ResponseEntity<?> response = accountController.deposit("NEX324939864", 500);

        assert(response.getStatusCodeValue() == 400);
        assert(response.getBody().equals("Deposit failed. Account not found."));
    }

    @Test
    public void withdrawsAmount() {
        // Mock AccountService to simulate withdrawal success
        when(accountService.withdraw(anyString(), anyDouble())).thenReturn(true);

        ResponseEntity<?> response = accountController.withdraw("NEX324939864", 500);

        assert(response.getStatusCodeValue() == 200);
        assert(response.getBody().equals("Amount withdrawn successfully."));
    }

    @Test
    public void failsToWithdrawIfBalanceLowOrAccountMissing() {
        // Mock AccountService to simulate withdrawal failure
        when(accountService.withdraw(anyString(), anyDouble())).thenReturn(false);

        ResponseEntity<?> response = accountController.withdraw("NEX324939864", 500);

        assert(response.getStatusCodeValue() == 400);
        assert(response.getBody().equals("Withdrawal failed. Insufficient balance or account not found."));
    }

    @Test
    public void getsAccountDetails() {
        // Mock Account with correct data
        account.setAccountNumber("NEX324939864");
        account.setIban("DE43500202220324939864");
        account.setBalance(20140.11);
        account.setCurrency("EUR");

        // Mock AccountService to return the correct account
        when(accountService.findAccountByAccountNumber(anyString())).thenReturn(Optional.of(account));
        when(accountService.findIbanByAccountNumber(anyString())).thenReturn(Optional.of(account.getIban()));

        ResponseEntity<?> response = accountController.getAccountDetails("NEX324939864");

        // Log the response body for debugging
        System.out.println("Response Body: " + response.getBody());

        // Assertions to check if the correct values are returned
        assert(response.getStatusCodeValue() == 200);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        // Check if the expected values are in the response
        assert(responseBody.get("accountNumber").equals("NEX324939864"));
        assert(responseBody.get("iban").equals(account.getIban()));
        assert(responseBody.get("balance").equals(20140.11));
        assert(responseBody.get("currency").equals("EUR"));
    }

    @Test
    public void failsToGetAccountDetailsIfAccountMissing() {
        // Mock AccountService to return an empty optional (account not found)
        when(accountService.findAccountByAccountNumber(anyString())).thenReturn(Optional.empty());

        ResponseEntity<?> response = accountController.getAccountDetails("NEX324939864");

        assert(response.getStatusCodeValue() == 400);
        assert(((Map<String, Object>) response.getBody()).get("message").equals("Account not found."));
    }
}