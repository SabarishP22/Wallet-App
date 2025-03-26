package com.example.walletapp.ui.screens.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.walletapp.R;
import com.example.walletapp.databinding.FragmentProfileBinding;
import com.example.walletapp.ui.base.BaseFragment;
import com.example.walletapp.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends BaseFragment {

    private FragmentProfileBinding profileBinding;
    private ProfileViewModel profileViewModel;

    @Inject
    SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false);
        return profileBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        init();
    }

    private void init() {
        clickActions();
        observers();
    }

    private void clickActions() {
        profileBinding.logoutBtn.setOnClickListener(v -> {
            profileViewModel.logOut();
            sessionManager.clearLocalData();
            navigateTo(R.id.loginFragment, null);
            if (getActivity() != null) {
                this.resetBottomNavigation();
            }
        });

        profileBinding.feedBackBtn.setOnClickListener(v -> {
            openMailToSendFeedback();
        });
    }

    private void observers() {
        fetchUserDetailsObserver();
    }

    private void fetchUserDetailsObserver() {
        profileViewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                profileBinding.nameTv.setText(user.getFullName());
                profileBinding.emailTv.setText(user.getEmail());
            }
        });
    }

    public void openMailToSendFeedback() {
        String email = "sabarish.p1510@gmail.com";
        String subject = "WalletApp Feedback";
        String body = "Hi, Sabarish P,";

        String uriText = "mailto:" + email +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(body);

        Uri uri = Uri.parse(uriText);
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(uri);

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
}
