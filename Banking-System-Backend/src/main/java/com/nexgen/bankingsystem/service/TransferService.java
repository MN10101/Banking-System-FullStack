package com.nexgen.bankingsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    @Autowired
    private AccountService accountService;

    public void processTransfer(String fromIban, String toIban, double amount, String currency) {
        // Validate inputs
        if (amount <= 0) {
            logger.error("Invalid transfer amount: {}", amount);
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null || !currency.matches("^[A-Z]{3}$")) {
            logger.error("Invalid currency code: {}", currency);
            throw new IllegalArgumentException("Currency must be a valid 3-letter ISO code");
        }

        // Validate the IBANs and process the transfer
        boolean success = accountService.transferMoney(fromIban, toIban, amount, currency);
        if (success) {
            logger.info("Transfer of {} {} completed from {} to {}", amount, currency, fromIban, toIban);
        } else {
            logger.error("Transfer failed from {} to {}: insufficient funds or invalid IBAN", fromIban, toIban);
            throw new RuntimeException("Transfer failed due to insufficient funds or invalid IBAN.");
        }
    }
}