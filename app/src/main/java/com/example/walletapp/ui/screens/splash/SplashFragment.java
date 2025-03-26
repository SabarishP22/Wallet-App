package com.example.walletapp.ui.screens.splash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.walletapp.R;
import com.example.walletapp.databinding.FragmentSplashBinding;
import com.example.walletapp.ui.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashFragment extends BaseFragment {

    private FragmentSplashBinding splashBinding;
    private SplashViewModel splashViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        splashBinding = FragmentSplashBinding.inflate(inflater, container, false);
        return splashBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        init();
    }

    private void init() {
        animateLogo();
        splashViewModel.getNavigationToHome().observe(getViewLifecycleOwner(), destination -> {
            if (destination != null) {
                navigateTo(destination, null);
            }
        });
    }

    private void animateLogo() {
        Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.small_to_big);
        splashBinding.appIconIv.startAnimation(animation);
    }
}
