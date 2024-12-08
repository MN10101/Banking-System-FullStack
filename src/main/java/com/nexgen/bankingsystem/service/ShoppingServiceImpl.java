package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.repository.AccountRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class ShoppingServiceImpl implements ShoppingService {

    @Value("${paypal.client-id}")
    private String paypalClientId;

    @Value("${paypal.client-secret}")
    private String paypalClientSecret;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;


    private static final Logger logger = LoggerFactory.getLogger(ShoppingServiceImpl.class);

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional
    public boolean processPurchase(String accountNumber, double amount, String paymentMethod, String cardNumber, String expMonth, String expYear, String cvc) {
        switch (paymentMethod.toLowerCase()) {
            case "balance":
                return processWithBalance(accountNumber, amount);
            case "paypal":
                return processWithPaypal(amount);
            case "credit-card":
                return processWithCreditCard(amount, cardNumber, expMonth, expYear, cvc);
            default:
                throw new IllegalArgumentException("Invalid payment method");
        }
    }

    private boolean processWithBalance(String accountNumber, double amount) {
        Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            if (account.getBalance() >= amount) {
                account.setBalance(account.getBalance() - amount);
                accountRepository.save(account);
                logger.info("Purchase of {} completed using balance for account {}", amount, accountNumber);
                return true;
            } else {
                logger.warn("Insufficient balance in account {}: attempted purchase of {}, but balance is only {}",
                        accountNumber, amount, account.getBalance());
                throw new IllegalArgumentException("Insufficient funds in your account. Current balance: " + account.getBalance());
            }
        } else {
            logger.warn("Account not found: {}", accountNumber);
            throw new IllegalArgumentException("Account not found with account number: " + accountNumber);
        }
    }



    private boolean processWithPaypal(double amount) {
        try {
            // Set PayPal SDK configuration
            Map<String, String> sdkConfig = new HashMap<>();
            sdkConfig.put("mode", "sandbox");

            OAuthTokenCredential tokenCredential = new OAuthTokenCredential(
                    paypalClientId,
                    paypalClientSecret,
                    sdkConfig
            );

            APIContext apiContext = new APIContext(tokenCredential.getAccessToken());
            apiContext.setConfigurationMap(sdkConfig);

            // Create a payment amount
            Amount paymentAmount = new Amount();
            paymentAmount.setTotal(String.format("%.2f", amount));
            paymentAmount.setCurrency("EUR");

            Transaction transaction = new Transaction();
            transaction.setAmount(paymentAmount);
            transaction.setDescription("PayPal Payment");

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            // Specify redirect URLs
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setReturnUrl("http://localhost:8080/api/paypal/success");
            redirectUrls.setCancelUrl("http://localhost:8080/api/paypal/cancel");

            // Create a payment with payer and transaction
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            Payment payment = new Payment();
            payment.setIntent("sale");
            payment.setPayer(payer);
            payment.setTransactions(transactions);
            payment.setRedirectUrls(redirectUrls);

            // Execute payment creation
            Payment createdPayment = payment.create(apiContext);
            logger.info("PayPal payment created successfully: {}", createdPayment.getId());
            return true;
        } catch (Exception e) {
            logger.error("PayPal payment failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean executePaypalPayment(String paymentId, String payerId) {
        try {
            // Set PayPal API context with credentials
            APIContext apiContext = new APIContext(paypalClientId, paypalClientSecret, "sandbox");

            // Create Payment and PaymentExecution objects
            Payment payment = new Payment();
            payment.setId(paymentId);
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            // Execute the payment
            Payment executedPayment = payment.execute(apiContext, paymentExecution);
            logger.info("Payment executed successfully: {}", executedPayment.getId());
            return true;
        } catch (Exception e) {
            logger.error("Error executing PayPal payment: {}", e.getMessage());
            return false;
        }
    }





    private boolean processWithCreditCard(double amount, String cardNumber, String expMonth, String expYear, String cvc) {
        try {
            // Detect the card type
            String cardType = detectCardType(cardNumber);
            logger.info("Detected card type: {}", cardType);

            // Set Stripe API key from the injected value
            Stripe.apiKey = stripeSecretKey;

            // Create a charge
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", (int) (amount * 100));
            chargeParams.put("currency", "eur");
            chargeParams.put("description", "Credit Card Payment");

            Map<String, Object> cardParams = new HashMap<>();
            cardParams.put("number", cardNumber);
            cardParams.put("exp_month", expMonth);
            cardParams.put("exp_year", expYear);
            cardParams.put("cvc", cvc);

            chargeParams.put("source", cardParams);

            Charge charge = Charge.create(chargeParams);
            logger.info("Credit Card payment successful: {}", charge.getId());
            return true;
        } catch (StripeException e) {
            logger.error("Credit Card payment failed: {}", e.getMessage());
            return false;
        }
    }


    private String detectCardType(String cardNumber) {
        String cardType = "Unknown";

        // Regex patterns for different card types
        if (cardNumber.matches("^4[0-9]{12}(?:[0-9]{3})?$")) {
            cardType = "Visa";
        } else if (cardNumber.matches("^5[1-5][0-9]{14}$")) {
            cardType = "MasterCard";
        } else if (cardNumber.matches("^3[47][0-9]{13}$")) {
            cardType = "American Express";
        } else if (cardNumber.matches("^6(?:011|5[0-9]{2})[0-9]{12}$")) {
            cardType = "Discover";
        } else if (cardNumber.matches("^35[2-8][0-9]{12}$")) {
            cardType = "JCB";
        }

        return cardType;
    }

}
