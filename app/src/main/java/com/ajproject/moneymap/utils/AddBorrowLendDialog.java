package com.ajproject.moneymap.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.BorrowLend;
import com.google.android.material.textfield.TextInputEditText;

public class AddBorrowLendDialog {

    private Context context;
    private Dialog dialog;
    private OnDataAddedListener listener;

    private TextInputEditText etPersonName, etAmount, etDescription;
    private RadioButton rbBorrowed, rbLent;
    private Button btnSave, btnCancel;
    private SharedPreferences prefs;

    public interface OnDataAddedListener {
        void onDataAdded();
    }

    public AddBorrowLendDialog(Context context, OnDataAddedListener listener) {
        this.context = context;
        this.listener = listener;
        this.prefs = context.getSharedPreferences("MoneyMapPrefs", Context.MODE_PRIVATE);
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_borrow_lend);
        dialog.getWindow().setLayout(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        etPersonName = dialog.findViewById(R.id.et_person_name);
        etAmount = dialog.findViewById(R.id.et_amount);
        etDescription = dialog.findViewById(R.id.et_description);
        rbBorrowed = dialog.findViewById(R.id.rb_borrowed);
        rbLent = dialog.findViewById(R.id.rb_lent);
        btnSave = dialog.findViewById(R.id.btn_save);
        btnCancel = dialog.findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> saveData());
    }

    private void saveData() {
        String personName = etPersonName.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String type = rbBorrowed.isChecked() ? "borrowed" : "lent";

        if (personName.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        long date = System.currentTimeMillis();

        BorrowLend borrowLend = new BorrowLend(personName, amount, amount, type,
                description, date, false);

        new Thread(() -> {
            MoneyMapDatabase db = MoneyMapDatabase.getInstance(context);
            db.borrowLendDao().insert(borrowLend);

            // Track for dashboard calculations
            if (type.equals("borrowed")) {
                // You borrowed = money came in
                float currentBorrowed = prefs.getFloat("totalBorrowed", 0f);
                prefs.edit().putFloat("totalBorrowed", currentBorrowed + (float) amount).apply();
            } else {
                // You lent = money went out
                float currentLent = prefs.getFloat("totalLent", 0f);
                prefs.edit().putFloat("totalLent", currentLent + (float) amount).apply();
            }

            if (listener != null) {
                listener.onDataAdded();
            }
        }).start();

        String message = type.equals("borrowed") ?
                "Borrowed entry added!" :
                "Lent entry added!";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    public void show() {
        dialog.show();
    }
}