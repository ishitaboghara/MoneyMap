package com.ajproject.moneymap.utils;

import android.app.Dialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.Expense;
import com.google.android.material.textfield.TextInputEditText;

public class AddExpenseDialog {

    private Context context;
    private Dialog dialog;
    private OnExpenseAddedListener listener;

    private TextInputEditText etAmount, etDescription;
    private Spinner spinnerCategory, spinnerPaymentMode;
    private RadioButton rbExpense, rbIncome;
    private Button btnSave, btnCancel;

    public interface OnExpenseAddedListener {
        void onExpenseAdded();
    }

    public AddExpenseDialog(Context context, OnExpenseAddedListener listener) {
        this.context = context;
        this.listener = listener;
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_expense);
        dialog.getWindow().setLayout(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        etAmount = dialog.findViewById(R.id.et_amount);
        etDescription = dialog.findViewById(R.id.et_description);
        spinnerCategory = dialog.findViewById(R.id.spinner_category);
        spinnerPaymentMode = dialog.findViewById(R.id.spinner_payment_mode);
        rbExpense = dialog.findViewById(R.id.rb_expense);
        rbIncome = dialog.findViewById(R.id.rb_income);
        btnSave = dialog.findViewById(R.id.btn_save);
        btnCancel = dialog.findViewById(R.id.btn_cancel);

        setupCategorySpinner();
        setupPaymentModeSpinner();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void setupCategorySpinner() {
        String[] categories = {"Food", "Travel", "Shopping", "Entertainment",
                "Bills", "Health", "Education", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupPaymentModeSpinner() {
        String[] paymentModes = {"Cash", "UPI", "Card", "Online"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, paymentModes);
        spinnerPaymentMode.setAdapter(adapter);
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String paymentMode = spinnerPaymentMode.getSelectedItem().toString();
        String type = rbExpense.isChecked() ? "expense" : "income";

        if (amountStr.isEmpty()) {
            Toast.makeText(context, "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            description = category;
        }

        double amount = Double.parseDouble(amountStr);
        long date = System.currentTimeMillis();

        Expense expense = new Expense(amount, category, paymentMode, description, date, type);

        new Thread(() -> {
            MoneyMapDatabase db = MoneyMapDatabase.getInstance(context);
            db.expenseDao().insert(expense);

            // Dashboard will auto-calculate from database
            // No manual balance updates needed

            if (listener != null) {
                listener.onExpenseAdded();
            }
        }).start();

        String message = type.equals("expense") ?
                "Expense added successfully!" :
                "Income added successfully!";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    public void show() {
        dialog.show();
    }
}