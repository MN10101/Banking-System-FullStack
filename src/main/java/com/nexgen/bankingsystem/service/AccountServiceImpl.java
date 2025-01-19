package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.repository.AccountRepository;
import com.nexgen.bankingsystem.security.AccountBalanceWebSocketHandler;
import org.iban4j.Iban;
import org.iban4j.IbanFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountBalanceWebSocketHandler webSocketHandler;

    private void notifyBalanceUpdate(String balance) {
        try {
            webSocketHandler.sendBalanceUpdate(balance);
        } catch (IOException e) {
            logger.error("Error sending balance update through WebSocket", e);
        }
    }

    private String generateAccountNumber() {
        return "NEX" + (int)(Math.random() * 1_000_000_000);
    }

    // Using SLF4J for logging
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    // IBAN Validation Method
    public boolean isValidIban(String iban) {
        try {
            Iban ibanObject = Iban.valueOf(iban);
            return true;
        } catch (IbanFormatException e) {
            logger.error("Invalid IBAN format for {}: {}", iban, e.getMessage());
            return false;
        }
    }

    @Override
    public Account createAccount(User user, String accountNumber, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(initialBalance);
        account.setCurrency("EUR");
        account.setUser(user);
        account.setIban(generateIban(account.getAccountNumber()));

        // Validate IBAN before saving
        if (!isValidIban(account.getIban())) {
            throw new IllegalArgumentException("Generated IBAN is invalid: " + account.getIban());
        }

        return accountRepository.save(account);
    }

    public String generateIban(String accountNumber) {
        // Ensure accountNumber is numeric (or convert if necessary)
        String numericAccountNumber = convertToNumericAccountNumber(accountNumber);

        // AccountNumber is now cleaned and formatted
        String bankCode = "50020222";

        // Combine the country code, bank code, and account number (without check digits)
        String ibanWithoutCheckDigits = "DE00" + bankCode + numericAccountNumber;

        // Log the intermediate values for debugging
        logger.debug("ibanWithoutCheckDigits: {}", ibanWithoutCheckDigits);

        // Calculate the correct check digits
        String checkDigits = calculateCheckDigits(ibanWithoutCheckDigits);

        // Log the check digits calculation
        logger.debug("Calculated Check Digits: {}", checkDigits);

        // Generate the full IBAN by adding the country code, check digits, bank code, and numeric account number
        String fullIban = "DE" + checkDigits + bankCode + numericAccountNumber;
        return fullIban;
    }



    public String convertToNumericAccountNumber(String accountNumber) {
        // Ensure account number only contains numeric characters
        String numericAccountNumber = accountNumber.replaceAll("[^0-9]", "");

        // Pad with leading zeros to reach 10 digits
        numericAccountNumber = String.format("%010d", Integer.parseInt(numericAccountNumber));

        return numericAccountNumber;
    }

    public String calculateCheckDigits(String ibanWithoutCheckDigits) {
        // Rearrange IBAN to move the country code and check digits to the end
        String ibanForCheck = ibanWithoutCheckDigits.substring(4) + ibanWithoutCheckDigits.substring(0, 4);

        // Log the ibanForCheck to ensure it's constructed correctly
        logger.debug("ibanForCheck: {}", ibanForCheck);

        // Convert the IBAN string to a numeric value by replacing letters with their numeric equivalents
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ibanForCheck.length(); i++) {
            char c = ibanForCheck.charAt(i);
            if (Character.isLetter(c)) {
                sb.append((int) c - 55);
            } else {
                sb.append(c);
            }
        }

        // Perform MOD97 check to calculate the check digits
        BigInteger ibanNumber = new BigInteger(sb.toString());
        int checkDigits = 98 - ibanNumber.mod(BigInteger.valueOf(97)).intValue();

        // Ensure that the result is two digits
        String formattedCheckDigits = String.format("%02d", checkDigits);

        // Log the final calculated check digits
        logger.debug("Calculated Check Digits: {}", formattedCheckDigits);

        return formattedCheckDigits;
    }




    // Method to convert letters to numeric values
    private String convertAccountNumber(String accountNumber) {
        StringBuilder numericAccount = new StringBuilder();
        for (char c : accountNumber.toCharArray()) {
            if (Character.isLetter(c)) {
                numericAccount.append((int) c - 55);
            } else {
                numericAccount.append(c);
            }
        }
        return numericAccount.toString();
    }


    @Override
    public Optional<Account> findAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public boolean deposit(String accountNumber, double amount) {
        logger.info("Initiating deposit of {} to account {}", amount, accountNumber);
        Optional<Account> optionalAccount = findAccountByAccountNumber(accountNumber);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            account.setBalance(account.getBalance() + amount);
            accountRepository.save(account);

            // Notify WebSocket clients
            notifyBalanceUpdate(String.valueOf(account.getBalance()));

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

                // Notify WebSocket clients
                notifyBalanceUpdate(String.valueOf(account.getBalance()));

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
                return false;
            }
            double conversionRate = currencyConversionService.getConversionRate(fromCurrency, toCurrency);
            if (conversionRate <= 0) {
                return false;
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
    @Transactional
    public boolean transferMoney(String fromIban, String toIban, double amount, String currency) {
        Optional<Account> optionalFromAccount = accountService.findAccountByIban(fromIban);
        Optional<Account> optionalToAccount = accountService.findAccountByIban(toIban);

        if (optionalFromAccount.isPresent() && optionalToAccount.isPresent()) {
            Account fromAccount = optionalFromAccount.get();
            Account toAccount = optionalToAccount.get();

            // Ensure the accounts are of the same currency
            if (!fromAccount.getCurrency().equals(currency) || !toAccount.getCurrency().equals(currency)) {
                throw new IllegalArgumentException("Currency mismatch");
            }

            // Check if the sender has enough balance
            if (fromAccount.getBalance() >= amount) {
                fromAccount.setBalance(fromAccount.getBalance() - amount);
                toAccount.setBalance(toAccount.getBalance() + amount);

                accountRepository.save(fromAccount);
                accountRepository.save(toAccount);

                // Notify WebSocket clients for both accounts
                notifyBalanceUpdate(String.valueOf(fromAccount.getBalance()));
                notifyBalanceUpdate(String.valueOf(toAccount.getBalance()));

                return true;
            } else {
                throw new IllegalArgumentException("Insufficient funds");
            }
        }

        throw new IllegalArgumentException("Invalid IBAN(s)");
    }




    @Override
    public Optional<String> findIbanByAccountNumber(String accountNumber) {
        Optional<Account> accountOptional = findAccountByAccountNumber(accountNumber);
        return accountOptional.map(Account::getIban);
    }

    @Override
    public Optional<Account> findAccountByIban(String iban) {
        return accountRepository.findByIban(iban);
    }
}
