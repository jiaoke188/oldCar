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
import com.example.carrentalapp.data.entity.CarEntity;
import com.example.carrentalapp.ui.adapter.CarAdapter;
import com.example.carrentalapp.util.SessionManager;

import java.util.List;

public class CarManagementActivity extends AppCompatActivity implements CarAdapter.OnItemClickListener {

    private CarAdapter adapter;
    private DataRepository repository;
    private SessionManager sessionManager;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_management);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);

        searchInput = findViewById(R.id.inputSearch);
        Button searchButton = findViewById(R.id.buttonSearch);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        View emptyView = findViewById(R.id.emptyView);
        View addButton = findViewById(R.id.fabAdd);

        adapter = new CarAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> loadCars(searchInput.getText().toString().trim(), emptyView));
        addButton.setOnClickListener(v -> startEdit(0));

        if (com.example.carrentalapp.data.UserRole.CUSTOMER.equals(sessionManager.getRole())) {
            addButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCars(searchInput.getText().toString().trim(), findViewById(R.id.emptyView));
    }

    private void loadCars(String keyword, View emptyView) {
        repository.searchCars(keyword, new RepositoryCallback<List<CarEntity>>() {
            @Override
            public void onComplete(List<CarEntity> result) {
                adapter.submitList(result);
                emptyView.setVisibility(result == null || result.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void startEdit(long carId) {
        Intent intent = new Intent(this, CarEditActivity.class);
        intent.putExtra(CarEditActivity.EXTRA_CAR_ID, carId);
        startActivity(intent);
    }

    @Override
    public void onItemClick(CarEntity entity) {
        if (com.example.carrentalapp.data.UserRole.CUSTOMER.equals(sessionManager.getRole())) {
            Toast.makeText(this, "无权限操作", Toast.LENGTH_SHORT).show();
            return;
        }
        startEdit(entity.getId());
    }

    @Override
    public void onItemLongClick(CarEntity entity) {
        if (com.example.carrentalapp.data.UserRole.CUSTOMER.equals(sessionManager.getRole())) {
            Toast.makeText(this, "无权限操作", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage("确认删除该车辆吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repository.deleteCar(entity, sessionManager.getUsername(), new RepositoryCallback<Boolean>() {
                            @Override
                            public void onComplete(Boolean result) {
                                Toast.makeText(CarManagementActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                loadCars(searchInput.getText().toString().trim(), findViewById(R.id.emptyView));
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
