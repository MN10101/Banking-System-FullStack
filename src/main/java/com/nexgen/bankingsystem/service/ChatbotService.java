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

    public String getResponse(String userMessage) {
        try {
            // Decode the user message to handle URL-encoded characters
            userMessage = URLDecoder.decode(userMessage, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, there was an issue decoding your message.";
        }
        System.out.println("Received message in service: " + userMessage);


        if (userMessage.contains("hi")) {
            return "Hi you, how are you doing today? 😃";
        }
        if (userMessage.contains("I'm fine thanks, how about you?")) {
            return "I'm doing great, thanks!. 🥰";
        }
        if (userMessage.contains("create account")) {
            return "To create an account, you'll need to provide your email and some account details. ✨";
        }

        if (userMessage.contains("register")) {
            return "To register, we need some basic personal information like your name, email, and password, etc. 🪧";
        }
        if (userMessage.contains("verify account")) {
            return "\n" +
                    "To verify your account, you will need to use the verification code sent to your email. ✅";
        }
        if (userMessage.contains("login")) {
            return "To log in, provide your registered email and password. 🔏";
        }
        if (userMessage.contains("contact support")) {
            return "If you need help, feel free to contact our support team at nexgin.bank@gmail.com. 💬";
        }
        if (userMessage.contains("convert currency")) {
            return "If you'd like to convert currencies, go to 'Convert Currency' and choose the currency you want to convert to. 💶";
        }
        if (userMessage.contains("purchase")) {
            return "To make a purchase, go to 'Online Shopping', select your payment method, and choose the items you're buying. 🛍️";
        }
        if (userMessage.contains("transfer money")) {
            return "Go to 'Send Money', provide the recipient's IBAN and the amount, then press 'Transfer'. 🤑";
            }
        if (userMessage.contains("reset my password")) {
            return "Contact our support team at nexgin.bank@gmail.com. 🗝️";
        }

        return "Sorry, I didn't quite understand that. Can you rephrase your question? ❌";
    }

//    private String extractAccountNumber(String message) {
//        // Extract account number from the message (assuming account numbers are alphanumeric)
//        String accountNumber = "";
//        String[] words = message.split(" ");
//
//        // Look for a word that matches an account number pattern (alphanumeric, 10-12 characters)
//        for (String word : words) {
//            if (word.matches("[A-Za-z0-9]{10,12}")) {
//                accountNumber = word;
//                break;
//            }
//        }
//
//        return accountNumber;
//    }
}