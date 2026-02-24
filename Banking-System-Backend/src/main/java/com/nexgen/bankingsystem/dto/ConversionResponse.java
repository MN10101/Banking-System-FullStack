package com.nexgen.bankingsystem.dto;

public class ConversionResponse {
    private double convertedAmount;

    public ConversionResponse(double convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public double getConvertedAmount() {
        return convertedAmount;
    }
}
