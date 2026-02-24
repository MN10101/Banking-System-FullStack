package com.nexgen.bankingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class TransferRequest {
    @NotBlank(message = "From IBAN must not be blank")
    private String fromIban;

    @NotBlank(message = "To IBAN must not be blank")
    private String toIban;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Currency must not be blank")
    private String currency;

    // Getters and setters
    public String getFromIban() {
        return fromIban;
    }

    public void setFromIban(String fromIban) {
        this.fromIban = fromIban;
    }

    public String getToIban() {
        return toIban;
    }

    public void setToIban(String toIban) {
        this.toIban = toIban;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}