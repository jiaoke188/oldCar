package com.example.carrentalapp.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
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

    public static final String EXTRA_INITIAL_KEYWORD = "initialKeyword";
    public static final String EXTRA_HIGHLIGHT_ORDER_ID = "highlightOrderId";

    private static final String[] STATUS_OPTIONS = new String[] { "已预定", "进行中", "已完成", "已取消" };

    private DataRepository repository;
    private SessionManager sessionManager;
    private OrderAdapter adapter;
    private EditText searchInput;
    private View emptyView;
    private RecyclerView recyclerView;
    private long highlightOrderId;
    private boolean highlightHandled;

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
        Button exportButton = findViewById(R.id.buttonExport);
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);

        adapter = new OrderAdapter();
        adapter.setListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        String initialKeyword = intent.getStringExtra(EXTRA_INITIAL_KEYWORD);
        highlightOrderId = intent.getLongExtra(EXTRA_HIGHLIGHT_ORDER_ID, 0L);
        highlightHandled = highlightOrderId <= 0;
        if (!TextUtils.isEmpty(initialKeyword)) {
            searchInput.setText(initialKeyword);
        }

        searchButton.setOnClickListener(v -> loadOrders(searchInput.getText().toString().trim()));
        exportButton.setOnClickListener(v -> exportOrders());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(searchInput.getText().toString().trim());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String initialKeyword = intent.getStringExtra(EXTRA_INITIAL_KEYWORD);
        highlightOrderId = intent.getLongExtra(EXTRA_HIGHLIGHT_ORDER_ID, 0L);
        highlightHandled = highlightOrderId <= 0;
        if (!TextUtils.isEmpty(initialKeyword)) {
            searchInput.setText(initialKeyword);
        }
    }

    private void loadOrders(String keyword) {
        repository.searchOrders(keyword, new RepositoryCallback<List<OrderWithDetail>>() {
            @Override
            public void onComplete(List<OrderWithDetail> result) {
                adapter.submitList(result);
                if (highlightOrderId > 0 && !highlightHandled) {
                    int position = adapter.getPositionById(highlightOrderId);
                    if (position >= 0) {
                        adapter.setHighlightedOrderId(highlightOrderId);
                        recyclerView.scrollToPosition(position);
                        highlightHandled = true;
                    }
                }
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
        // 订单状态不能手动修改，根据支付状态自动更新
        Toast.makeText(this, "订单状态由系统自动管理，不能手动修改", Toast.LENGTH_SHORT).show();
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
    public void onPayment(OrderWithDetail order) {
        // 跳转到支付界面
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("orderId", order.getId());
        intent.putExtra("orderCode", order.getOrderCode());
        intent.putExtra("totalAmount", order.getTotalAmount());
        intent.putExtra("returnDate", com.example.carrentalapp.util.FormatUtils.formatDate(order.getEndDate()));
        intent.putExtra("isFromRentalManagement", true);
        startActivity(intent);
    }

    @Override
    public void onEarlyReturn(OrderWithDetail order) {
        // 显示确认对话框
        new AlertDialog.Builder(this)
                .setTitle("提前还车确认")
                .setMessage("确认提前还车吗？车辆库存将自动增加 1。")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 更新订单状态为已完成
                        repository.updateOrderStatus(order.getId(), "已完成", sessionManager.getUsername(),
                                new RepositoryCallback<Boolean>() {
                                    @Override
                                    public void onComplete(Boolean result) {
                                        if (result) {
                                            // 增加车辆库存
                                            repository.increaseCarInventory(order.getCarId(),
                                                    sessionManager.getUsername(),
                                                    new RepositoryCallback<Boolean>() {
                                                        @Override
                                                        public void onComplete(Boolean inventoryResult) {
                                                            if (inventoryResult) {
                                                                Toast.makeText(RentalManagementActivity.this,
                                                                        "提前还车成功，库存已更新",
                                                                        Toast.LENGTH_SHORT).show();
                                                                loadOrders(searchInput.getText().toString().trim());
                                                            } else {
                                                                Toast.makeText(RentalManagementActivity.this,
                                                                        "库存更新失败", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(RentalManagementActivity.this,
                                                    "订单状态更新失败", Toast.LENGTH_SHORT).show();
                                        }
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
