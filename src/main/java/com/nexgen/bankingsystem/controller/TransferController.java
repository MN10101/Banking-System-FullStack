package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.dto.TransferRequest;
import com.nexgen.bankingsystem.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    @Autowired
    private TransferService transferService;

    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);

    @PostMapping
    public ResponseEntity<?> transferMoney(@RequestBody TransferRequest transferRequest) {
        try {
            // Assuming the TransferRequest object contains fromIban, toIban, amount, and currency
            transferService.processTransfer(
                    transferRequest.getFromIban(),
                    transferRequest.getToIban(),
                    transferRequest.getAmount(),
                    transferRequest.getCurrency()
            );
            return ResponseEntity.ok().body("Transfer successful");
        } catch (Exception e) {
            logger.error("Transfer failed: ", e);
            return ResponseEntity.badRequest().body("Transfer failed: " + e.getMessage());
        }
    }

}

