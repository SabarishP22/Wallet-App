package com.example.walletapp.listeners;

public interface FireStoreUserCheckCallback {
    void onUserCheckResult(boolean isUserAvailable, String receiverName);
}
