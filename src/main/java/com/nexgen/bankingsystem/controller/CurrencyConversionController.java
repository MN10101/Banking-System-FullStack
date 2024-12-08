package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.dto.ConversionResponse;
import com.nexgen.bankingsystem.service.CurrencyConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/currency")
public class CurrencyConversionController {

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @GetMapping("/convert")
    public ResponseEntity<?> convertCurrency(@RequestParam String from, @RequestParam String to, @RequestParam double amount) {
        System.out.println("Received request to convert from: " + from + ", to: " + to + ", amount: " + amount);
        try {
            // Fetch conversion rate
            double conversionRate = currencyConversionService.getConversionRate(from, to);
            double convertedAmount = amount * conversionRate;
            System.out.println("Conversion rate: " + conversionRate + ", Converted Amount: " + convertedAmount);
            return ResponseEntity.ok().body(new ConversionResponse(convertedAmount));
        } catch (RuntimeException e) {
            // Log error and return appropriate message
            System.err.println("Error converting currency: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error converting currency: " + e.getMessage());
        }
    }


}
