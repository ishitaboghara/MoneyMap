package com.ajproject.moneymap;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ajproject.moneymap.activities.LoginActivity;
import com.ajproject.moneymap.fragments.AnalyticsFragment;
import com.ajproject.moneymap.fragments.BorrowLendFragment;
import com.ajproject.moneymap.fragments.DashboardFragment;
import com.ajproject.moneymap.fragments.ExpensesFragment;
import com.ajproject.moneymap.fragments.GroupsFragment;
import com.ajproject.moneymap.utils.AddBorrowLendDialog;
import com.ajproject.moneymap.utils.AddExpenseDialog;
import com.ajproject.moneymap.utils.CreateGroupDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAdd;
    private TextView toolbarTitle;
    private ImageButton btnLogout;
    private Fragment currentFragment;
    private String currentScreen = "dashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        btnLogout = findViewById(R.id.btn_logout);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAdd = findViewById(R.id.fab_add);

        setupBottomNavigation();
        setupFab();
        setupLogout();

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            toolbarTitle.setText(R.string.nav_dashboard);
        }
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences("MoneyMapPrefs", MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    loadFragment(new DashboardFragment());
                    toolbarTitle.setText(R.string.nav_dashboard);
                    currentScreen = "dashboard";
                    return true;
                } else if (itemId == R.id.nav_expenses) {
                    loadFragment(new ExpensesFragment());
                    toolbarTitle.setText(R.string.nav_expenses);
                    currentScreen = "expenses";
                    return true;
                } else if (itemId == R.id.nav_borrow_lend) {
                    loadFragment(new BorrowLendFragment());
                    toolbarTitle.setText(R.string.nav_borrow_lend);
                    currentScreen = "borrow_lend";
                    return true;
                } else if (itemId == R.id.nav_groups) {
                    loadFragment(new GroupsFragment());
                    toolbarTitle.setText(R.string.nav_groups);
                    currentScreen = "groups";
                    return true;
                } else if (itemId == R.id.nav_analytics) {
                    loadFragment(new AnalyticsFragment());
                    toolbarTitle.setText(R.string.nav_analytics);
                    currentScreen = "analytics";
                    return true;
                }

                return false;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        String[] options = {"Add Expense", "Add Borrow/Lend", "Create Group"};

        new AlertDialog.Builder(this)
                .setTitle("Add New")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddExpenseDialog();
                    } else if (which == 1) {
                        showAddBorrowLendDialog();
                    } else {
                        showCreateGroupDialog();
                    }
                })
                .show();
    }

    private void showAddExpenseDialog() {
        AddExpenseDialog dialog = new AddExpenseDialog(this, new AddExpenseDialog.OnExpenseAddedListener() {
            @Override
            public void onExpenseAdded() {
                refreshCurrentFragment();
            }
        });
        dialog.show();
    }

    private void showAddBorrowLendDialog() {
        AddBorrowLendDialog dialog = new AddBorrowLendDialog(this, new AddBorrowLendDialog.OnDataAddedListener() {
            @Override
            public void onDataAdded() {
                refreshCurrentFragment();
            }
        });
        dialog.show();
    }

    private void showCreateGroupDialog() {
        CreateGroupDialog dialog = new CreateGroupDialog(this, new CreateGroupDialog.OnGroupCreatedListener() {
            @Override
            public void onGroupCreated() {
                refreshCurrentFragment();
            }
        });
        dialog.show();
    }

    private void refreshCurrentFragment() {
        if (currentFragment instanceof DashboardFragment) {
            ((DashboardFragment) currentFragment).loadDashboardData();
        } else if (currentFragment instanceof ExpensesFragment) {
            ((ExpensesFragment) currentFragment).loadExpenses();
        } else if (currentFragment instanceof BorrowLendFragment) {
            ((BorrowLendFragment) currentFragment).loadData();
        } else if (currentFragment instanceof GroupsFragment) {
            ((GroupsFragment) currentFragment).loadGroups();
        }
    }

    private void loadFragment(Fragment fragment) {
        currentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}