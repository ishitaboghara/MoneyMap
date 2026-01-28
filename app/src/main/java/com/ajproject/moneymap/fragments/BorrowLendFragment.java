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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.adapters.BorrowLendAdapter;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.BorrowLend;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BorrowLendFragment extends Fragment implements BorrowLendAdapter.OnItemClickListener {

    private RecyclerView rvBorrowLend;
    private LinearLayout layoutEmptyState;
    private TabLayout tabLayout;
    private BorrowLendAdapter adapter;
    private MoneyMapDatabase database;
    private SharedPreferences prefs;
    private List<BorrowLend> itemList;
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_borrow_lend, container, false);

        database = MoneyMapDatabase.getInstance(getContext());
        prefs = getContext().getSharedPreferences("MoneyMapPrefs", Context.MODE_PRIVATE);

        rvBorrowLend = view.findViewById(R.id.rv_borrow_lend);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        tabLayout = view.findViewById(R.id.tab_layout);

        setupTabs();
        setupRecyclerView();
        loadData();

        return view;
    }

    private void setupTabs() {
        // FIXED: Clear, descriptive tab labels
        tabLayout.addTab(tabLayout.newTab().setText("All Loans"));
        tabLayout.addTab(tabLayout.newTab().setText("I Borrowed"));
        tabLayout.addTab(tabLayout.newTab().setText("I Lent"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    currentFilter = "all";
                } else if (tab.getPosition() == 1) {
                    currentFilter = "borrowed";
                } else {
                    currentFilter = "lent";
                }
                loadData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        itemList = new ArrayList<>();
        adapter = new BorrowLendAdapter(itemList, this);
        rvBorrowLend.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBorrowLend.setAdapter(adapter);
    }

    public void loadData() {
        new Thread(() -> {
            List<BorrowLend> items;

            if (currentFilter.equals("all")) {
                items = database.borrowLendDao().getAllUnsettled();
            } else {
                items = database.borrowLendDao().getUnsettledByType(currentFilter);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    itemList.clear();
                    itemList.addAll(items);
                    adapter.notifyDataSetChanged();

                    if (items.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        rvBorrowLend.setVisibility(View.GONE);
                    } else {
                        layoutEmptyState.setVisibility(View.GONE);
                        rvBorrowLend.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onSettleClick(BorrowLend item) {
        String title = item.getType().equals("borrowed") ?
                "Pay Back to " + item.getPersonName() :
                "Receive from " + item.getPersonName();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText input = new EditText(getContext());
        input.setHint("Enter amount to settle (Max: " + item.getRemainingAmount() + ")");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Settle", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount > item.getRemainingAmount()) {
                Toast.makeText(getContext(), "Amount exceeds remaining balance", Toast.LENGTH_SHORT).show();
                return;
            }

            settlePayment(item, amount);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void settlePayment(BorrowLend item, double amount) {
        new Thread(() -> {
            double newRemaining = item.getRemainingAmount() - amount;
            item.setRemainingAmount(newRemaining);

            if (newRemaining <= 0) {
                item.setSettled(true);
            }

            database.borrowLendDao().update(item);

            // Track settlements for dashboard
            if (item.getType().equals("borrowed")) {
                // You're paying back borrowed money = money going out
                float currentPaidBack = prefs.getFloat("paidBackBorrowed", 0f);
                prefs.edit().putFloat("paidBackBorrowed", currentPaidBack + (float) amount).apply();
            } else {
                // You're receiving money back that you lent = money coming in
                float currentReceived = prefs.getFloat("receivedFromLending", 0f);
                prefs.edit().putFloat("receivedFromLending", currentReceived + (float) amount).apply();
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String message = item.getType().equals("borrowed") ?
                            "Payment sent! Balance updated." :
                            "Payment received! Balance updated.";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    loadData();
                });
            }
        }).start();
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}