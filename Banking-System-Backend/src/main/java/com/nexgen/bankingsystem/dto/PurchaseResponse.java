package com.nexgen.bankingsystem.dto;

public class PurchaseResponse {
    private boolean success;
    private String message;

    // Constructor, getters, setters
    public PurchaseResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
