package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<String> askQuestion(@RequestBody String userMessage) {
        System.out.println("Received message: " + userMessage);
        String response = chatbotService.getResponse(userMessage);
        return ResponseEntity.ok(response);
    }
}
