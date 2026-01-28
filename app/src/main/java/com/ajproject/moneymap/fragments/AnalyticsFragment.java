package com.ajproject.moneymap.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.Expense;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private PieChart pieChart;
    private LinearLayout layoutCategoryBreakdown;
    private TextView tvNoData;
    private MoneyMapDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        database = MoneyMapDatabase.getInstance(getContext());

        pieChart = view.findViewById(R.id.pie_chart);
        layoutCategoryBreakdown = view.findViewById(R.id.layout_category_breakdown);
        tvNoData = view.findViewById(R.id.tv_no_data);

        loadAnalytics();

        return view;
    }

    private void loadAnalytics() {
        new Thread(() -> {
            List<Expense> expenses = database.expenseDao().getExpensesByType("expense");

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (expenses.isEmpty()) {
                        tvNoData.setVisibility(View.VISIBLE);
                        pieChart.setVisibility(View.GONE);
                    } else {
                        tvNoData.setVisibility(View.GONE);
                        pieChart.setVisibility(View.VISIBLE);
                        setupPieChart(expenses);
                        setupCategoryBreakdown(expenses);
                    }
                });
            }
        }).start();
    }

    private void setupPieChart(List<Expense> expenses) {
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense expense : expenses) {
            String category = expense.getCategory();
            double amount = categoryTotals.getOrDefault(category, 0.0);
            categoryTotals.put(category, amount + expense.getAmount());
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            colors.add(getCategoryColor(entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);

        // GOOGLE-STYLE: Percentages outside with clean lines
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(13f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);

        // Position OUTSIDE slices
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        // Clean white lines connecting to percentages
        dataSet.setValueLineColor(Color.WHITE);
        dataSet.setValueLineWidth(1.2f);
        dataSet.setValueLinePart1OffsetPercentage(95f);
        dataSet.setValueLinePart1Length(0.3f);
        dataSet.setValueLinePart2Length(0.5f);

        // Slice spacing for clean look
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // GOOGLE-STYLE CHART CONFIGURATION
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);

        // Large center hole like Google charts
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(62f);
        pieChart.setTransparentCircleRadius(65f);

        // Center text (optional)
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Expenses by\nCategory");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.WHITE);

        // No category labels on chart
        pieChart.setDrawEntryLabels(false);

        // Rotation & interaction
        pieChart.setRotationEnabled(true);
        pieChart.setRotationAngle(0);
        pieChart.setHighlightPerTapEnabled(true);

        // Legend (like Google's bottom legend)
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        pieChart.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        pieChart.getLegend().setDrawInside(false);
        pieChart.getLegend().setTextColor(Color.WHITE);
        pieChart.getLegend().setTextSize(10f);
        pieChart.getLegend().setFormSize(8f);
        pieChart.getLegend().setFormToTextSpace(4f);
        pieChart.getLegend().setXEntrySpace(8f);
        pieChart.getLegend().setYEntrySpace(4f);
        pieChart.getLegend().setWordWrapEnabled(true);

        // Extra space for outside labels
        pieChart.setExtraOffsets(15, 10, 15, 10);

        // Smooth animation
        pieChart.animateY(1200);
        pieChart.invalidate();
    }

    private void setupCategoryBreakdown(List<Expense> expenses) {
        layoutCategoryBreakdown.removeAllViews();

        Map<String, Double> categoryTotals = new HashMap<>();
        double totalExpenses = 0;

        for (Expense expense : expenses) {
            String category = expense.getCategory();
            double amount = categoryTotals.getOrDefault(category, 0.0);
            categoryTotals.put(category, amount + expense.getAmount());
            totalExpenses += expense.getAmount();
        }

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            View row = createCategoryRow(entry.getKey(), entry.getValue(), totalExpenses);
            layoutCategoryBreakdown.addView(row);
        }
    }

    private View createCategoryRow(String category, double amount, double total) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 12, 0, 12);

        TextView tvCategory = new TextView(getContext());
        tvCategory.setText(category);
        tvCategory.setTextSize(16);
        tvCategory.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvAmount = new TextView(getContext());
        tvAmount.setText(formatCurrency(amount));
        tvAmount.setTextSize(16);
        tvAmount.setTextColor(getCategoryColor(category));
        tvAmount.setTypeface(null, Typeface.BOLD);

        TextView tvPercentage = new TextView(getContext());
        double percentage = (amount / total) * 100;
        tvPercentage.setText(String.format(" (%.1f%%)", percentage));
        tvPercentage.setTextSize(14);

        row.addView(tvCategory);
        row.addView(tvAmount);
        row.addView(tvPercentage);

        return row;
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount);
    }

    private int getCategoryColor(String category) {
        switch (category.toLowerCase()) {
            case "food": return Color.parseColor("#EF5350");
            case "travel": return Color.parseColor("#42A5F5");
            case "shopping": return Color.parseColor("#EC407A");
            case "entertainment": return Color.parseColor("#AB47BC");
            case "bills": return Color.parseColor("#FFA726");
            case "health": return Color.parseColor("#66BB6A");
            case "education": return Color.parseColor("#26C6DA");
            default: return Color.parseColor("#78909C");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAnalytics();
    }
}