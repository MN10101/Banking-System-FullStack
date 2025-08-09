package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.service.ChatBotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ChatBotControllerTest {

    @Mock
    private ChatBotService chatBotService;

    @InjectMocks
    private ChatBotController chatBotController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatBotController).build();
    }

    @Test
    public void respondsToHi() throws Exception {
        String userMessage = "hi";
        String expectedResponse = "Hi you, how are you doing today? ?";
        when(chatBotService.getResponse(anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post("/chatbot/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"hi\""))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void respondsToFine() throws Exception {
        String userMessage = "I'm fine thanks, how about you?";
        String expectedResponse = "I'm doing great, thanks!. ?";
        when(chatBotService.getResponse(anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post("/chatbot/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"I'm fine thanks, how about you?\""))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void respondsToAbout() throws Exception {
        String userMessage = "about";
        String expectedResponse = "This app is a secure banking platform developed by Nexgin, offering services such as account creation, transfers, currency conversion, and more. ?";
        when(chatBotService.getResponse(anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post("/chatbot/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"about\""))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void respondsToCreateAccount() throws Exception {
        String userMessage = "create account";
        String expectedResponse = "To create an account, you'll need to provide your email and some account details. ?";
        when(chatBotService.getResponse(anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post("/chatbot/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"create account\""))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void respondsToUnknown() throws Exception {
        String userMessage = "random query";
        String expectedResponse = "Sorry, I didn't quite understand that. Can you rephrase your question? ?";
        when(chatBotService.getResponse(anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post("/chatbot/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"random query\""))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void respondsToURLEncoded() throws Exception {
        String userMessage = "hi%20there";
        String expectedResponse = "Hi you, how are you doing today? ?";
        // Stub the encoded input as received by the controller
        when(chatBotService.getResponse("\"hi%20there\"")).thenReturn(expectedResponse);

        mockMvc.perform(post("/chatbot/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"hi%20there\""))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void respondsToEmpty() throws Exception {
        String userMessage = "";
        String expectedResponse = "Sorry, I didn't quite understand that. Can you rephrase your question? ?";
        when(chatBotService.getResponse(anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post("/chatbot/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"\""))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }
}