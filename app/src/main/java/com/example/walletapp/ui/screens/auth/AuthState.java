package com.example.walletapp.ui.screens.auth;

public class AuthState {

    public static final AuthState LOADING = new AuthState(Status.LOADING, null);
    public static final AuthState SUCCESS = new AuthState(Status.SUCCESS, null);

    private final Status status;
    private final String errorMsg;

    private AuthState(Status status, String errorMsg) {
        this.status = status;
        this.errorMsg = errorMsg;
    }

    public Status getStatus() {
        return status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public static AuthState error(String errorMsg) {
        return new AuthState(Status.ERROR, errorMsg);
    }

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }
}
