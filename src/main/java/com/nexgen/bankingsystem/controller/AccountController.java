package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.service.AccountService;
import com.nexgen.bankingsystem.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@Valid @RequestBody Account account, @RequestParam String email) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        Optional<User> userOptional = userService.findUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (account.getAccountNumber() == null || account.getAccountNumber().isEmpty()) {
                return ResponseEntity.badRequest().body("Account number cannot be null or empty.");
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
    public ResponseEntity<Account> getAccountDetails(@RequestParam String accountNumber) {
        return accountService.findAccountByAccountNumber(accountNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
