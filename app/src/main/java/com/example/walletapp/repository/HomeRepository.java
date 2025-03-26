package com.example.walletapp.repository;

import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.walletapp.listeners.FireStoreUserCheckCallback;
import com.example.walletapp.listeners.WalletUpdateListener;
import com.example.walletapp.models.Transaction;
import com.example.walletapp.models.User;
import com.example.walletapp.models.Wallet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

public class HomeRepository {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    public LiveData<User> getCurrentUserDetails() {
        return userLiveData;
    }

    @Inject
    public HomeRepository(FirebaseFirestore firestore, FirebaseAuth firebaseAuth) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        fetchUserDetails();
    }

    public void updateWalletDetails(Wallet wallet, OnWalletUpdateListener listener) {
        String uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        // Fetch current walletBalance
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double currentBalance = documentSnapshot.getDouble("walletBalance");
                        if (currentBalance == null) {
                            currentBalance = 0.0;
                        }

                        // Update both wallet and walletBalance in FireStore
                        firestore.collection("users").document(uid)
                                .update("wallet", wallet, "walletBalance", currentBalance)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("UserRepository", "Wallet & balance updated successfully");
                                    fetchUserDetails(); // Refresh user data
                                    listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UserRepository", "Error updating wallet & balance", e);
                                    listener.onFailure(e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error fetching current walletBalance", e);
                    listener.onFailure(e);
                });
    }

    public void addMoneyToWallet(double amountToAdd, OnWalletUpdateListener listener) {
        String uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double currentBalance = documentSnapshot.getDouble("walletBalance");
                        if (currentBalance == null) {
                            currentBalance = 0.0;
                        }

                        double updatedBalance = currentBalance + amountToAdd;

                        // Update only walletBalance in FireStore
                        firestore.collection("users").document(uid)
                                .update("walletBalance", updatedBalance)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("UserRepository", "Wallet balance updated successfully");
                                    fetchUserDetails(); // Refresh user data
                                    listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UserRepository", "Error updating wallet balance", e);
                                    listener.onFailure(e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error fetching walletBalance", e);
                    listener.onFailure(e);
                });
    }

    public void fetchUserDetails() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        userLiveData.setValue(currentUser);
                    }
                })
                .addOnFailureListener(e -> Log.e("FireStore", "Error fetching user details", e));
    }

    public void checkUserInFireStore(String userId, FireStoreUserCheckCallback callback) {
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String receiverName = documentSnapshot.getString("fullName");
                        callback.onUserCheckResult(true, receiverName);
                    } else {
                        callback.onUserCheckResult(false, "");
                    }
                })
                .addOnFailureListener(e -> callback.onUserCheckResult(false, ""));
    }

    public LiveData<Boolean> processTransaction(double amount, String receiverUid, String paymentType) {
        MutableLiveData<Boolean> transactionResult = new MutableLiveData<>();
        String currentUserId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        firestore.runTransaction(transaction -> {
            DocumentReference senderRef = firestore.collection("users").document(currentUserId);
            DocumentReference receiverRef = firestore.collection("users").document(receiverUid);

            User sender = transaction.get(senderRef).toObject(User.class);
            User receiver = transaction.get(receiverRef).toObject(User.class);

            if (sender == null || receiver == null) {
                throw new FirebaseFirestoreException("User not found", FirebaseFirestoreException.Code.ABORTED);
            }

            if (sender.getWalletBalance() < amount) {
                throw new FirebaseFirestoreException("Insufficient balance", FirebaseFirestoreException.Code.ABORTED);
            }

            // Deduct amount from sender
            double updatedSenderBalance = sender.getWalletBalance() - amount;

            // Add amount to receiver
            double updatedReceiverBalance = receiver.getWalletBalance() + amount;

            // Generate unique transaction ID
            String transactionId = UUID.randomUUID().toString();
            long timestamp = new Date().getTime();

            // Create transaction history
            Transaction senderTransaction = new Transaction(transactionId, amount, "Sent", "Paid to " + receiver.getFullName(), paymentType, timestamp);
            Transaction receiverTransaction = new Transaction(transactionId, amount, "Received", "Received from " + sender.getFullName(), paymentType, timestamp);

            // Update sender's balance and transactions**
            transaction.update(senderRef, "walletBalance", updatedSenderBalance);
            transaction.update(senderRef, "transactions", FieldValue.arrayUnion(senderTransaction));

            // Update receiver's balance and transactions**
            transaction.update(receiverRef, "walletBalance", updatedReceiverBalance);
            transaction.update(receiverRef, "transactions", FieldValue.arrayUnion(receiverTransaction));

            return null;
        }).addOnSuccessListener(aVoid -> {
            transactionResult.setValue(true);
        }).addOnFailureListener(e -> {
            Log.i("TransactionError", "Transaction failed: " + e.getMessage());
            transactionResult.setValue(false);
        });

        return transactionResult;
    }

    public void listenForWalletUpdates(String userId, WalletUpdateListener walletUpdateListener) {
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e("FireStoreListener", "Error listening to wallet updates", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                User updatedUser = documentSnapshot.toObject(User.class);
                if (updatedUser != null) {
                    userLiveData.setValue(updatedUser);
                    List<Transaction> transactions = updatedUser.getTransactions();
                    if (transactions != null && !transactions.isEmpty()) {
                        Transaction latestTransaction = transactions.get(transactions.size() - 1);
                        if ("Received".equals(latestTransaction.getType()) && walletUpdateListener != null) {
                            walletUpdateListener.onTransactionReceived(latestTransaction.getAmount());
                        }
                    }
                }
            }
        });
    }

    public LiveData<Pair<Double, Double>> getTotalSpentAndIncome(String userId) {
        MutableLiveData<Pair<Double, Double>> totalLiveData = new MutableLiveData<>();
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e("FireStoreError", "Error fetching transactions", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null && user.getTransactions() != null) {
                    List<Transaction> transactions = user.getTransactions();

                    double totalSpent = transactions.stream()
                            .filter(t -> "Sent".equals(t.getType()))
                            .mapToDouble(Transaction::getAmount)
                            .sum();

                    double totalIncome = transactions.stream()
                            .filter(t -> "Received".equals(t.getType()))
                            .mapToDouble(Transaction::getAmount)
                            .sum();

                    totalLiveData.setValue(new Pair<>(totalSpent, totalIncome));
                }
            }
        });

        return totalLiveData;
    }

    public void removeWallet(String userId, OnCompleteListener<Void> onCompleteListener) {
        final CollectionReference usersCollection = firestore.collection("users");
        usersCollection.document(userId)
                .update("wallet", null)
                .addOnCompleteListener(onCompleteListener);
    }

    public interface OnWalletUpdateListener {
        void onSuccess();

        void onFailure(Exception e);
    }
}
