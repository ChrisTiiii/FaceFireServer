package com.example.juicekaaa.fireserver.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.juicekaaa.fireserver.R;
import com.example.juicekaaa.fireserver.api.FaceApi;
import com.example.juicekaaa.fireserver.db.DBManager;
import com.example.juicekaaa.fireserver.entity.Group;


import java.util.List;

public class UserGroupManagerActivity extends AppCompatActivity implements View.OnClickListener{

    private Button viewGroupBtn;
    private Button addGroupBtn;
    private Button userRegBtn;
    private Button batchRegBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group_manager);

        viewGroupBtn = (Button) findViewById(R.id.view_group_btn);
        addGroupBtn = (Button) findViewById(R.id.add_group_btn);
        userRegBtn = (Button) findViewById(R.id.user_reg_btn);
        batchRegBtn = (Button) findViewById(R.id.batch_import_btn);

        userRegBtn.setOnClickListener(this);
        viewGroupBtn.setOnClickListener(this);
        addGroupBtn.setOnClickListener(this);
        batchRegBtn.setOnClickListener(this);

        DBManager.getInstance().init(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        if (v == userRegBtn) {
            List<Group> groupList = FaceApi.getInstance().getGroupList(0, 1000);
            if (groupList.size() == 0) {
                Toast.makeText(this, "请先添加分组", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, RegActivity.class);
            startActivity(intent);
        } else if (v == viewGroupBtn) {
            List<Group> groupList = FaceApi.getInstance().getGroupList(0, 1000);

            if (groupList.size() <= 0) {
                Toast.makeText(this, "还没有分组，请创建分组并添加用户", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, GroupListActivity.class);
            startActivity(intent);
        } else if (v == addGroupBtn) {

            Intent intent = new Intent(this, AddGroupActivity.class);
            startActivity(intent);
        } else if (v == batchRegBtn) {
            List<Group> groupList = FaceApi.getInstance().getGroupList(0, 1000);
            if (groupList.size() == 0) {
                Toast.makeText(this, "请先添加分组", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, BatchImportActivity.class);
            startActivity(intent);
        }
    }
}
