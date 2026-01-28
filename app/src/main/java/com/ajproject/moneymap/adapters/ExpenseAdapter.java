package com.ajproject.moneymap.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.models.Expense;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
        void onExpenseLongClick(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenses, OnExpenseClickListener listener) {
        this.expenses = expenses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        holder.tvCategory.setText(expense.getCategory());
        holder.tvPaymentMode.setText(expense.getPaymentMode());
        holder.tvDescription.setText(expense.getDescription());
        holder.tvDate.setText(formatDate(expense.getDate()));

        // Format amount
        String amountText = formatCurrency(expense.getAmount());
        holder.tvAmount.setText(amountText);

        // Set color based on type
        if (expense.getType().equals("expense")) {
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Red
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Green
        }

        // Set category color indicator
        holder.viewCategoryIndicator.setBackgroundColor(getCategoryColor(expense.getCategory()));

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onExpenseClick(expense);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onExpenseLongClick(expense);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateExpenses(List<Expense> newExpenses) {
        this.expenses = newExpenses;
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

    private int getCategoryColor(String category) {
        switch (category.toLowerCase()) {
            case "food": return Color.parseColor("#FF5722");
            case "travel": return Color.parseColor("#2196F3");
            case "shopping": return Color.parseColor("#E91E63");
            case "entertainment": return Color.parseColor("#9C27B0");
            case "bills": return Color.parseColor("#FF9800");
            case "health": return Color.parseColor("#4CAF50");
            case "education": return Color.parseColor("#00BCD4");
            default: return Color.parseColor("#607D8B");
        }
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvPaymentMode, tvDescription, tvDate, tvAmount;
        View viewCategoryIndicator;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPaymentMode = itemView.findViewById(R.id.tv_payment_mode);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            viewCategoryIndicator = itemView.findViewById(R.id.view_category_indicator);
        }
    }
}