package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.dto.ContactMessage;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ContactControllerTest {

    @MockBean
    private JavaMailSender emailSender;

    @InjectMocks
    private ContactController contactController;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSendMessage_Success() throws Exception {
        // Create a mock ContactMessage object
        ContactMessage contactMessage = new ContactMessage();
        contactMessage.setEmail("test@me.com");
        contactMessage.setMessage("This is a test message.");

        // Mock the send method of the JavaMailSender
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        // Perform the POST request and check the response
        mockMvc.perform(post("/api/contact")
                        .contentType("application/json")
                        .content("{\"email\":\"test@me.com\", \"message\":\"This is a test message.\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Message sent successfully!"));

        // Verify that the emailSender's send method was called once
        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendMessageFailure() throws Exception {
        // Arrange: Create a mock ContactMessage object
        ContactMessage contactMessage = new ContactMessage();
        contactMessage.setEmail("test@me.com");
        contactMessage.setMessage("This is a test message.");

        // Mock the send method to throw an exception
        doThrow(new RuntimeException("Error sending message")).when(emailSender).send(any(SimpleMailMessage.class));

        // Act & Assert: Perform the POST request and check the response
        mockMvc.perform(post("/api/contact")
                        .contentType("application/json")
                        .content("{\"email\":\"test@me.com\", \"message\":\"This is a test message.\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error sending message: Error sending message"));

        // Verify that the emailSender's send method was called once
        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
