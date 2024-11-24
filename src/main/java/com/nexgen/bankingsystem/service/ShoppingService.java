package com.nexgen.bankingsystem.service;

public interface ShoppingService {
    boolean processPurchase(String accountNumber, double amount, String currency);
}

