package com.example.walletapp.repository;

import com.example.walletapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;

public class ProfileRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    @Inject
    public ProfileRepository(FirebaseAuth firebaseAuth, FirebaseFirestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    public String getUserId() {
        return firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
    }

    public void getUserData(OnUserDataFetchedListener listener) {
        String userId = getUserId();
        if (userId == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }

        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                listener.onSuccess(user);
            } else {
                listener.onFailure(new Exception("User data not found"));
            }
        }).addOnFailureListener(listener::onFailure);
    }

    public void logOut() {
        firebaseAuth.signOut();
    }

    public interface OnUserDataFetchedListener {
        void onSuccess(User user);

        void onFailure(Exception e);
    }
}
