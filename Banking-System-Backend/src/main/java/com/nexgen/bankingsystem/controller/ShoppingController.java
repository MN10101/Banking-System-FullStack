package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.dto.PurchaseRequest;
import com.nexgen.bankingsystem.dto.PurchaseResponse;
import com.nexgen.bankingsystem.service.ShoppingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shopping")
public class ShoppingController {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingController.class);

    @Autowired
    private ShoppingServiceImpl onlineShoppingImpl;

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody PurchaseRequest purchaseRequest) {
        try {
            boolean success = onlineShoppingImpl.processPurchase(
                    purchaseRequest.getAccountNumber(),
                    purchaseRequest.getAmount(),
                    purchaseRequest.getPaymentMethod(),
                    purchaseRequest.getCardNumber(),
                    purchaseRequest.getExpMonth(),
                    purchaseRequest.getExpYear(),
                    purchaseRequest.getCvc()
            );

            if (success) {
                // Send a more structured response (e.g., success flag, message)
                return ResponseEntity.ok(new PurchaseResponse(true, "Purchase successful"));
            } else {
                return ResponseEntity.badRequest().body(new PurchaseResponse(false, "Purchase failed. Please check your payment details."));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new PurchaseResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during purchase processing", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PurchaseResponse(false, "Error during purchase: " + e.getMessage()));
        }
    }



    @GetMapping("/api/paypal/success")
    public ResponseEntity<String> handlePaypalSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId
    ) {
        try {
            // Delegate PayPal payment execution to the service
            boolean paymentExecuted = onlineShoppingImpl.executePaypalPayment(paymentId, payerId);

            if (paymentExecuted) {
                return ResponseEntity.ok("Payment successful!");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment execution failed.");
            }
        } catch (Exception e) {
            logger.error("Error executing PayPal payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment execution failed.");
        }
    }


    @GetMapping("/api/paypal/cancel")
    public ResponseEntity<String> handlePaypalCancel() {
        logger.info("PayPal payment canceled by the user.");
        return ResponseEntity.ok("Payment canceled.");
    }
}
