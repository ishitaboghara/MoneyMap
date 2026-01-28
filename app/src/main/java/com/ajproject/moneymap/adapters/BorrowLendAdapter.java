package com.ajproject.moneymap.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.models.BorrowLend;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BorrowLendAdapter extends RecyclerView.Adapter<BorrowLendAdapter.ViewHolder> {

    private List<BorrowLend> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onSettleClick(BorrowLend item);
    }

    public BorrowLendAdapter(List<BorrowLend> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_borrow_lend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BorrowLend item = items.get(position);

        holder.tvPersonName.setText(item.getPersonName());
        holder.tvDescription.setText(item.getDescription());
        holder.tvDate.setText(formatDate(item.getDate()));
        holder.tvAmount.setText(formatCurrency(item.getAmount()));
        holder.tvRemaining.setText("Remaining: " + formatCurrency(item.getRemainingAmount()));

        if (item.getType().equals("borrowed")) {
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Red
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Green
        }

        if (item.isSettled() || item.getRemainingAmount() <= 0) {
            holder.btnSettle.setVisibility(View.GONE);
        } else {
            holder.btnSettle.setVisibility(View.VISIBLE);
            holder.btnSettle.setOnClickListener(v -> {
                if (listener != null) listener.onSettleClick(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<BorrowLend> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPersonName, tvDescription, tvDate, tvAmount, tvRemaining;
        Button btnSettle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPersonName = itemView.findViewById(R.id.tv_person_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvRemaining = itemView.findViewById(R.id.tv_remaining);
            btnSettle = itemView.findViewById(R.id.btn_settle);
        }
    }
}