package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.dto.ExchangeRateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

@Service
public class CurrencyConversionService {

    private final String API_URL = "https://api.exchangerate-api.com/v4/latest/";

    @Autowired
    private RestTemplate restTemplate;

    public double getConversionRate(String fromCurrency, String toCurrency) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(API_URL + fromCurrency).build().toUri();
            System.out.println("Fetching from URL: " + uri);
            ExchangeRateResponse response = restTemplate.getForObject(uri, ExchangeRateResponse.class);

            if (response != null && response.getRates().containsKey(toCurrency)) {
                System.out.println("Rates fetched: " + response.getRates());
                return response.getRates().get(toCurrency);
            } else {
                throw new RuntimeException("Conversion rate not found for " + fromCurrency + " to " + toCurrency);
            }
        } catch (Exception e) {
            System.err.println("Error fetching conversion rate: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unable to fetch conversion rate: " + e.getMessage());
        }
    }

}

