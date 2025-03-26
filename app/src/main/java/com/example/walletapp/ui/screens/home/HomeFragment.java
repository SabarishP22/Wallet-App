package com.example.walletapp.ui.screens.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.walletapp.R;
import com.example.walletapp.databinding.FragmentHomeBinding;
import com.example.walletapp.listeners.ConfirmationDialogListener;
import com.example.walletapp.listeners.PaymentTypeSelectionCallback;
import com.example.walletapp.models.Wallet;
import com.example.walletapp.repository.HomeRepository;
import com.example.walletapp.ui.base.BaseFragment;
import com.example.walletapp.utils.DialogUtil;
import com.example.walletapp.utils.FormatterUtil;
import com.example.walletapp.utils.QRCodeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.DecimalFormat;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends BaseFragment {
    private FragmentHomeBinding homeBinding;

    private HomeViewmodel homeViewmodel;
    private Dialog qrDialog;

    private Boolean isWalletAdded = false;
    private String paymentMode = "";

    private final ActivityResultLauncher<ScanOptions> qrScannerLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedUUID = result.getContents();
                    Log.d("QRScanner", "Scanned UUID: " + scannedUUID);
                    homeViewmodel.checkUser(scannedUUID, (isUserAvailable, receiverName) -> {
                        if (isUserAvailable) {
                            showSentAmountDialog(requireContext(), scannedUUID, receiverName);
                        } else {
                            showErrorPopup();
                        }
                    });
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return homeBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewmodel = new ViewModelProvider(this).get(HomeViewmodel.class);
        listenStoreUpdates();
        setBottomMenuVisibility(true);
        init();
    }

    private void listenStoreUpdates() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        homeViewmodel.listenWalletUpdates(userId, amount -> {
            if (qrDialog != null && qrDialog.isShowing()) {
                qrDialog.dismiss();
                showTransactionLottie(R.raw.transaction_success, R.string.payment_success);
            }
        });
        homeViewmodel.fetchTotalSpentAndIncome(userId);
    }

    private void init() {
        clickActions();
        observers();
    }

    private void clickActions() {
        homeBinding.addWalletBtn.setOnClickListener(v -> showAddWalletDialog(requireContext()));

        homeBinding.myQrIv.setOnClickListener(v -> showQRCodeDialog());

        homeBinding.addAmountToWalletBtn.setOnClickListener(v -> showAddAmountToWalletDialog(requireContext()));

        homeBinding.scanQrIv.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan a QR Code");
            options.setBeepEnabled(false);
            options.setBarcodeImageEnabled(true);
            options.setOrientationLocked(false);
            options.setCaptureActivity(CustomCaptureActivity.class); // Opens camera for scanning

            qrScannerLauncher.launch(options);
        });

        homeBinding.removeWalletBtn.setOnClickListener(v -> showRemoveWalletConfirmationPopup());
    }

    private void observers() {
        getUserDetailsObserver();
        observeTransactionStatus();
        spentIncomeObserver();
        removeWalletObserver();
    }

    private void getUserDetailsObserver() {
        homeViewmodel.getCurrentUserDetails().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                homeBinding.nameTv.setText(result.getFullName());

                // set wallet details
                if (result.getWallet() != null) {
                    isWalletAdded = true;
                    homeBinding.lastFourDigitTv.setText(result.getWallet().getLast4Digits());
                    homeBinding.expiredTv.setText(result.getWallet().getExpiryDate());
                    homeBinding.cardHolderTv.setText(result.getWallet().getCardHolderName());
                } else {
                    isWalletAdded = false;
                }

                // common details
                String formattedAmount = FormatterUtil.formatAmount(result.getWalletBalance());
                homeBinding.totalBalanceTv.setText(formattedAmount);
                handleWalletVisibility();
            }
        });
    }

    private void observeTransactionStatus() {
        homeViewmodel.getTransactionStatus().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                showLoader(false);
                showTransactionLottie(R.raw.transaction_success, R.string.payment_success);
            } else {
                showLoader(false);
                showTransactionLottie(R.raw.transaction_failed, R.string.payment_failed);
                Toast.makeText(getContext(), "Transaction Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void spentIncomeObserver() {
        homeViewmodel.getTotalSpentIncomeLiveData().observe(getViewLifecycleOwner(), totalData -> {
            if (totalData != null) {
                String formattedSpent = FormatterUtil.formatAmount(totalData.first);
                String formattedIncome = FormatterUtil.formatAmount(totalData.second);
                homeBinding.spentAmountTv.setText(getString(R.string.add_minus, formattedSpent));
                homeBinding.incomeAmountTv.setText(getString(R.string.add_plus, formattedIncome));
            }
        });
    }

    private void removeWalletObserver() {
        homeViewmodel.getWalletRemovedStatus().observe(getViewLifecycleOwner(), isRemoved -> {
            if (isRemoved) {
                DialogUtil.showErrorDialog(requireContext(), R.string.wallet_remove_desc, R.string.success, () -> {
                });
            } else {
                DialogUtil.showErrorDialog(requireContext(), R.string.wallet_remove_failed_desc, R.string.whoops, () -> {
                });
            }
        });
    }

    private void handleWalletVisibility() {
        if (isWalletAdded) {
            homeBinding.cardCv.setVisibility(View.VISIBLE);
            homeBinding.cardCvTop1.setVisibility(View.VISIBLE);
            homeBinding.cardCvTop2.setVisibility(View.VISIBLE);
            homeBinding.addAmountToWalletBtn.setVisibility(View.VISIBLE);
            homeBinding.removeWalletBtn.setVisibility(View.VISIBLE);
            homeBinding.scanQrIv.setEnabled(true);
            homeBinding.scanQrIv.setAlpha(1f);
            homeBinding.myQrIv.setEnabled(true);
            homeBinding.myQrIv.setAlpha(1f);
            homeBinding.totalBalanceTv.setAlpha(1f);
            homeBinding.spentAmountTv.setAlpha(1f);
            homeBinding.incomeAmountTv.setAlpha(1f);
            homeBinding.addWalletBtn.setVisibility(View.GONE);
        } else {
            homeBinding.cardCv.setVisibility(View.GONE);
            homeBinding.cardCvTop1.setVisibility(View.GONE);
            homeBinding.cardCvTop2.setVisibility(View.GONE);
            homeBinding.addAmountToWalletBtn.setVisibility(View.GONE);
            homeBinding.removeWalletBtn.setVisibility(View.GONE);
            homeBinding.scanQrIv.setEnabled(false);
            homeBinding.scanQrIv.setAlpha(0.4f);
            homeBinding.myQrIv.setEnabled(false);
            homeBinding.myQrIv.setAlpha(0.4f);
            homeBinding.totalBalanceTv.setAlpha(0.4f);
            homeBinding.spentAmountTv.setAlpha(0.4f);
            homeBinding.incomeAmountTv.setAlpha(0.4f);
            homeBinding.addWalletBtn.setVisibility(View.VISIBLE);
        }
    }

    private void showAddWalletDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.add_wallet_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView cancelIv = dialogView.findViewById(R.id.cancelIv);
        Button addWalletBtn = dialogView.findViewById(R.id.addWalletBtn);
        EditText lastFourDigitEt = dialogView.findViewById(R.id.lastFourDigitEt);
        EditText expiredEt = dialogView.findViewById(R.id.expiredEt);
        EditText cardHolderEt = dialogView.findViewById(R.id.cardHolderEt);
        EditText cvvEt = dialogView.findViewById(R.id.cvvEt);

        cancelIv.setOnClickListener(v -> dialog.dismiss());

        addWalletBtn.setOnClickListener(v -> {
            String lastFourDigits = lastFourDigitEt.getText().toString().trim();
            String expiryDate = expiredEt.getText().toString().trim();
            String cardHolderName = cardHolderEt.getText().toString().trim();
            String cvv = cvvEt.getText().toString().trim();

            if (lastFourDigits.isEmpty() || expiryDate.isEmpty() || cardHolderName.isEmpty() || cvv.isEmpty()) {
                DialogUtil.showErrorDialog(requireContext(), R.string.enter_all_field_wallet, R.string.whoops, () -> {
                });
            } else {
                Wallet wallet = new Wallet(
                        cardHolderName,
                        lastFourDigits,
                        expiryDate,
                        cvv
                );
                Log.i("WALLET_DETAILS::::::::", wallet.toString());

                homeViewmodel.updateWallet(wallet, new HomeRepository.OnWalletUpdateListener() {
                    @Override
                    public void onSuccess() {
                        isWalletAdded = true;
                        getUserDetailsObserver();
                        dialog.dismiss();
                        DialogUtil.showErrorDialog(requireContext(), R.string.wallet_added_desc, R.string.success, () -> {
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(context, "Failed to update wallet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        isWalletAdded = false;
                        getUserDetailsObserver();
                        dialog.dismiss();
                    }
                });
            }
        });

        homeViewmodel.getIsWalletUpdated().observe(getViewLifecycleOwner(), isUpdated -> {
            if (isUpdated) {
                Toast.makeText(getContext(), "Wallet Created Successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to Create Wallet", Toast.LENGTH_SHORT).show();
            }
        });

        expiredEt.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false; // To prevent infinite loop

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isEditing) return;
                isEditing = true;

                String input = s.toString().replaceAll("[^0-9]", ""); // Remove non-numeric chars
                String formatted;

                if (input.length() >= 2) {
                    formatted = input.substring(0, 2);
                    if (input.length() > 2) {
                        formatted += "/" + input.substring(2, Math.min(input.length(), 4));
                    }
                } else {
                    formatted = input;
                }

                expiredEt.setText(formatted);
                expiredEt.setSelection(formatted.length()); // Move cursor to end

                isEditing = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        dialog.show();
    }

    private void handlePopupMenu(TextView selectedPaymentMethodTv, ImageView dropdownIconIv, Context context, PaymentTypeSelectionCallback callback) {
        String[] paymentMethods = {"Credit Card", "Debit Card", "Bank Account", "Money Cash"};

        dropdownIconIv.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, selectedPaymentMethodTv);
            for (String method : paymentMethods) {
                popupMenu.getMenu().add(method);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                selectedPaymentMethodTv.setText(item.getTitle());
                String selectedMethod = Objects.requireNonNull(item.getTitle()).toString();
                callback.onItemSelected(selectedMethod);
                return true;
            });

            popupMenu.show();
        });
    }

    private void showAddAmountToWalletDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.add_amount_to_wallet_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView cancelIv = dialogView.findViewById(R.id.cancelIv);
        Button addAmountBtn = dialogView.findViewById(R.id.addAmountBtn);
        EditText enterAmountToAddEt = dialogView.findViewById(R.id.enterAmountToAddEt);

        cancelIv.setOnClickListener(v -> dialog.dismiss());

        addAmountBtn.setOnClickListener(v -> {
            String enteredAmountStr = enterAmountToAddEt.getText().toString().trim();
            if (enteredAmountStr.isEmpty()) {
                DialogUtil.showErrorDialog(requireContext(), R.string.enter_amount_error, R.string.whoops, () -> {
                });
                return;
            }
            String sanitizedAmountStr = enteredAmountStr.replace("₹", "").replace(",", "").trim();
            double enteredAmount = sanitizedAmountStr.isEmpty() ? 0.0 : Double.parseDouble(sanitizedAmountStr);
            homeViewmodel.addMoney(enteredAmount, new HomeRepository.OnWalletUpdateListener() {
                @Override
                public void onSuccess() {
                    DialogUtil.showErrorDialog(requireContext(), R.string.amount_added_to_wallet, R.string.success, () -> {
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    DialogUtil.showErrorDialog(requireContext(), R.string.amount_added_to_wallet_failed, R.string.whoops, () -> {
                    });
                }
            });
            dialog.dismiss();
        });

        enterAmountToAddEt.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String input = s.toString().replace("₹", "").replace(",", "").trim();

                if (!input.isEmpty()) {
                    try {
                        long parsed = Long.parseLong(input);
                        String formatted = formatIndianNumber(parsed);

                        enterAmountToAddEt.setText("₹" + formatted);
                        enterAmountToAddEt.setSelection(enterAmountToAddEt.getText().length());
                    } catch (NumberFormatException e) {
                        Log.e("Error", Objects.requireNonNull(e.getMessage()));
                    }
                } else {
                    enterAmountToAddEt.setText("₹");
                    enterAmountToAddEt.setSelection(enterAmountToAddEt.getText().length());
                }

                isEditing = false;
            }
        });

        dialog.show();
    }

    private String formatIndianNumber(long value) {
        DecimalFormat formatter = new DecimalFormat("#,##,##0");
        return formatter.format(value);
    }

    private void showQRCodeDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userUID = user.getUid();
        Bitmap qrCodeBitmap = QRCodeUtils.generateQRCode(userUID, 700);

        if (qrCodeBitmap != null) {
            qrDialog = new Dialog(requireContext());
            qrDialog.setContentView(R.layout.dialog_qr_code);
            Objects.requireNonNull(qrDialog.getWindow()).setDimAmount(0.5f);
            ImageView qrImageView = qrDialog.findViewById(R.id.qrCodeIv);
            qrImageView.setImageBitmap(qrCodeBitmap);
            qrDialog.show();
        }
    }

    private void showSentAmountDialog(Context context, String scannedUUID, String receiverName) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.send_amount_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView cancelIv = dialogView.findViewById(R.id.cancelIv);
        Button payAmountBtn = dialogView.findViewById(R.id.payAmountBtn);
        EditText enterAmountToPayEt = dialogView.findViewById(R.id.enterAmountToPayEt);
        TextView payingToTv = dialogView.findViewById(R.id.payingToTv);

        TextView selectedPaymentMethodTv = dialogView.findViewById(R.id.selectedPaymentMethodTv);
        ImageView dropdownIconIv = dialogView.findViewById(R.id.dropdownIconIv);
        handlePopupMenu(selectedPaymentMethodTv, dropdownIconIv, requireContext(), selectedItem -> paymentMode = selectedItem);

        cancelIv.setOnClickListener(v -> dialog.dismiss());
        payingToTv.setText(getString(R.string.paying_to, receiverName));

        payAmountBtn.setOnClickListener(v -> {
            showLoader(true);
            String enteredAmountStr = enterAmountToPayEt.getText().toString().trim();
            String sanitizedAmountStr = enteredAmountStr.replace("₹", "").replace(",", "").trim();
            double enteredAmount = sanitizedAmountStr.isEmpty() ? 0.0 : Double.parseDouble(sanitizedAmountStr);
            homeViewmodel.processTransaction(enteredAmount, scannedUUID, paymentMode);
            dialog.dismiss();
        });

        enterAmountToPayEt.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String input = s.toString().replace("₹", "").replace(",", "").trim();

                if (!input.isEmpty()) {
                    try {
                        long parsed = Long.parseLong(input);
                        String formatted = formatIndianNumber(parsed);

                        enterAmountToPayEt.setText("₹" + formatted);
                        enterAmountToPayEt.setSelection(enterAmountToPayEt.getText().length());
                    } catch (NumberFormatException e) {
                        Log.e("Error", Objects.requireNonNull(e.getMessage()));
                    }
                } else {
                    enterAmountToPayEt.setText("₹");
                    enterAmountToPayEt.setSelection(enterAmountToPayEt.getText().length());
                }

                isEditing = false;
            }
        });

        dialog.show();
    }

    private void showErrorPopup() {
        DialogUtil.showErrorDialog(requireContext(), R.string.scanned_fail_desc, R.string.whoops, () -> {
        });
    }

    private void showRemoveWalletConfirmationPopup() {
        DialogUtil.showEConfirmationDialog(requireContext(), R.string.remove_wallet_confirmation_desc, R.string.remove_wallet_confirmation_head, new ConfirmationDialogListener() {
            @Override
            public void onProceedClick() {
                String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                homeViewmodel.removeWallet(userId);
            }

            @Override
            public void onCancelClick() {

            }
        });
    }
}
