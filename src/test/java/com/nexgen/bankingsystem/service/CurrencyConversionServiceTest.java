package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.dto.ExchangeRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class CurrencyConversionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CurrencyConversionService currencyConversionService;

    private ExchangeRateResponse response;

    @BeforeEach
    void setUp() {
        // Initialize a sample ExchangeRateResponse with base and rates
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        rates.put("EUR", 0.85);
        rates.put("GBP", 0.73);
        response = new ExchangeRateResponse("USD", rates);
    }

    @Test
    void returnsConversionRateForValidCurrencies() {
        // Given
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        when(restTemplate.getForObject(any(URI.class), eq(ExchangeRateResponse.class))).thenReturn(response);

        // When
        double rate = currencyConversionService.getConversionRate(fromCurrency, toCurrency);

        // Then
        assertThat(rate).isEqualTo(0.85);
    }

    @Test
    void throwsExceptionWhenTargetCurrencyNotAvailable() {
        // Given
        String fromCurrency = "USD";
        String toCurrency = "JPY";
        when(restTemplate.getForObject(any(URI.class), eq(ExchangeRateResponse.class))).thenReturn(response);

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                currencyConversionService.getConversionRate(fromCurrency, toCurrency));
        assertThat(exception.getMessage()).isEqualTo("Unable to fetch conversion rate: Conversion rate not found for USD to JPY");
    }

    @Test
    void throwsExceptionWhenApiReturnsNull() {
        // Given
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        when(restTemplate.getForObject(any(URI.class), eq(ExchangeRateResponse.class))).thenReturn(null);

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                currencyConversionService.getConversionRate(fromCurrency, toCurrency));
        assertThat(exception.getMessage()).isEqualTo("Unable to fetch conversion rate: Conversion rate not found for USD to EUR");
    }

    @Test
    void throwsExceptionWhenApiCallFails() {
        // Given
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        when(restTemplate.getForObject(any(URI.class), eq(ExchangeRateResponse.class)))
                .thenThrow(new RestClientException("API unavailable"));

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                currencyConversionService.getConversionRate(fromCurrency, toCurrency));
        assertThat(exception.getMessage()).isEqualTo("Unable to fetch conversion rate: API unavailable");
    }
}