package com.ajproject.moneymap.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.adapters.GroupExpenseDetailAdapter;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.Group;
import com.ajproject.moneymap.models.GroupExpense;
import com.ajproject.moneymap.utils.AddGroupExpenseDialog;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GroupDetailActivity extends AppCompatActivity implements GroupExpenseDetailAdapter.OnPaymentClickListener {

    private TextView tvGroupName, tvMembers;
    private Button btnAddExpense;
    private RecyclerView rvGroupExpenses;
    private LinearLayout layoutEmptyState;
    private GroupExpenseDetailAdapter adapter;
    private MoneyMapDatabase database;
    private SharedPreferences prefs;
    private Group group;
    private int groupId;
    private List<GroupExpense> expenseList;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        database = MoneyMapDatabase.getInstance(this);
        prefs = getSharedPreferences("MoneyMapPrefs", Context.MODE_PRIVATE);
        currentUserName = prefs.getString("userName", "You");

        groupId = getIntent().getIntExtra("groupId", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvGroupName = findViewById(R.id.tv_group_name);
        tvMembers = findViewById(R.id.tv_members);
        btnAddExpense = findViewById(R.id.btn_add_expense);
        rvGroupExpenses = findViewById(R.id.rv_group_expenses);
        layoutEmptyState = findViewById(R.id.layout_empty_state);

        loadGroupDetails();
        setupRecyclerView();
        loadExpenses();

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void loadGroupDetails() {
        new Thread(() -> {
            group = database.groupDao().getGroupById(groupId);

            runOnUiThread(() -> {
                if (group != null) {
                    tvGroupName.setText(group.getGroupName());
                    tvMembers.setText("Members: " + group.getMembers());
                }
            });
        }).start();
    }

    private void setupRecyclerView() {
        expenseList = new ArrayList<>();
        adapter = new GroupExpenseDetailAdapter(expenseList, this, this);
        rvGroupExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvGroupExpenses.setAdapter(adapter);
    }

    private void loadExpenses() {
        new Thread(() -> {
            List<GroupExpense> expenses = database.groupExpenseDao().getExpensesByGroup(groupId);

            runOnUiThread(() -> {
                expenseList.clear();
                expenseList.addAll(expenses);
                adapter.notifyDataSetChanged();

                if (expenses.isEmpty()) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    rvGroupExpenses.setVisibility(View.GONE);
                } else {
                    layoutEmptyState.setVisibility(View.GONE);
                    rvGroupExpenses.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private void showAddExpenseDialog() {
        if (group == null) return;

        AddGroupExpenseDialog dialog = new AddGroupExpenseDialog(this, group, () -> {
            loadExpenses();
        });
        dialog.show();
    }

    @Override
    public void onPayClick(GroupExpense expense, String memberName, double amountOwed) {
        showPaymentDialog(expense, memberName, amountOwed, false);
    }

    @Override
    public void onMarkPaidClick(GroupExpense expense, String memberName, double amountOwed) {
        showPaymentDialog(expense, memberName, amountOwed, true);
    }

    private void showPaymentDialog(GroupExpense expense, String memberName, double amountOwed, boolean isMarkingPaid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isMarkingPaid ? "Mark Payment from " + memberName : "Pay to " + expense.getPaidBy());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);

        // Get views
        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        Spinner spinnerPaymentMode = dialogView.findViewById(R.id.spinner_payment_mode);

        // Hide unnecessary fields
        dialogView.findViewById(R.id.spinner_category).setVisibility(View.GONE);
        dialogView.findViewById(R.id.rg_type).setVisibility(View.GONE);

        // Setup payment mode spinner
        String[] paymentModes = {"Cash", "UPI", "Card", "Online"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, paymentModes);
        spinnerPaymentMode.setAdapter(adapter);

        // Pre-fill amount
        etAmount.setText(String.valueOf(amountOwed));
        etDescription.setText(isMarkingPaid ?
                memberName + " paid for " + expense.getDescription() :
                "Payment to " + expense.getPaidBy() + " for " + expense.getDescription());

        builder.setView(dialogView);

        builder.setPositiveButton(isMarkingPaid ? "Mark Paid" : "Pay", (dialog, which) -> {
            String amountStr = etAmount.getText().toString().trim();
            String paymentMode = spinnerPaymentMode.getSelectedItem().toString();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double paymentAmount = Double.parseDouble(amountStr);
            if (paymentAmount > amountOwed) {
                Toast.makeText(this, "Amount exceeds owed amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isMarkingPaid) {
                processMarkPaid(expense, memberName, paymentAmount, amountOwed, paymentMode);
            } else {
                processUserPayment(expense, memberName, paymentAmount, amountOwed, paymentMode);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void processUserPayment(GroupExpense expense, String memberName, double paymentAmount,
                                    double totalOwed, String paymentMode) {
        new Thread(() -> {
            try {
                JSONObject payments = new JSONObject(expense.getPayments());
                double currentlyPaid = payments.optDouble(memberName, 0.0);
                double newPaidAmount = currentlyPaid + paymentAmount;
                payments.put(memberName, newPaidAmount);

                expense.setPayments(payments.toString());
                database.groupExpenseDao().update(expense);

                // YOUR payment → Deduct from YOUR balance
                float currentGroupPayments = prefs.getFloat("myGroupPayments", 0f);
                prefs.edit().putFloat("myGroupPayments", currentGroupPayments + (float) paymentAmount).apply();

                runOnUiThread(() -> {
                    double remaining = totalOwed - paymentAmount;
                    String message = remaining <= 0 ?
                            "✓ Full payment complete!\nPaid via " + paymentMode + "\nBalance updated." :
                            "Partial payment: " + formatCurrency(paymentAmount) + " via " + paymentMode +
                                    "\nRemaining: " + formatCurrency(remaining);

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    loadExpenses();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error processing payment", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void processMarkPaid(GroupExpense expense, String memberName, double receivedAmount,
                                 double totalOwed, String paymentMode) {
        new Thread(() -> {
            try {
                JSONObject payments = new JSONObject(expense.getPayments());
                double currentlyPaid = payments.optDouble(memberName, 0.0);
                double newPaidAmount = currentlyPaid + receivedAmount;
                payments.put(memberName, newPaidAmount);

                expense.setPayments(payments.toString());
                database.groupExpenseDao().update(expense);

                // Someone paid YOU back → Reduce your group payments (money back)
                float currentGroupPayments = prefs.getFloat("myGroupPayments", 0f);
                prefs.edit().putFloat("myGroupPayments", currentGroupPayments - (float) receivedAmount).apply();

                runOnUiThread(() -> {
                    double remaining = totalOwed - receivedAmount;
                    String message = remaining <= 0 ?
                            "✓ " + memberName + " fully settled!\nReceived " + formatCurrency(receivedAmount) +
                                    " via " + paymentMode + "\nBalance updated." :
                            memberName + " paid " + formatCurrency(receivedAmount) + " via " + paymentMode +
                                    "\nRemaining: " + formatCurrency(remaining);

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    loadExpenses();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error marking payment", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount);
    }
}