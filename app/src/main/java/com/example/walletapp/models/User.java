package com.example.walletapp.models;

import java.util.List;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String qrCodeUrl;
    private double walletBalance;
    private List<Transaction> transactions;
    private Wallet wallet;

    public User() {
    } // Required for FireStore

    public User(String uid, String fullName, String email,
                String lastFourDigits, String expiryDate, String cvv,
                String qrCodeUrl, double walletBalance, List<Transaction> transactions) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.qrCodeUrl = qrCodeUrl;
        this.walletBalance = walletBalance;
        this.transactions = transactions;
    }

    // Getters and Setters (Required for FireStore)

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}

