package com.nexgen.bankingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    @Autowired
    private AccountService accountService;

    public void processTransfer(String fromIban, String toIban, double amount, String currency) {
        // Validate the IBANs and process the transfer (update account balance)
        boolean success = accountService.transferMoney(fromIban, toIban, amount, currency);
        if (!success) {
            throw new RuntimeException("Transfer failed due to insufficient funds or invalid IBAN.");
        }
    }
}
