package com.example.walletapp.ui.screens.home;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.walletapp.listeners.FireStoreUserCheckCallback;
import com.example.walletapp.listeners.WalletUpdateListener;
import com.example.walletapp.models.User;
import com.example.walletapp.models.Wallet;
import com.example.walletapp.repository.HomeRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewmodel extends ViewModel {

    private final HomeRepository homeRepository;

    private final MutableLiveData<Boolean> isWalletUpdated = new MutableLiveData<>();

    public LiveData<Boolean> getIsWalletUpdated() {
        return isWalletUpdated;
    }

    private final LiveData<User> userLiveData;

    public LiveData<User> getCurrentUserDetails() {
        return userLiveData;
    }

    private final MutableLiveData<Boolean> transactionStatus = new MutableLiveData<>();

    public LiveData<Boolean> getTransactionStatus() {
        return transactionStatus;
    }

    private final MutableLiveData<Pair<Double, Double>> totalSpentIncomeLiveData = new MutableLiveData<>();

    public LiveData<Pair<Double, Double>> getTotalSpentIncomeLiveData() {
        return totalSpentIncomeLiveData;
    }

    private final MutableLiveData<Boolean> isWalletRemoved = new MutableLiveData<>();

    public LiveData<Boolean> getWalletRemovedStatus() {
        return isWalletRemoved;
    }

    @Inject
    public HomeViewmodel(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
        this.userLiveData = homeRepository.getCurrentUserDetails();
    }

    public void updateWallet(Wallet wallet, HomeRepository.OnWalletUpdateListener listener) {
        homeRepository.updateWalletDetails(wallet, listener);
    }

    public void addMoney(double amount, HomeRepository.OnWalletUpdateListener listener) {
        homeRepository.addMoneyToWallet(amount, listener);
    }

    public void checkUser(String userId, FireStoreUserCheckCallback callback) {
        homeRepository.checkUserInFireStore(userId, callback);
    }

    public void processTransaction(double amount, String receiverUid, String paymentType) {
        homeRepository.processTransaction(amount, receiverUid, paymentType).observeForever(transactionStatus::setValue);
    }

    public void listenWalletUpdates(String userId, WalletUpdateListener walletUpdateListener) {
        homeRepository.listenForWalletUpdates(userId, walletUpdateListener);
    }

    public void fetchTotalSpentAndIncome(String userId) {
        homeRepository.getTotalSpentAndIncome(userId).observeForever(totalSpentIncomeLiveData::setValue);
    }

    public void removeWallet(String userId) {
        homeRepository.removeWallet(userId, task -> {
            if (task.isSuccessful()) {
                isWalletRemoved.postValue(true);
                homeRepository.fetchUserDetails();
            } else {
                isWalletRemoved.postValue(false);
            }
        });
    }
}
