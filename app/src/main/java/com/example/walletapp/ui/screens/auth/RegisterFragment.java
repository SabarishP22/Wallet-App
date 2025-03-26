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
import com.example.walletapp.databinding.FragmentRegisterBinding;
import com.example.walletapp.models.User;
import com.example.walletapp.ui.base.BaseFragment;
import com.example.walletapp.utils.DialogUtil;
import com.example.walletapp.utils.SessionManager;
import com.example.walletapp.utils.ToastUtil;
import com.example.walletapp.utils.ValidatorUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends BaseFragment {

    private FragmentRegisterBinding registerBinding;

    private AuthViewModel authViewModel;
    private FirebaseAuth firebaseAuth;

    @Inject
    SessionManager sessionManager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        registerBinding = FragmentRegisterBinding.inflate(inflater, container, false);
        return registerBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        firebaseAuth = FirebaseAuth.getInstance();
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
        registerBinding.registerBtn.setOnClickListener(v -> {
            String email = registerBinding.emailEt.getText().toString();
            String password = registerBinding.passwordEt.getText().toString();
            String name = registerBinding.nameEt.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                ToastUtil.showCustomToast(requireContext(), R.string.email_password_name_validation_error);
            } else if (!ValidatorUtil.isValidEmail(email)) {
                ToastUtil.showCustomToast(requireContext(), R.string.email_validation_error);
            } else if (!ValidatorUtil.isValidPassword(password)) {
                ToastUtil.showCustomToast(requireContext(), R.string.password_validation_error);
            } else if (!registerBinding.termsConditionCheck.isChecked()) {
                ToastUtil.showCustomToast(requireContext(), R.string.terms_and_condition_desc);
            } else {
                showLoader(true);
                authViewModel.registerUser(email, password);
            }
        });

        registerBinding.loginLinkTv.setOnClickListener(v -> {
            popBack();
        });
    }

    private void observers() {
        registerObserver();
    }

    private void registerObserver() {
        authViewModel.getRegisterState().observe(getViewLifecycleOwner(), authState -> {
            if (authState.getStatus() == AuthState.Status.LOADING) {
                System.out.println("register loading start");
            } else {
                if (authState.getStatus() == AuthState.Status.SUCCESS) {
                    showLoader(false);
                    sessionManager.saveIsLoggedIn(true);
                    navigateTo(R.id.homeFragment, null);
                    String email = registerBinding.emailEt.getText().toString();
                    String fullName = registerBinding.nameEt.getText().toString();
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        storeUserInFireStore(firebaseUser.getUid(), fullName, email);
                    }
                } else if (authState.getStatus() == AuthState.Status.ERROR) {
                    showLoader(false);
                    sessionManager.saveIsLoggedIn(false);
                }
            }
        });
    }

    private void storeUserInFireStore(String uid, String fullName, String email) {
        User user = new User(uid, fullName, email, "", "", "", "", 0.0, new ArrayList<>());

        authViewModel.registerUserInFireStore(user).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "User registered successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to store user details!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listeners() {
        passwordTxtListener();
    }

    private void passwordTxtListener() {
        EditText passwordEt = registerBinding.passwordEt;
        ImageView showHidePasswordIv = registerBinding.showHidePasswordIv;

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
            int selection = passwordEt.getSelectionStart();

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

            passwordEt.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.popins_600));
        });
    }

    private void handleSpannable() {
        String fullText = getString(R.string.already_have_an_account_log_in);
        String targetText = "Log in";

        SpannableString spannableString = new SpannableString(fullText);
        int startIndex = fullText.indexOf(targetText);
        if (startIndex != -1) {
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue)),
                    startIndex, startIndex + targetText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        registerBinding.loginLinkTv.setText(spannableString);
    }
}
