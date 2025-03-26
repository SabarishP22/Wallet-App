package com.example.walletapp.ui.screens.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.walletapp.R;
import com.example.walletapp.databinding.FragmentLoginBinding;
import com.example.walletapp.ui.base.BaseFragment;
import com.example.walletapp.utils.DialogUtil;
import com.example.walletapp.utils.SessionManager;
import com.example.walletapp.utils.ToastUtil;
import com.example.walletapp.utils.ValidatorUtil;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends BaseFragment {

    private FragmentLoginBinding loginBinding;

    private AuthViewModel authViewModel;

    @Inject
    SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loginBinding = FragmentLoginBinding.inflate(inflater, container, false);
        return loginBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setBottomMenuVisibility(false);
        init();
    }

    private void init() {
        clickActions();
        observers();
        listeners();
        handleSpannable();
    }

    private void clickActions() {
        loginBinding.loginBtn.setOnClickListener(v -> {
            String email = loginBinding.emailEt.getText().toString();
            String password = loginBinding.passwordEt.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                ToastUtil.showCustomToast(requireContext(), R.string.email_password_empty_error);
            } else if (!ValidatorUtil.isValidEmail(email)) {
                ToastUtil.showCustomToast(requireContext(), R.string.email_validation_error);
            } else if (!ValidatorUtil.isValidPassword(password)) {
                ToastUtil.showCustomToast(requireContext(), R.string.password_validation_error);
            } else {
                showLoader(true);
                authViewModel.loginUser(email, password);
            }
        });

        loginBinding.registerLinkTv.setOnClickListener(v -> navigateTo(R.id.registerFragment, null));

        loginBinding.forgotPasswordTv.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Forgot Password", Toast.LENGTH_SHORT).show();
        });
    }

    private void observers() {
        loginObserver();
    }

    private void loginObserver() {
        authViewModel.getLoginState().observe(getViewLifecycleOwner(), authState -> {
            if (authState.getStatus() == AuthState.Status.LOADING) {
                System.out.println("login loading start");
            } else {
                if (authState.getStatus() == AuthState.Status.SUCCESS) {
                    showLoader(false);
                    sessionManager.saveIsLoggedIn(true);
                    navigateTo(R.id.homeFragment, null);
                } else if (authState.getStatus() == AuthState.Status.ERROR) {
                    showLoader(false);
                    ToastUtil.showCustomToast(requireContext(), R.string.user_not_found_desc);
                    sessionManager.saveIsLoggedIn(false);
                }
            }
        });
    }

    private void listeners() {
        passwordTxtListener();
    }

    private void passwordTxtListener() {
        EditText passwordEt = loginBinding.passwordEt;
        ImageView showHidePasswordIv = loginBinding.showHidePasswordIv;

        // Show/hide eye icon based on text input
        passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showHidePasswordIv.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        showHidePasswordIv.setOnClickListener(v -> {
            int selection = passwordEt.getSelectionStart(); // Store cursor position

            if (passwordEt.getTransformationMethod() instanceof PasswordTransformationMethod) {
                // Show password
                passwordEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showHidePasswordIv.setImageResource(R.drawable.hide);
            } else {
                // Hide password
                passwordEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showHidePasswordIv.setImageResource(R.drawable.view);
            }

            // Restore cursor position
            passwordEt.setSelection(selection);

            // Reapply font to prevent UI reset issue
            passwordEt.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.popins_600));
        });
    }

    private void handleSpannable() {
        String fullText = getString(R.string.don_t_have_an_account_register);
        String targetText = "Register";

        SpannableString spannableString = new SpannableString(fullText);
        int startIndex = fullText.indexOf(targetText);
        if (startIndex != -1) {
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue)),
                    startIndex, startIndex + targetText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        loginBinding.registerLinkTv.setText(spannableString);
    }
}
