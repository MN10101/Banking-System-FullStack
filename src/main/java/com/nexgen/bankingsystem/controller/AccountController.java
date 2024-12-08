package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.service.AccountService;
import com.nexgen.bankingsystem.service.IPDetectionService;
import com.nexgen.bankingsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private IPDetectionService ipDetectionService;

    private static final String IBAN_REGEX = "^DE\\d{20}$";

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);


    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@Valid @RequestBody Account account, @RequestParam String email, HttpServletRequest request) {
        String clientIP = ipDetectionService.getClientIP(request);
        String locationInfo = ipDetectionService.getLocationFromIP(clientIP);
        logger.info("Account creation request from IP: {} - Location: {}", clientIP, locationInfo);

        // Existing logic remains unchanged
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        Optional<User> userOptional = userService.findUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (account.getAccountNumber() == null || account.getAccountNumber().isEmpty()) {
                return ResponseEntity.badRequest().body("Account number cannot be null or empty.");
            }

            if (account.getIban() == null || account.getIban().isEmpty()) {
                return ResponseEntity.badRequest().body("IBAN cannot be null or empty.");
            }
            if (!Pattern.matches(IBAN_REGEX, account.getIban())) {
                return ResponseEntity.badRequest().body("IBAN must be a valid German IBAN starting with 'DE' and 22 characters long.");
            }

            if (account.getBalance() < 0) {
                return ResponseEntity.badRequest().body("Initial balance cannot be negative.");
            }

            Account createdAccount = accountService.createAccount(user, account.getAccountNumber(), account.getBalance());
            return ResponseEntity.ok("Account created successfully: " + createdAccount.getAccountNumber());
        }
        return ResponseEntity.badRequest().body("User not found.");
    }


    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestParam String accountNumber, @RequestParam double amount) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Account number cannot be null or empty.");
        }
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Deposit amount must be greater than zero.");
        }
        if (accountService.deposit(accountNumber, amount)) {
            return ResponseEntity.ok("Amount deposited successfully.");
        }
        return ResponseEntity.badRequest().body("Deposit failed. Account not found.");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestParam String accountNumber, @RequestParam double amount) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Account number cannot be null or empty.");
        }
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Withdrawal amount must be greater than zero.");
        }
        if (accountService.withdraw(accountNumber, amount)) {
            return ResponseEntity.ok("Amount withdrawn successfully.");
        }
        return ResponseEntity.badRequest().body("Withdrawal failed. Insufficient balance or account not found.");
    }

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getAccountDetails(@RequestParam String accountNumber) {
        Optional<Account> accountOptional = accountService.findAccountByAccountNumber(accountNumber);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            Optional<String> iban = accountService.findIbanByAccountNumber(accountNumber);
            return ResponseEntity.ok(Map.of(
                    "accountNumber", account.getAccountNumber(),
                    "iban", iban.orElse("IBAN not available"),
                        "balance", account.getBalance(),
                    "currency", account.getCurrency()
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Account not found."));
    }


}
