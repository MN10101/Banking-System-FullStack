package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.repository.AccountRepository;
import com.nexgen.bankingsystem.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class ChatbotService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmailUtil emailUtil;

    public String getResponse(String userMessage) {
        try {
            // Decode the user message to handle URL-encoded characters
            userMessage = URLDecoder.decode(userMessage, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, there was an issue decoding your message.";
        }
        System.out.println("Received message in service: " + userMessage);

        if (userMessage.contains("create account")) {
            return "To create an account, you'll need to provide your email and some account details.";
        }
        if (userMessage.contains("deposit")) {
            return "To deposit money, simply provide your account number and the amount you'd like to deposit.";
        }
        if (userMessage.contains("withdraw")) {
            return "To withdraw money, please let us know your account number and the amount you'd like to withdraw.";
        }
        if (userMessage.contains("account details")) {
            return "To view your account details, please provide your account number.";
        }

        // Handle account number for details lookup
        if (userMessage.matches(".*\\d{10,12}.*")) {
            String accountNumber = extractAccountNumber(userMessage);
            System.out.println("Extracted account number: " + accountNumber);
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);

            if (optionalAccount.isPresent()) {
                Account account = optionalAccount.get();
                return "Account details for " + accountNumber + ":\n" +
                        "Account Balance: " + account.getBalance() + " " + account.getCurrency() + "\n" +
                        "IBAN: " + account.getIban();
            } else {
                return "Sorry, no account found with that number. Please check your account number.";
            }
        }

        if (userMessage.contains("register")) {
            return "To register, we need some basic personal information like your name, email, and password.";
        }
        if (userMessage.contains("verify account")) {
            return "To verify your account, you will need to use the verification code sent to your email.";
        }
        if (userMessage.contains("login")) {
            return "To log in, provide your registered email and password.";
        }
        if (userMessage.contains("contact support")) {
            // Send an email to support team when user asks for help
            String supportEmail = "nexgin.bank@gmail.com";
            String subject = "User Request for Support";
            String htmlContent = "<p>A user has requested support.</p><p>Please assist the user with their issue.</p>";
            emailUtil.sendHtmlMessage(supportEmail, subject, htmlContent);
            return "If you need help, you can contact our support team, and we'll assist you shortly. A request has been sent to our support team.";
        }
        if (userMessage.contains("convert currency")) {
            return "If you'd like to convert currencies, tell us the amount and which currencies you'd like to convert from and to.";
        }
        if (userMessage.contains("purchase")) {
            return "To make a purchase, provide your payment method and the items you're buying.";
        }
        if (userMessage.contains("transfer money")) {
            String accountNumber = extractAccountNumber(userMessage);
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);

            if (optionalAccount.isPresent()) {
                Account account = optionalAccount.get();
                return "To transfer money from your account " + accountNumber + ", please provide the recipient's account details and the amount you'd like to transfer.";
            } else {
                return "Sorry, no account found with that number. Please check your account number.";
            }
        }

        return "Sorry, I didn't quite understand that. Can you rephrase your question?";
    }

    private String extractAccountNumber(String message) {
        // Extract account number from the message
        String[] words = message.split(" ");
        for (String word : words) {
            if (word.matches("\\d{10,12}")) {
                System.out.println("Account number found: " + word);
                return word;
            }
        }
        System.out.println("No valid account number found.");
        return "";
    }
}
