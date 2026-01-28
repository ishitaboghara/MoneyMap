package com.ajproject.moneymap.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.Group;
import com.ajproject.moneymap.models.GroupExpense;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

public class AddGroupExpenseDialog {

    private Context context;
    private Dialog dialog;
    private Group group;
    private OnExpenseAddedListener listener;

    private TextInputEditText etDescription, etTotalAmount;
    private Spinner spinnerPaidBy;
    private TextView tvSplitCalculation, tvSplitDetails;
    private Button btnSave, btnCancel;
    private String[] memberArray;
    private SharedPreferences prefs;
    private String currentUserName;

    public interface OnExpenseAddedListener {
        void onExpenseAdded();
    }

    public AddGroupExpenseDialog(Context context, Group group, OnExpenseAddedListener listener) {
        this.context = context;
        this.group = group;
        this.listener = listener;
        this.prefs = context.getSharedPreferences("MoneyMapPrefs", Context.MODE_PRIVATE);
        this.currentUserName = prefs.getString("userName", "You");
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_group_expense);
        dialog.getWindow().setLayout(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        etDescription = dialog.findViewById(R.id.et_description);
        etTotalAmount = dialog.findViewById(R.id.et_total_amount);
        spinnerPaidBy = dialog.findViewById(R.id.spinner_paid_by);
        tvSplitCalculation = dialog.findViewById(R.id.tv_split_calculation);
        tvSplitDetails = dialog.findViewById(R.id.tv_split_details);
        btnSave = dialog.findViewById(R.id.btn_save);
        btnCancel = dialog.findViewById(R.id.btn_cancel);

        setupSpinner();
        setupAmountListener();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void setupSpinner() {
        memberArray = group.getMembers().split(", ");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, memberArray);
        spinnerPaidBy.setAdapter(adapter);

        // Try to select current user as default payer
        for (int i = 0; i < memberArray.length; i++) {
            if (memberArray[i].equalsIgnoreCase(currentUserName)) {
                spinnerPaidBy.setSelection(i);
                break;
            }
        }
    }

    private void setupAmountListener() {
        etTotalAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSplitCalculation();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateSplitCalculation() {
        String amountStr = etTotalAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            tvSplitCalculation.setText("Each person owes: ₹0");
            tvSplitDetails.setText("Enter amount to see split details");
            return;
        }

        try {
            double totalAmount = Double.parseDouble(amountStr);
            int memberCount = memberArray.length;
            double amountPerPerson = totalAmount / memberCount;

            tvSplitCalculation.setText(String.format("Each person owes: %s",
                    formatCurrency(amountPerPerson)));

            String paidBy = spinnerPaidBy.getSelectedItem() != null ?
                    spinnerPaidBy.getSelectedItem().toString() : memberArray[0];

            StringBuilder details = new StringBuilder();
            for (String member : memberArray) {
                if (!member.equals(paidBy)) {
                    details.append("• ").append(member)
                            .append(" owes ").append(paidBy)
                            .append(": ").append(formatCurrency(amountPerPerson))
                            .append("\n");
                }
            }

            if (details.length() == 0) {
                details.append(paidBy).append(" paid for everyone equally!");
            }

            tvSplitDetails.setText(details.toString().trim());

        } catch (NumberFormatException e) {
            tvSplitCalculation.setText("Invalid amount");
            tvSplitDetails.setText("Please enter a valid number");
        }
    }

    private void saveExpense() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etTotalAmount.getText().toString().trim();

        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalAmount = Double.parseDouble(amountStr);
        String paidBy = spinnerPaidBy.getSelectedItem().toString();
        String splitAmong = group.getMembers();

        int memberCount = memberArray.length;
        double amountPerPerson = totalAmount / memberCount;

        JSONObject payments = new JSONObject();
        try {
            for (String member : memberArray) {
                if (!member.equals(paidBy)) {
                    payments.put(member, 0.0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        long date = System.currentTimeMillis();

        GroupExpense groupExpense = new GroupExpense(
                group.getId(),
                description,
                totalAmount,
                paidBy,
                splitAmong,
                payments.toString(),
                date
        );

        new Thread(() -> {
            MoneyMapDatabase db = MoneyMapDatabase.getInstance(context);
            db.groupExpenseDao().insert(groupExpense);

            // CRITICAL: If current user paid, deduct from balance
            if (paidBy.equalsIgnoreCase(currentUserName)) {
                float currentGroupPayments = prefs.getFloat("myGroupPayments", 0f);
                prefs.edit().putFloat("myGroupPayments", currentGroupPayments + (float) totalAmount).apply();
            }

            if (listener != null) {
                listener.onExpenseAdded();
            }
        }).start();

        StringBuilder message = new StringBuilder();
        if (paidBy.equalsIgnoreCase(currentUserName)) {
            message.append("You paid ").append(formatCurrency(totalAmount))
                    .append("!\nBalance updated.\n\n");
        } else {
            message.append("Expense added!\n").append(paidBy).append(" paid.\n\n");
        }

        message.append("Split: ").append(formatCurrency(amountPerPerson))
                .append(" per person");

        Toast.makeText(context, message.toString(), Toast.LENGTH_LONG).show();
        dialog.dismiss();
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount);
    }

    public void show() {
        dialog.show();
    }
}