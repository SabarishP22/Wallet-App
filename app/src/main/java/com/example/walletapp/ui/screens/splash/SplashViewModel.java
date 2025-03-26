package com.example.walletapp.ui.screens.splash;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.walletapp.R;
import com.example.walletapp.utils.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SplashViewModel extends ViewModel {

    private final MutableLiveData<Integer> navigationToHome = new MutableLiveData<>();
    private final SessionManager sessionManager;

    public LiveData<Integer> getNavigationToHome() {
        return navigationToHome;
    }

    @Inject
    public SplashViewModel(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        startSplashTimer();
    }

    private void startSplashTimer() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean isLoggedIn = sessionManager.fetchIsLoggedIn();
            navigationToHome.setValue(isLoggedIn ? R.id.homeFragment : R.id.loginFragment);
        }, 3000);
    }

}
