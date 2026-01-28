package com.ajproject.moneymap.utils;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.Group;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupDialog {

    private Context context;
    private Dialog dialog;
    private OnGroupCreatedListener listener;

    private TextInputEditText etGroupName, etMemberName;
    private TextView tvMembersList;
    private Button btnAddMember, btnCreate, btnCancel;
    private List<String> members;

    public interface OnGroupCreatedListener {
        void onGroupCreated();
    }

    public CreateGroupDialog(Context context, OnGroupCreatedListener listener) {
        this.context = context;
        this.listener = listener;
        this.members = new ArrayList<>();
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_create_group);
        dialog.getWindow().setLayout(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        etGroupName = dialog.findViewById(R.id.et_group_name);
        etMemberName = dialog.findViewById(R.id.et_member_name);
        tvMembersList = dialog.findViewById(R.id.tv_members_list);
        btnAddMember = dialog.findViewById(R.id.btn_add_member);
        btnCreate = dialog.findViewById(R.id.btn_create);
        btnCancel = dialog.findViewById(R.id.btn_cancel);

        btnAddMember.setOnClickListener(v -> addMember());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnCreate.setOnClickListener(v -> createGroup());

        updateMembersList();
    }

    private void addMember() {
        String memberName = etMemberName.getText().toString().trim();

        if (memberName.isEmpty()) {
            Toast.makeText(context, "Please enter member name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (members.contains(memberName)) {
            Toast.makeText(context, "Member already added", Toast.LENGTH_SHORT).show();
            return;
        }

        members.add(memberName);
        etMemberName.setText("");
        updateMembersList();
    }

    private void updateMembersList() {
        if (members.isEmpty()) {
            tvMembersList.setText("Members: None added yet");
        } else {
            tvMembersList.setText("Members: " + String.join(", ", members));
        }
    }

    private void createGroup() {
        String groupName = etGroupName.getText().toString().trim();

        if (groupName.isEmpty()) {
            Toast.makeText(context, "Please enter group name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (members.isEmpty()) {
            Toast.makeText(context, "Please add at least one member", Toast.LENGTH_SHORT).show();
            return;
        }

        String membersStr = String.join(", ", members);
        long date = System.currentTimeMillis();

        Group group = new Group(groupName, membersStr, date);

        new Thread(() -> {
            MoneyMapDatabase db = MoneyMapDatabase.getInstance(context);
            db.groupDao().insert(group);

            if (listener != null) {
                listener.onGroupCreated();
            }
        }).start();

        Toast.makeText(context, "Group created successfully!", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    public void show() {
        dialog.show();
    }
}