package com.ajproject.moneymap.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.models.Group;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private List<Group> groups;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
        void onViewExpensesClick(Group group);
    }

    public GroupAdapter(List<Group> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groups.get(position);

        holder.tvGroupName.setText(group.getGroupName());
        holder.tvMembers.setText("Members: " + group.getMembers());
        holder.tvDate.setText("Created: " + formatDate(group.getCreatedDate()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onGroupClick(group);
        });

        holder.btnViewExpenses.setOnClickListener(v -> {
            if (listener != null) listener.onViewExpensesClick(group);
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void updateGroups(List<Group> newGroups) {
        this.groups = newGroups;
        notifyDataSetChanged();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvMembers, tvDate;
        Button btnViewExpenses;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvMembers = itemView.findViewById(R.id.tv_members);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnViewExpenses = itemView.findViewById(R.id.btn_view_expenses);
        }
    }
}