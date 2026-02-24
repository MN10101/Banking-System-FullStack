package com.nexgen.bankingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexgen.bankingsystem.dto.TransferRequest;
import com.nexgen.bankingsystem.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transferController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void transfersMoney() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromIban("DE54500202220172720185");
        request.setToIban("DE52500202220657984061");
        request.setAmount(1000.0);
        request.setCurrency("EUR");

        doNothing().when(transferService).processTransfer(
                anyString(), anyString(), anyDouble(), anyString());

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successful"));
    }

    @Test
    public void failsToTransferIfInsufficientFunds() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromIban("DE54500202220172720185");
        request.setToIban("DE52500202220657984061");
        request.setAmount(40000.0);
        request.setCurrency("EUR");

        doThrow(new RuntimeException("Transfer failed due to insufficient funds or invalid IBAN."))
                .when(transferService).processTransfer(
                        anyString(), anyString(), anyDouble(), anyString());

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Transfer failed: Transfer failed due to insufficient funds or invalid IBAN."));
    }

    @Test
    public void failsToTransferIfInvalidIban() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromIban("INVALID_IBAN");
        request.setToIban("DE52500202220657984061");
        request.setAmount(1000.0);
        request.setCurrency("EUR");

        doThrow(new RuntimeException("Transfer failed due to insufficient funds or invalid IBAN."))
                .when(transferService).processTransfer(
                        anyString(), anyString(), anyDouble(), anyString());

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Transfer failed: Transfer failed due to insufficient funds or invalid IBAN."));
    }

    @Test
    public void failsToTransferIfNegativeAmount() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromIban("DE54500202220172720185");
        request.setToIban("DE52500202220657984061");
        request.setAmount(-1000.0);
        request.setCurrency("EUR");

        doThrow(new RuntimeException("Transfer failed due to insufficient funds or invalid IBAN."))
                .when(transferService).processTransfer(
                        anyString(), anyString(), anyDouble(), anyString());

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Transfer failed: Transfer failed due to insufficient funds or invalid IBAN."));
    }

    @Test
    public void failsToTransferIfFieldsMissing() throws Exception {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromIban("DE54500202220172720185");
        // toIban, amount, and currency are null

        doThrow(new RuntimeException("Transfer failed due to insufficient funds or invalid IBAN."))
                .when(transferService).processTransfer(
                        anyString(), anyString(), anyDouble(), anyString());

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Transfer failed")));
    }
}