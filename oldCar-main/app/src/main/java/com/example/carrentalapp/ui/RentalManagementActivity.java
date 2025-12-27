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
import com.example.carrentalapp.data.entity.RentalOrderEntity;
import com.example.carrentalapp.data.model.OrderWithDetail;
import com.example.carrentalapp.ui.adapter.OrderAdapter;
import com.example.carrentalapp.util.SessionManager;

import java.io.File;
import java.util.List;

public class RentalManagementActivity extends AppCompatActivity implements OrderAdapter.OnItemActionListener {

    private static final String[] STATUS_OPTIONS = new String[]{"已预定", "进行中", "已完成", "已取消"};

    private DataRepository repository;
    private SessionManager sessionManager;
    private OrderAdapter adapter;
    private EditText searchInput;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_management);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);

        searchInput = findViewById(R.id.inputSearch);
        Button searchButton = findViewById(R.id.buttonSearch);
        Button addButton = findViewById(R.id.buttonAddOrder);
        Button exportButton = findViewById(R.id.buttonExport);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);

        adapter = new OrderAdapter();
        adapter.setListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> loadOrders(searchInput.getText().toString().trim()));
        addButton.setOnClickListener(v -> startActivity(new Intent(this, OrderEditActivity.class)));
        exportButton.setOnClickListener(v -> exportOrders());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(searchInput.getText().toString().trim());
    }

    private void loadOrders(String keyword) {
        repository.searchOrders(keyword, new RepositoryCallback<List<OrderWithDetail>>() {
            @Override
            public void onComplete(List<OrderWithDetail> result) {
                adapter.submitList(result);
                emptyView.setVisibility(result == null || result.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void exportOrders() {
        repository.exportOrders(this, new RepositoryCallback<File>() {
            @Override
            public void onComplete(File result) {
                if (result != null) {
                    String message = getString(R.string.export_success_message, result.getAbsolutePath());
                    Toast.makeText(RentalManagementActivity.this, message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RentalManagementActivity.this, "导出失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onEdit(OrderWithDetail order) {
        Intent intent = new Intent(this, OrderEditActivity.class);
        intent.putExtra(OrderEditActivity.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
    }

    @Override
    public void onStatusChange(OrderWithDetail order) {
        new AlertDialog.Builder(this)
                .setTitle("更新订单状态")
                .setItems(STATUS_OPTIONS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repository.updateOrderStatus(order.getId(), STATUS_OPTIONS[which], sessionManager.getUsername(), new RepositoryCallback<Boolean>() {
                            @Override
                            public void onComplete(Boolean result) {
                                Toast.makeText(RentalManagementActivity.this, "状态已更新", Toast.LENGTH_SHORT).show();
                                loadOrders(searchInput.getText().toString().trim());
                            }
                        });
                    }
                })
                .show();
    }

    @Override
    public void onDelete(OrderWithDetail order) {
        new AlertDialog.Builder(this)
                .setMessage("确认删除该订单吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RentalOrderEntity entity = new RentalOrderEntity();
                        entity.setId(order.getId());
                        entity.setOrderCode(order.getOrderCode());
                        repository.deleteOrder(entity, sessionManager.getUsername(), new RepositoryCallback<Boolean>() {
                            @Override
                            public void onComplete(Boolean result) {
                                Toast.makeText(RentalManagementActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                loadOrders(searchInput.getText().toString().trim());
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
