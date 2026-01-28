package com.ajproject.moneymap.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.adapters.ExpenseAdapter;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.Expense;

import java.util.ArrayList;
import java.util.List;

public class ExpensesFragment extends Fragment implements ExpenseAdapter.OnExpenseClickListener {

    private RecyclerView rvExpenses;
    private LinearLayout layoutEmptyState;
    private ExpenseAdapter adapter;
    private MoneyMapDatabase database;
    private List<Expense> expenseList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        database = MoneyMapDatabase.getInstance(getContext());

        rvExpenses = view.findViewById(R.id.rv_expenses);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        setupRecyclerView();
        loadExpenses();

        return view;
    }

    private void setupRecyclerView() {
        expenseList = new ArrayList<>();
        adapter = new ExpenseAdapter(expenseList, this);
        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExpenses.setAdapter(adapter);
    }

    public void loadExpenses() {
        new Thread(() -> {
            List<Expense> expenses = database.expenseDao().getAllExpenses();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    expenseList.clear();
                    expenseList.addAll(expenses);
                    adapter.notifyDataSetChanged();

                    if (expenses.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        rvExpenses.setVisibility(View.GONE);
                    } else {
                        layoutEmptyState.setVisibility(View.GONE);
                        rvExpenses.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onExpenseClick(Expense expense) {
        // TODO: Edit expense
        Toast.makeText(getContext(), "Edit - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onExpenseLongClick(Expense expense) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> deleteExpense(expense))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteExpense(Expense expense) {
        new Thread(() -> {
            database.expenseDao().delete(expense);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Expense deleted", Toast.LENGTH_SHORT).show();
                    loadExpenses();
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpenses();
    }
}