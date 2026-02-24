package com.nexgen.bankingsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ChatBotServiceTest {

    private final ChatBotService chatBotService = new ChatBotService();

    @Test
    void returnsGreetingForHiMessage() {
        // Given
        String userMessage = "hi there";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("Hi you, how are you doing today? 😃");
    }

    @Test
    void returnsFollowUpForFineResponse() {
        // Given
        String userMessage = "I'm fine thanks, how about you?";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("I'm doing great, thanks!. 🥰");
    }

    @Test
    void returnsAppDescriptionForAboutQuery() {
        // Given
        String userMessage = "tell me about the app";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("This app is a secure banking platform developed by Nexgin, offering services such as account creation, transfers, currency conversion, and more. 🚀");
    }

    @Test
    void explainsAccountCreationProcess() {
        // Given
        String userMessage = "how to create account";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("To create an account, you'll need to provide your email and some account details. ✨");
    }

    @Test
    void explainsRegistrationProcess() {
        // Given
        String userMessage = "how do I register";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("To register, we need some basic personal information like your name, email, and password, etc. 🪧");
    }

    @Test
    void explainsAccountVerificationSteps() {
        // Given
        String userMessage = "verify account steps";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("\nTo verify your account, you will need to use the verification code sent to your email. ✅");
    }

    @Test
    void explainsLoginProcess() {
        // Given
        String userMessage = "how to login";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("To log in, provide your registered email and password. 🔏");
    }

    @Test
    void providesSupportContactInformation() {
        // Given
        String userMessage = "contact support team";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("If you need help, feel free to contact our support team at nexgin.bank@gmail.com. 💬");
    }

    @Test
    void explainsCurrencyConversion() {
        // Given
        String userMessage = "how to convert currency";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("If you'd like to convert currencies, go to 'Convert Currency' and choose the currency you want to convert to. 💶");
    }

    @Test
    void explainsPurchaseProcess() {
        // Given
        String userMessage = "make a purchase";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("To make a purchase, go to 'Online Shopping', select your payment method, and choose the items you're buying. 🛍️");
    }

    @Test
    void explainsMoneyTransferProcess() {
        // Given
        String userMessage = "how to transfer money";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("Go to 'Send Money', provide the recipient's IBAN and the amount, then press 'Transfer'. 🤑");
    }

    @Test
    void explainsPasswordResetProcess() {
        // Given
        String userMessage = "reset my password please";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("Contact our support team at nexgin.bank@gmail.com. 🗝️");
    }

    @Test
    void returnsDefaultResponseForUnknownQueries() {
        // Given
        String userMessage = "random question";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("Sorry, I didn't quite understand that. Can you rephrase your question? ❌");
    }

    @Test
    void handlesUrlEncodedMessages() {
        // Given
        String userMessage = null;
        try {
            userMessage = URLEncoder.encode("hi there!", StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("Hi you, how are you doing today? 😃");
    }

    @Test
    void handlesInvalidUrlEncodedMessages() {
        // Given
        String userMessage = "%";

        // When
        String response = chatBotService.getResponse(userMessage);

        // Then
        assertThat(response).isEqualTo("Sorry, there was an issue decoding your message.");
    }
}