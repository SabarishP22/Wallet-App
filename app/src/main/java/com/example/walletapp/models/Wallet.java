package com.example.walletapp.models;

public class Wallet {
    private String cardHolderName;
    private String last4Digits;
    private String expiryDate;
    private String cvv;

    // Empty constructor for FireStore
    public Wallet() {
    }

    public Wallet(String cardHolderName, String last4Digits, String expiryDate, String cvv) {
        this.cardHolderName = cardHolderName;
        this.last4Digits = last4Digits;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    // Getters and Setters
    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getLast4Digits() {
        return last4Digits;
    }

    public void setLast4Digits(String last4Digits) {
        this.last4Digits = last4Digits;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}

