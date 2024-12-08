package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import java.util.Optional;

public interface AccountService {
    Account createAccount(User user, String accountNumber, double initialBalance);
    Optional<Account> findAccountByAccountNumber(String accountNumber);
    boolean deposit(String accountNumber, double amount);
    boolean withdraw(String accountNumber, double amount);
    boolean convertCurrency(String accountNumber, String fromCurrency, String toCurrency, double amount);
    boolean transferMoney(String fromAccount, String toAccount, double amount, String currency);

    // Add method to get IBAN by account number
    Optional<String> findIbanByAccountNumber(String accountNumber);

    Optional<Account> findAccountByIban(String iban);


}

