package com.example.walletapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class SessionManager {
    private static final String SESSION_PREFS = "SessionPreferences";
    private static final String IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;

    @Inject
    public SessionManager(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
    }

    public void saveIsLoggedIn(Boolean isLoggedIn) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public Boolean fetchIsLoggedIn() {
        return prefs.getBoolean(IS_LOGGED_IN, false);
    }

    public void clearLocalData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

}
