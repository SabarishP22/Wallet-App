package com.example.walletapp.ui.screens.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.walletapp.models.User;
import com.example.walletapp.repository.ProfileRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final ProfileRepository profileRepository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    @Inject
    public ProfileViewModel(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        fetchUserData();
    }

    public LiveData<User> getUserData() {
        return userLiveData;
    }

    private void fetchUserData() {
        profileRepository.getUserData(new ProfileRepository.OnUserDataFetchedListener() {
            @Override
            public void onSuccess(User user) {
                userLiveData.setValue(user);
            }

            @Override
            public void onFailure(Exception e) {
                // Handle error
            }
        });
    }

    public void logOut() {
        profileRepository.logOut();
    }
}
