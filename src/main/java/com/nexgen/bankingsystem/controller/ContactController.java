package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.dto.ContactMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private JavaMailSender emailSender;

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody ContactMessage contactMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("nexgin.bank@gmail.com");
            message.setReplyTo(contactMessage.getEmail());
            message.setSubject("Contact Us Message");
            message.setText("From: " + contactMessage.getEmail() + "\nMessage: " + contactMessage.getMessage());

            emailSender.send(message);
            return ResponseEntity.ok("Message sent successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending message: " + e.getMessage());
        }
    }
}

