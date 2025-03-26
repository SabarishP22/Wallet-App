package com.example.walletapp.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Objects;

public class QRCodeUtils {
    public static Bitmap generateQRCode(String text, int size) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, size, size);
        } catch (WriterException e) {
            Log.e("QRCodeUtilError", Objects.requireNonNull(e.getMessage()));
            return null;
        }
    }
}
