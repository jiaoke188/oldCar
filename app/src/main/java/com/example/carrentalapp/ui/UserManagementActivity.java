package com.example.carrentalapp.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.entity.UserEntity;
import com.example.carrentalapp.ui.adapter.UserAdapter;
import com.example.carrentalapp.util.SessionManager;

import java.util.List;

public class UserManagementActivity extends AppCompatActivity implements UserAdapter.OnItemClickListener {

    private UserAdapter adapter;
    private DataRepository repository;
    private SessionManager sessionManager;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sessionManager = new SessionManager(this);
        repository = DataRepository.getInstance(this);

        searchInput = findViewById(R.id.inputSearch);
        Button searchButton = findViewById(R.id.buttonSearch);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        View emptyView = findViewById(R.id.emptyView);
        View addButton = findViewById(R.id.fabAdd);

        adapter = new UserAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 搜索按钮
        searchButton.setOnClickListener(v -> loadUsers(searchInput.getText().toString().trim(), emptyView));
        // 新增用户
        addButton.setOnClickListener(v -> startEditActivity(0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        View emptyView = findViewById(R.id.emptyView);
        loadUsers(searchInput.getText().toString().trim(), emptyView);
    }

    private void loadUsers(String keyword, View emptyView) {
        repository.searchUsers(keyword, new RepositoryCallback<List<UserEntity>>() {
            @Override
            public void onComplete(List<UserEntity> result) {
                adapter.submitList(result);
                emptyView.setVisibility(result == null || result.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void startEditActivity(long userId) {
        Intent intent = new Intent(this, UserEditActivity.class);
        intent.putExtra(UserEditActivity.EXTRA_USER_ID, userId);
        startActivity(intent);
    }

    @Override
    public void onItemClick(UserEntity entity) {
        startEditActivity(entity.getId());
    }

    @Override
    public void onItemLongClick(UserEntity entity) {
        if (entity.getId() == sessionManager.getUserId()) {
            Toast.makeText(this, "无法删除当前登录用户", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage("确认删除该用户吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repository.deleteUser(entity, sessionManager.getUsername(), new RepositoryCallback<Boolean>() {
                            @Override
                            public void onComplete(Boolean result) {
                                Toast.makeText(UserManagementActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                loadUsers(searchInput.getText().toString().trim(), findViewById(R.id.emptyView));
                            }
                        });
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
