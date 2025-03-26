package com.example.walletapp.ui.screens.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.walletapp.models.User;
import com.example.walletapp.repository.AuthRepository;
import com.example.walletapp.utils.SingleLiveEvent;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final SingleLiveEvent<AuthState> loginState = new SingleLiveEvent<>();

    public LiveData<AuthState> getLoginState() {
        return loginState;
    }

    private final MutableLiveData<AuthState> registerState = new MutableLiveData<>();

    public LiveData<AuthState> getRegisterState() {
        return registerState;
    }

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void loginUser(String email, String password) {
        loginState.setValue(AuthState.LOADING);
        authRepository.loginUser(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                loginState.setValue(AuthState.SUCCESS);
            }

            @Override
            public void onFailure(String error) {
                loginState.setValue(AuthState.error(error));
            }
        });
    }

    public void registerUser(String email, String password) {
        registerState.setValue(AuthState.LOADING);
        authRepository.registerUser(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                registerState.setValue(AuthState.SUCCESS);
            }

            @Override
            public void onFailure(String error) {
                registerState.setValue(AuthState.error(error));
            }
        });
    }

    public LiveData<Boolean> registerUserInFireStore(User user) {
        return authRepository.registerUserInFireStore(user);
    }

    public void logOut() {
        authRepository.logOutUser();
    }
}
