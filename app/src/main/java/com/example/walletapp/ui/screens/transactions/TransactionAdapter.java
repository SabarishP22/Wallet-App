package com.example.walletapp.ui.screens.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletapp.R;
import com.example.walletapp.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactionList = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        int sentColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.red_400);
        int receivedColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.green_600);
        String bankAccountTxt = ContextCompat.getString(holder.itemView.getContext(), R.string.bank_account);

        if (position == 0) {
            holder.lineView.setVisibility(View.GONE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.transactionCardView.getLayoutParams();
            params.topMargin = (int) holder.itemView.getContext().getResources().getDisplayMetrics().density * 12;
            holder.transactionCardView.setLayoutParams(params);
        } else {
            holder.lineView.setVisibility(View.VISIBLE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.transactionCardView.getLayoutParams();
            params.topMargin = 0;
            holder.transactionCardView.setLayoutParams(params);
        }

        String prefix = "Sent".equals(transaction.getType()) ? "- " : "+ ";
        int color = "Sent".equals(transaction.getType()) ? sentColor : receivedColor;

        holder.amountTextView.setText(String.format(Locale.getDefault(), "%s₹%.2f", prefix, transaction.getAmount()));
        holder.amountTextView.setTextColor(color);

        holder.nameTextView.setText(transaction.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.dateTextView.setText(sdf.format(transaction.getTimestamp()));
        if (transaction.getPaymentType().isEmpty()) {
            holder.paymentTypeTv.setText(bankAccountTxt);
        } else {
            holder.paymentTypeTv.setText(transaction.getPaymentType());
        }
        holder.paymentTypeIv.setImageResource(getPaymentIcon(transaction.getPaymentType()));
    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView amountTextView, nameTextView, dateTextView, paymentTypeTv;
        View lineView;
        ImageView paymentTypeIv;
        androidx.cardview.widget.CardView transactionCardView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.amountTv);
            nameTextView = itemView.findViewById(R.id.nameTv);
            dateTextView = itemView.findViewById(R.id.dateTv);
            lineView = itemView.findViewById(R.id.line1);
            transactionCardView = itemView.findViewById(R.id.transactionItemCv);
            paymentTypeTv = itemView.findViewById(R.id.paymentTypeTv);
            paymentTypeIv = itemView.findViewById(R.id.paymentTypeIv);
        }
    }

    private int getPaymentIcon(String paymentType) {
        if (paymentType == null || paymentType.trim().isEmpty()) {
            return R.drawable.bank_account;
        }

        return switch (paymentType) {
            case "Credit Card" -> R.drawable.credit;
            case "Debit Card" -> R.drawable.debit;
            case "Money Cash" -> R.drawable.money;
            default -> R.drawable.bank_account;
        };
    }

}
