package com.ajproject.moneymap.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.models.GroupExpense;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupExpenseDetailAdapter extends RecyclerView.Adapter<GroupExpenseDetailAdapter.ViewHolder> {

    private List<GroupExpense> expenses;
    private OnPaymentClickListener listener;
    private String currentUserName;

    public interface OnPaymentClickListener {
        void onPayClick(GroupExpense expense, String memberName, double amountOwed);
        void onMarkPaidClick(GroupExpense expense, String memberName, double amountOwed);
    }

    public GroupExpenseDetailAdapter(List<GroupExpense> expenses, OnPaymentClickListener listener, Context context) {
        this.expenses = expenses;
        this.listener = listener;
        SharedPreferences prefs = context.getSharedPreferences("MoneyMapPrefs", Context.MODE_PRIVATE);
        this.currentUserName = prefs.getString("userName", "You");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_expense_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupExpense expense = expenses.get(position);

        holder.tvDescription.setText(expense.getDescription());
        holder.tvPaidBy.setText("Paid by: " + expense.getPaidBy());
        holder.tvDate.setText(formatDate(expense.getDate()));
        holder.tvTotalAmount.setText(formatCurrency(expense.getTotalAmount()));

        // Clear previous members
        holder.layoutMembersOwing.removeAllViews();

        // Calculate split per person
        String[] members = expense.getSplitAmong().split(", ");
        double amountPerPerson = expense.getTotalAmount() / members.length;

        // Check if current user paid
        boolean currentUserPaid = expense.getPaidBy().equalsIgnoreCase(currentUserName);

        try {
            JSONObject payments = new JSONObject(expense.getPayments());

            for (String member : members) {
                if (!member.equals(expense.getPaidBy())) {
                    double amountPaid = payments.optDouble(member, 0.0);
                    double remaining = amountPerPerson - amountPaid;

                    View memberView = createMemberOwingView(
                            holder.itemView,
                            member,
                            amountPerPerson,
                            amountPaid,
                            remaining,
                            expense,
                            currentUserPaid
                    );
                    holder.layoutMembersOwing.addView(memberView);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View createMemberOwingView(View parent, String memberName, double totalOwed,
                                       double paid, double remaining, GroupExpense expense,
                                       boolean currentUserPaid) {
        LinearLayout layout = new LinearLayout(parent.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 8, 0, 8);

        // Member info row
        LinearLayout infoRow = new LinearLayout(parent.getContext());
        infoRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvMemberName = new TextView(parent.getContext());
        tvMemberName.setText(memberName);
        tvMemberName.setTextSize(14);
        tvMemberName.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvOwes = new TextView(parent.getContext());
        tvOwes.setText("Owes: " + formatCurrency(totalOwed));
        tvOwes.setTextSize(14);

        infoRow.addView(tvMemberName);
        infoRow.addView(tvOwes);

        // Payment status row
        TextView tvStatus = new TextView(parent.getContext());
        if (remaining <= 0) {
            tvStatus.setText("✓ Paid " + formatCurrency(paid));
            tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if (paid > 0) {
            tvStatus.setText("Paid " + formatCurrency(paid) + " | Remaining: " + formatCurrency(remaining));
            tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
        } else {
            tvStatus.setText("Not paid yet | Owes: " + formatCurrency(remaining));
            tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
        }
        tvStatus.setTextSize(12);
        tvStatus.setPadding(0, 4, 0, 4);

        layout.addView(infoRow);
        layout.addView(tvStatus);

        // Button logic
        if (remaining > 0) {
            boolean isCurrentUserOwing = memberName.equalsIgnoreCase(currentUserName);

            if (isCurrentUserOwing) {
                // Current user owes → Show "Pay Now" button
                Button btnPay = new Button(parent.getContext());
                btnPay.setText("Pay Now");
                btnPay.setTextSize(12);
                btnPay.setPadding(16, 8, 16, 8);
                btnPay.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onPayClick(expense, memberName, remaining);
                    }
                });
                layout.addView(btnPay);

            } else if (currentUserPaid) {
                // Current user paid for group → Show "Mark as Paid" button
                Button btnMarkPaid = new Button(parent.getContext());
                btnMarkPaid.setText("Mark " + memberName + " Paid");
                btnMarkPaid.setTextSize(12);
                btnMarkPaid.setPadding(16, 8, 16, 8);
                btnMarkPaid.setBackgroundColor(Color.parseColor("#00BFA5"));
                btnMarkPaid.setTextColor(Color.WHITE);
                btnMarkPaid.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onMarkPaidClick(expense, memberName, remaining);
                    }
                });
                layout.addView(btnMarkPaid);
            }
        }

        // Add divider
        View divider = new View(parent.getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) divider.getLayoutParams();
        params.setMargins(0, 8, 0, 0);
        divider.setLayoutParams(params);
        layout.addView(divider);

        return layout;
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateExpenses(List<GroupExpense> newExpenses) {
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvPaidBy, tvDate, tvTotalAmount;
        LinearLayout layoutMembersOwing;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvPaidBy = itemView.findViewById(R.id.tv_paid_by);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            layoutMembersOwing = itemView.findViewById(R.id.layout_members_owing);
        }
    }
}