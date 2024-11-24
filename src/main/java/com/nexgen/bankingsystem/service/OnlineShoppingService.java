package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OnlineShoppingService implements ShoppingService {

    private static final Logger logger = LoggerFactory.getLogger(OnlineShoppingService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public boolean processPurchase(String accountNumber, double amount, String currency) {
        Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            if (account.getCurrency().equals(currency)) {
                if (account.getBalance() >= amount) {
                    account.setBalance(account.getBalance() - amount);
                    accountRepository.save(account);
                    logger.info("Purchase of {} {} completed for account {}", amount, currency, accountNumber);
                    return true;
                } else {
                    logger.warn("Insufficient balance in account {}: attempted purchase of {} {}, but balance is only {}",
                            accountNumber, amount, currency, account.getBalance());
                }
            } else {
                logger.warn("Currency mismatch for account {}: attempted purchase in {} when account currency is {}",
                        accountNumber, currency, account.getCurrency());
            }
        } else {
            logger.warn("Account not found: {}", accountNumber);
        }
        return false;
    }
}

