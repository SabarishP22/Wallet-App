package com.example.walletapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.walletapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import javax.inject.Inject;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    @Inject
    public AuthRepository(FirebaseAuth firebaseAuth, FirebaseFirestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    public void loginUser(String email, String password, AuthCallback authCallback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        authCallback.onSuccess();
                    } else {
                        authCallback.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    public void registerUser(String email, String password, AuthCallback authCallback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        authCallback.onSuccess();
                    } else {
                        authCallback.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    public LiveData<Boolean> registerUserInFireStore(User user) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        firestore.collection("users").document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void logOutUser() {
        firebaseAuth.signOut();
    }

    public interface AuthCallback {
        void onSuccess();

        void onFailure(String error);
    }
}
