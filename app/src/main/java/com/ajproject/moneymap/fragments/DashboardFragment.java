package com.ajproject.moneymap.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.database.MoneyMapDatabase;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvCurrentBalance, tvTotalIncome, tvTotalSpent;
    private TextView tvYouOwe, tvOwedToYou, tvWelcome;
    private MoneyMapDatabase database;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        database = MoneyMapDatabase.getInstance(getContext());
        prefs = getContext().getSharedPreferences("MoneyMapPrefs", Context.MODE_PRIVATE);

        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvCurrentBalance = view.findViewById(R.id.tv_current_balance);
        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        tvTotalSpent = view.findViewById(R.id.tv_total_spent);
        tvYouOwe = view.findViewById(R.id.tv_you_owe);
        tvOwedToYou = view.findViewById(R.id.tv_owed_to_you);

        // Set welcome message
        String userName = prefs.getString("userName", "User");
        tvWelcome.setText("Welcome, " + userName + "!");

        // Click to add income/salary
        tvCurrentBalance.setOnClickListener(v -> showAddIncomeDialog());

        loadDashboardData();

        return view;
    }

    public void loadDashboardData() {
        new Thread(() -> {
            // Calculate all finances
            double totalIncome = calculateTotalIncome();
            double totalSpent = calculateTotalSpent();
            double currentBalance = totalIncome - totalSpent;

            double youOwe = database.borrowLendDao().getTotalBorrowed();
            double owedToYou = database.borrowLendDao().getTotalLent();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvTotalIncome.setText(formatCurrency(totalIncome));
                    tvTotalSpent.setText(formatCurrency(totalSpent));
                    tvCurrentBalance.setText(formatCurrency(currentBalance));
                    tvYouOwe.setText(formatCurrency(youOwe));
                    tvOwedToYou.setText(formatCurrency(owedToYou));
                });
            }
        }).start();
    }

    private double calculateTotalIncome() {
        // Get base salary/income from SharedPreferences
        double baseSalary = prefs.getFloat("baseSalary", 0f);

        // Get income from database (income type expenses)
        double dbIncome = database.expenseDao().getTotalIncome();

        // Get money received back from lending
        double receivedBack = prefs.getFloat("receivedFromLending", 0f);

        // Get money borrowed (adds to available money)
        double borrowed = prefs.getFloat("totalBorrowed", 0f);

        return baseSalary + dbIncome + receivedBack + borrowed;
    }

    private double calculateTotalSpent() {
        // Regular expenses
        double expenses = database.expenseDao().getTotalExpenses();

        // Money lent to others
        double lent = prefs.getFloat("totalLent", 0f);

        // Money paid back (when you borrowed)
        double paidBack = prefs.getFloat("paidBackBorrowed", 0f);

        // YOUR group payments only
        double groupPayments = prefs.getFloat("myGroupPayments", 0f);

        return expenses + lent + paidBack + groupPayments;
    }

    private void showAddIncomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Income/Salary");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText inputAmount = new EditText(getContext());
        inputAmount.setHint("Enter amount (e.g., 50000)");
        inputAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputAmount);

        TextView tvNote = new TextView(getContext());
        tvNote.setText("\nThis will be added to your total income");
        tvNote.setTextSize(12);
        layout.addView(tvNote);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String amountStr = inputAmount.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    addIncome(amount);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void addIncome(double amount) {
        double currentSalary = prefs.getFloat("baseSalary", 0f);
        double newSalary = currentSalary + amount;

        prefs.edit().putFloat("baseSalary", (float) newSalary).apply();

        Toast.makeText(getContext(), "Added " + formatCurrency(amount) + " to income!",
                Toast.LENGTH_SHORT).show();
        loadDashboardData();
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }
}