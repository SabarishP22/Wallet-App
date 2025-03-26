package com.example.walletapp.models;

public class Transaction {
    private String transactionId;
    private double amount;
    private String type;  // "income" or "spend"
    private String description;
    private String paymentType;
    private long timestamp;

    public Transaction() {
    } // Required for FireStore

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Transaction(String transactionId, double amount, String type, String description, String paymentType, long timestamp) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.paymentType = paymentType;
        this.timestamp = timestamp;
    }
}

