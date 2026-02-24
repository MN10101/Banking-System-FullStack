package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.service.CurrencyConversionService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyConversionController.class)
public class CurrencyConversionControllerTest {

    @MockBean
    private CurrencyConversionService currencyConversionService;

    @InjectMocks
    private CurrencyConversionController currencyConversionController;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void testConvertCurrencySuccess() throws Exception {
        String from = "EUR";
        String to = "USD";
        double amount = 100.0;
        double conversionRate = 0.85;
        double expectedConvertedAmount = amount * conversionRate;

        // Mock the service to return the conversion rate
        when(currencyConversionService.getConversionRate(from, to)).thenReturn(conversionRate);

        // Perform the GET request and check the response
        mockMvc.perform(get("/api/currency/convert")
                        .param("from", from)
                        .param("to", to)
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convertedAmount").value(expectedConvertedAmount));

        // Verify that the service's getConversionRate method was called once
        verify(currencyConversionService, times(1)).getConversionRate(from, to);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void testConvertCurrencyError() throws Exception {
        String from = "EUR";
        String to = "USD";
        double amount = 100.0;

        // Mock the service to throw an exception
        when(currencyConversionService.getConversionRate(from, to)).thenThrow(new RuntimeException("Conversion failed"));

        // Perform the GET request and check the response
        mockMvc.perform(get("/api/currency/convert")
                        .param("from", from)
                        .param("to", to)
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isBadRequest()) // Expecting status 400
                .andExpect(content().string("Error converting currency: Conversion failed"));

        // Verify that the service's getConversionRate method was called once
        verify(currencyConversionService, times(1)).getConversionRate(from, to);
    }

}
