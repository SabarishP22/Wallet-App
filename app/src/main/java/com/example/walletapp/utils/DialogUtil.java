package com.example.walletapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.walletapp.R;
import com.example.walletapp.listeners.ConfirmationDialogListener;
import com.example.walletapp.listeners.ErrorDialogListener;

public class DialogUtil {

    public static void showErrorDialog(Context context, Integer desc, Integer header, ErrorDialogListener errorDialogListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.error_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button okBtn = dialogView.findViewById(R.id.okBtn);
        TextView descTv = dialogView.findViewById(R.id.descTv);
        TextView headerTv = dialogView.findViewById(R.id.headerTv);

        headerTv.setText(header);
        descTv.setText(desc);

        okBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (errorDialogListener != null) {
                errorDialogListener.onOkClicked();
            }
        });

        dialog.show();
    }

    public static void showEConfirmationDialog(Context context, Integer desc, Integer header, ConfirmationDialogListener confirmationDialogListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.confirmation_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button proceedBtn = dialogView.findViewById(R.id.proceedBtn);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        TextView descTv = dialogView.findViewById(R.id.descTv);
        TextView headerTv = dialogView.findViewById(R.id.headerTv);

        headerTv.setText(header);
        descTv.setText(desc);

        proceedBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (confirmationDialogListener != null) {
                confirmationDialogListener.onProceedClick();
            }
        });

        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (confirmationDialogListener != null) {
                confirmationDialogListener.onCancelClick();
            }
        });

        dialog.show();
    }
}
