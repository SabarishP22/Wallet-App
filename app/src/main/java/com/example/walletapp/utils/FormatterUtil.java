package com.example.walletapp.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatterUtil {
    public static String formatAmount(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return "₹" + formatter.format(amount);
    }
}