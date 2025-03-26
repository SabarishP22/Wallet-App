package com.example.walletapp.ui.screens.transactions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.walletapp.models.User;
import com.example.walletapp.repository.TransactionRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransactionViewModel extends ViewModel {

    private final TransactionRepository transactionRepository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    @Inject
    public TransactionViewModel(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        fetchUserDetails();
    }

    public void fetchUserDetails() {
        transactionRepository.getUserData().observeForever(user -> {
            if (user != null) {
                userLiveData.setValue(user);
            }
        });
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }
}
