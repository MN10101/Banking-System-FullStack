package com.nexgen.bankingsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull(message = "User cannot be null")
    private User user;

    @NotBlank(message = "Account number cannot be blank")
    @Size(min = 10, max = 12, message = "Account number must be between 10 and 12 characters")
    private String accountNumber;

    @NotBlank(message = "IBAN cannot be blank")
    @Pattern(regexp = "^DE[0-9]{20}$", message = "IBAN must be a valid German IBAN starting with 'DE' and 22 characters long")
    private String iban;

    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    private double balance;

    @NotBlank(message = "Currency cannot be blank")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    private String currency;

    public Account() {}

    public Account(User user, String accountNumber, String iban, double balance, String currency) {
        this.user = user;
        this.accountNumber = accountNumber;
        this.iban = iban;
        this.balance = balance;
        this.currency = currency;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
