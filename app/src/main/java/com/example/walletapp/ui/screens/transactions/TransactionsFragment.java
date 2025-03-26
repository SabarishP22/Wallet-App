package com.example.walletapp.ui.screens.transactions;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.walletapp.databinding.FragmentTransactionsBinding;
import com.example.walletapp.models.Transaction;
import com.example.walletapp.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionsFragment extends BaseFragment {

    private FragmentTransactionsBinding transactionsBinding;
    private TransactionViewModel transactionViewModel;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        transactionsBinding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return transactionsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        init();
    }

    private void init() {
        setupRecyclerView();
        observeTransactions();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(transactionsList);
        transactionsBinding.transactionsRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        transactionsBinding.transactionsRv.setAdapter(transactionAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeTransactions() {
        transactionViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getTransactions() != null) {
                if (user.getTransactions().isEmpty()) {
                    transactionsBinding.transactionsRv.setVisibility(View.GONE);
                    transactionsBinding.noTransactionsTv.setVisibility(View.VISIBLE);
                    return;
                }
                transactionsBinding.transactionsRv.setVisibility(View.VISIBLE);
                transactionsBinding.noTransactionsTv.setVisibility(View.GONE);
                transactionsList.clear();
                transactionsList.addAll(user.getTransactions());
                transactionAdapter.notifyDataSetChanged();
            }
        });
    }
}
