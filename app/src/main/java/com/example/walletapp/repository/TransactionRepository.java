package com.example.walletapp.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.walletapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;

public class TransactionRepository {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    @Inject
    public TransactionRepository(FirebaseFirestore firestore, FirebaseAuth firebaseAuth) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        listenForUserUpdates();
    }

    private void listenForUserUpdates() {
        String uid = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        DocumentReference userRef = firestore.collection("users").document(uid);
        userRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e("FireStore", "Listen failed", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                User updatedUser = documentSnapshot.toObject(User.class);
                if (updatedUser != null) {
                    userLiveData.setValue(updatedUser);
                    Log.d("FireStore", "User data updated in real-time!");
                }
            }
        });
    }

    public LiveData<User> getUserData() {
        return userLiveData;
    }
}
