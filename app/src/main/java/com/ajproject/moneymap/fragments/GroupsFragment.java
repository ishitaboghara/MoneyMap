package com.ajproject.moneymap.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.activities.GroupDetailActivity;
import com.ajproject.moneymap.adapters.GroupAdapter;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment implements GroupAdapter.OnGroupClickListener {

    private RecyclerView rvGroups;
    private LinearLayout layoutEmptyState;
    private GroupAdapter adapter;
    private MoneyMapDatabase database;
    private List<Group> groupList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        database = MoneyMapDatabase.getInstance(getContext());

        rvGroups = view.findViewById(R.id.rv_groups);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        setupRecyclerView();
        loadGroups();

        return view;
    }

    private void setupRecyclerView() {
        groupList = new ArrayList<>();
        adapter = new GroupAdapter(groupList, this);
        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGroups.setAdapter(adapter);
    }

    public void loadGroups() {
        new Thread(() -> {
            List<Group> groups = database.groupDao().getAllGroups();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    groupList.clear();
                    groupList.addAll(groups);
                    adapter.notifyDataSetChanged();

                    if (groups.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        rvGroups.setVisibility(View.GONE);
                    } else {
                        layoutEmptyState.setVisibility(View.GONE);
                        rvGroups.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onGroupClick(Group group) {
        openGroupDetail(group);
    }

    @Override
    public void onViewExpensesClick(Group group) {
        openGroupDetail(group);
    }

    private void openGroupDetail(Group group) {
        Intent intent = new Intent(getContext(), GroupDetailActivity.class);
        intent.putExtra("groupId", group.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGroups();
    }
}