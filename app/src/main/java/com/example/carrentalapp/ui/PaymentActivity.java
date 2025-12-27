package com.example.carrentalapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.entity.CarEntity;
import com.example.carrentalapp.ui.adapter.CarAdapter;
import com.example.carrentalapp.util.SessionManager;

import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DataRepository repository;
    private RecyclerView searchResultsRecyclerView;
    private CarAdapter carAdapter;
    private TextView textSearchKeyword;
    private TextView textReturnDate;
    private Button paymentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sessionManager = new SessionManager(this);
        repository = DataRepository.getInstance(this);

        // 获取从 HomeActivity 或 RentalManagementActivity 传过来的参数
        Intent intent = getIntent();
        String searchKeyword = intent.getStringExtra("searchKeyword");
        String returnDate = intent.getStringExtra("returnDate");
        long orderId = intent.getLongExtra("orderId", 0);
        String orderCode = intent.getStringExtra("orderCode");
        double totalAmount = intent.getDoubleExtra("totalAmount", 0);
        boolean isFromRentalManagement = intent.getBooleanExtra("isFromRentalManagement", false);

        // 初始化UI控件
        textSearchKeyword = findViewById(R.id.textSearchKeyword);
        textReturnDate = findViewById(R.id.textReturnDate);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        paymentButton = findViewById(R.id.paymentButton);

        // 根据来源显示不同的信息
        if (isFromRentalManagement) {
            // 来自订单列表的支付请求
            textSearchKeyword.setText("订单号: " + orderCode);
            if (TextUtils.isEmpty(returnDate)) {
                textReturnDate.setText("支付金额: ¥" + totalAmount);
            } else {
                textReturnDate.setText("还车日期: " + returnDate);
            }
            // 隐藏车辆搜索列表
            searchResultsRecyclerView.setVisibility(android.view.View.GONE);
        } else {
            // 来自首页的支付请求
            // 设置搜索关键字显示
            if (TextUtils.isEmpty(searchKeyword)) {
                textSearchKeyword.setText("搜索条件: 全部车型");
            } else {
                textSearchKeyword.setText("搜索条件: " + searchKeyword);
            }

            // 设置还车日期显示
            if (TextUtils.isEmpty(returnDate)) {
                textReturnDate.setText("还车日期: 未设置");
            } else {
                textReturnDate.setText("还车日期: " + returnDate);
            }

            // 设置 RecyclerView
            searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            carAdapter = new CarAdapter();
            carAdapter.setOnItemClickListener(new CarAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(CarEntity entity) {
                    // 点击汽车项目时的处理，可显示详情或直接选择
                    Toast.makeText(PaymentActivity.this, "已选择: " + entity.getName(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onItemLongClick(CarEntity entity) {
                    // 长按处理
                }
            });
            searchResultsRecyclerView.setAdapter(carAdapter);

            // 加载搜索结果
            loadSearchResults(searchKeyword);
        }

        // 支付按钮点击事件
        paymentButton.setOnClickListener(v -> {
            if (isFromRentalManagement) {
                // 从订单列表来的支付请求，直接处理支付
                performOrderPayment(orderId, orderCode, totalAmount);
            } else if (carAdapter.getItemCount() == 0) {
                Toast.makeText(this, "请先选择车辆", Toast.LENGTH_SHORT).show();
            } else {
                // 执行支付逻辑
                performPayment(searchKeyword, returnDate);
            }
        });

        // 如果不是从RentalManagement来的，加载搜索结果
        if (!isFromRentalManagement) {
            loadSearchResults(searchKeyword);
        }
    }

    private void loadSearchResults(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            // 加载所有车型
            repository.loadAllCars(cars -> {
                if (cars != null) {
                    carAdapter.submitList(cars);
                }
            });
        } else {
            // 搜索特定车型
            repository.searchCars(keyword, cars -> {
                if (cars != null) {
                    carAdapter.submitList(cars);
                }
            });
        }
    }

    private void performPayment(String searchKeyword, String returnDate) {
        // TODO: 实现实际的支付逻辑
        Toast.makeText(this, "支付处理中...", Toast.LENGTH_SHORT).show();
        // 这里可以调用支付 API 或跳转到支付页面
    }

    private void performOrderPayment(long orderId, String orderCode, double totalAmount) {
        // 订单支付逻辑：支付成功后更新订单状态为"进行中"
        Toast.makeText(this, "支付处理中...", Toast.LENGTH_SHORT).show();

        // 模拟支付成功，实际应调用支付API
        repository.updateOrderStatus(orderId, "进行中", "system", result -> {
            if (result != null && result) {
                Toast.makeText(PaymentActivity.this, "支付成功！订单已开始", Toast.LENGTH_SHORT).show();
                // 返回到RentalManagementActivity
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(PaymentActivity.this, "支付失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
