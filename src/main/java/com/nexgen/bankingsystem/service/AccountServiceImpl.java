package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;


@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @Autowired
    private NotificationService notificationService;


    private String generateAccountNumber() {
        return "ACC" + (int)(Math.random() * 1_000_000_000);
    }

    // Using SLF4J for logging
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    public Account createAccount(User user, String accountNumber, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }

        // Generate account number if not provided
        if (accountNumber == null || accountNumber.isEmpty()) {
            accountNumber = generateAccountNumber();
        }

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(initialBalance);
        account.setCurrency("EUR");
        account.setUser(user);

        logger.debug("Saving account: {} for user: {}", accountNumber, user.getEmail());
        return accountRepository.save(account);
    }


    @Override
    public Optional<Account> findAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public boolean deposit(String accountNumber, double amount) {
        // Using SLF4J logging
        logger.info("Initiating deposit of {} to account {}", amount, accountNumber);
        Optional<Account> optionalAccount = findAccountByAccountNumber(accountNumber);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            account.setBalance(account.getBalance() + amount);
            accountRepository.save(account);
            logger.info("Deposit successful. New balance: {}", account.getBalance());
            return true;
        }
        logger.warn("Deposit failed. Account {} not found.", accountNumber);
        return false;
    }

    @Override
    public boolean withdraw(String accountNumber, double amount) {
        Optional<Account> optionalAccount = findAccountByAccountNumber(accountNumber);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            if (account.getBalance() >= amount) {
                account.setBalance(account.getBalance() - amount);
                accountRepository.save(account);

                // Send notification
                User user = account.getUser();
                String message = "A withdrawal of " + amount + " was made from your account " + accountNumber;
                notificationService.sendNotification(user.getEmail(), "Withdrawal Notification", message);

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean convertCurrency(String accountNumber, String fromCurrency, String toCurrency, double amount) {
        Optional<Account> optionalAccount = findAccountByAccountNumber(accountNumber);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            if (account.getBalance() < amount) {
                return false; // Insufficient balance
            }
            double conversionRate = currencyConversionService.getConversionRate(fromCurrency, toCurrency);
            if (conversionRate <= 0) {
                return false; // Invalid conversion rate
            }

            account.setBalance(account.getBalance() - amount);
            accountRepository.save(account);

            double convertedAmount = amount * conversionRate;
            Account convertedAccount = new Account();
            convertedAccount.setUser(account.getUser());
            convertedAccount.setAccountNumber(accountNumber);
            convertedAccount.setBalance(convertedAmount);
            convertedAccount.setCurrency(toCurrency);
            accountRepository.save(convertedAccount);
            return true;
        }
        return false;
    }

    @Override
    public boolean transferMoney(String fromAccount, String toAccount, double amount, String currency) {
        Optional<Account> optionalFromAccount = findAccountByAccountNumber(fromAccount);
        Optional<Account> optionalToAccount = findAccountByAccountNumber(toAccount);
        if (optionalFromAccount.isPresent() && optionalToAccount.isPresent()) {
            Account fromAcc = optionalFromAccount.get();
            Account toAcc = optionalToAccount.get();
            if (fromAcc.getCurrency().equals(currency) && toAcc.getCurrency().equals(currency) && fromAcc.getBalance() >= amount) {
                fromAcc.setBalance(fromAcc.getBalance() - amount);
                toAcc.setBalance(toAcc.getBalance() + amount);
                accountRepository.save(fromAcc);
                accountRepository.save(toAcc);
                return true;
            }
        }
        return false;
    }
}