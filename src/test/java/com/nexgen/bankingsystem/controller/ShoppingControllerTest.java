package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.dto.PurchaseRequest;
import com.nexgen.bankingsystem.dto.PurchaseResponse;
import com.nexgen.bankingsystem.service.ShoppingServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShoppingController.class)
public class ShoppingControllerTest {

    @MockBean
    private ShoppingServiceImpl onlineShoppingImpl;

    @InjectMocks
    private ShoppingController shoppingController;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testPurchase_Success() throws Exception {
        // Prepare PurchaseRequest
        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setAccountNumber("12345");
        purchaseRequest.setAmount(100.0);
        purchaseRequest.setPaymentMethod("CreditCard");
        purchaseRequest.setCardNumber("4111111111111111");
        purchaseRequest.setExpMonth("12");
        purchaseRequest.setExpYear("2025");
        purchaseRequest.setCvc("123");

        // Mock service method to return success
        when(onlineShoppingImpl.processPurchase(
                purchaseRequest.getAccountNumber(),
                purchaseRequest.getAmount(),
                purchaseRequest.getPaymentMethod(),
                purchaseRequest.getCardNumber(),
                purchaseRequest.getExpMonth(),
                purchaseRequest.getExpYear(),
                purchaseRequest.getCvc()))
                .thenReturn(true);

        // Perform POST request and validate the response
        mockMvc.perform(post("/api/shopping/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"accountNumber\": \"12345\", \"amount\": 100.0, \"paymentMethod\": \"CreditCard\", \"cardNumber\": \"4111111111111111\", \"expMonth\": \"12\", \"expYear\": \"2025\", \"cvc\": \"123\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Purchase successful"));

        verify(onlineShoppingImpl, times(1)).processPurchase(
                purchaseRequest.getAccountNumber(),
                purchaseRequest.getAmount(),
                purchaseRequest.getPaymentMethod(),
                purchaseRequest.getCardNumber(),
                purchaseRequest.getExpMonth(),
                purchaseRequest.getExpYear(),
                purchaseRequest.getCvc());
    }

    @Test
    public void testPurchase_Failure() throws Exception {
        // Prepare PurchaseRequest
        PurchaseRequest purchaseRequest = new PurchaseRequest();
        purchaseRequest.setAccountNumber("12345");
        purchaseRequest.setAmount(100.0);
        purchaseRequest.setPaymentMethod("CreditCard");
        purchaseRequest.setCardNumber("4111111111111111");
        purchaseRequest.setExpMonth("12");
        purchaseRequest.setExpYear("2025");
        purchaseRequest.setCvc("123");

        // Mock service method to return failure
        when(onlineShoppingImpl.processPurchase(
                purchaseRequest.getAccountNumber(),
                purchaseRequest.getAmount(),
                purchaseRequest.getPaymentMethod(),
                purchaseRequest.getCardNumber(),
                purchaseRequest.getExpMonth(),
                purchaseRequest.getExpYear(),
                purchaseRequest.getCvc()))
                .thenReturn(false);

        // Perform POST request and validate the response
        mockMvc.perform(post("/api/shopping/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"accountNumber\": \"12345\", \"amount\": 100.0, \"paymentMethod\": \"CreditCard\", \"cardNumber\": \"4111111111111111\", \"expMonth\": \"12\", \"expYear\": \"2025\", \"cvc\": \"123\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Purchase failed. Please check your payment details."));

        verify(onlineShoppingImpl, times(1)).processPurchase(
                purchaseRequest.getAccountNumber(),
                purchaseRequest.getAmount(),
                purchaseRequest.getPaymentMethod(),
                purchaseRequest.getCardNumber(),
                purchaseRequest.getExpMonth(),
                purchaseRequest.getExpYear(),
                purchaseRequest.getCvc());
    }

    @Test
    public void testPaypalSuccess() throws Exception {
        String paymentId = "PAYID-123";
        String payerId = "PAYER123";

        // Mock the service method for PayPal payment execution
        when(onlineShoppingImpl.executePaypalPayment(paymentId, payerId)).thenReturn(true);

        // Perform GET request and validate the response
        mockMvc.perform(get("/api/shopping/api/paypal/success")
                        .param("paymentId", paymentId)
                        .param("PayerID", payerId))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment successful!"));

        verify(onlineShoppingImpl, times(1)).executePaypalPayment(paymentId, payerId);
    }

    @Test
    public void testPaypalCancel() throws Exception {
        // Perform GET request and validate the response
        mockMvc.perform(get("/api/shopping/api/paypal/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment canceled."));
    }
}
