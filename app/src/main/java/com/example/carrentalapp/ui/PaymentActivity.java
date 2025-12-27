package com.example.carrentalapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.entity.CarEntity;
import com.example.carrentalapp.data.entity.RentalOrderEntity;
import com.example.carrentalapp.ui.adapter.CarAdapter;
import com.example.carrentalapp.util.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class PaymentActivity extends AppCompatActivity {

    private static final long PAYMENT_TIMEOUT_MS = 15 * 60 * 1000L;
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    private static final String PREFS_PAYMENT = "payment_session";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    private SessionManager sessionManager;
    private DataRepository repository;
    private RecyclerView searchResultsRecyclerView;
    private CarAdapter carAdapter;
    private TextView textSearchKeyword;
    private TextView textReturnDate;
    private TextView textCountdown;
    private Button paymentButton;
    private RadioGroup paymentMethodGroup;
    private CountDownTimer countDownTimer;
    private SharedPreferences paymentPreferences;
    private CarEntity selectedCar;
    private final AtomicBoolean processingPayment = new AtomicBoolean(false);

    private long orderId;
    private String orderCode;
    private double totalAmount;
    private boolean isFromRentalManagement;
    private String searchKeyword;
    private String returnDate;
    private String countdownKey;

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
        searchKeyword = intent.getStringExtra("searchKeyword");
        returnDate = intent.getStringExtra("returnDate");
        orderId = intent.getLongExtra("orderId", 0);
        orderCode = intent.getStringExtra("orderCode");
        totalAmount = intent.getDoubleExtra("totalAmount", 0);
        isFromRentalManagement = intent.getBooleanExtra("isFromRentalManagement", false);
        countdownKey = buildCountdownKey(orderId, orderCode);

        // 初始化UI控件
        textSearchKeyword = findViewById(R.id.textSearchKeyword);
        textReturnDate = findViewById(R.id.textReturnDate);
        textCountdown = findViewById(R.id.textCountdown);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        paymentButton = findViewById(R.id.paymentButton);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);

        paymentPreferences = getSharedPreferences(PREFS_PAYMENT, MODE_PRIVATE);
        restorePaymentSelection();
        startCountdownIfNeeded();

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
            searchResultsRecyclerView.setVisibility(View.GONE);
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
            carAdapter.setSelectionHighlightEnabled(true);
            carAdapter.setOnItemClickListener(new CarAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(CarEntity entity) {
                    // 点击汽车项目时的处理，可显示详情或直接选择
                    selectedCar = entity;
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
            if (!ensurePaymentMethodSelected()) {
                return;
            }
            if (isFromRentalManagement) {
                // 从订单列表来的支付请求，直接处理支付
                performOrderPayment();
            } else {
                // 执行支付逻辑
                performPayment();
            }
        });

        // 如果不是从RentalManagement来的，加载搜索结果
        if (!isFromRentalManagement) {
            loadSearchResults(searchKeyword);
        }
    }

    private void startCountdownIfNeeded() {
        long start = paymentPreferences.getLong(countdownKey, 0L);
        if (start == 0L) {
            start = System.currentTimeMillis();
            paymentPreferences.edit().putLong(countdownKey, start).apply();
        }

        long elapsed = System.currentTimeMillis() - start;
        long remaining = PAYMENT_TIMEOUT_MS - elapsed;
        if (remaining <= 0L) {
            handlePaymentTimeout();
            return;
        }

        textCountdown.setVisibility(View.VISIBLE);
        updateCountdownText(remaining);
        countDownTimer = new CountDownTimer(remaining, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateCountdownText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                handlePaymentTimeout();
            }
        };
        countDownTimer.start();
    }

    private void updateCountdownText(long millis) {
        long totalSeconds = millis / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        String text = String.format(Locale.CHINA, "支付剩余时间: %02d:%02d", minutes, seconds);
        textCountdown.setText(text);
    }

    private void restorePaymentSelection() {
        int selectedMethod = paymentPreferences.getInt(countdownKey + "_method", -1);
        if (selectedMethod != -1) {
            paymentMethodGroup.check(selectedMethod);
        }
        paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> paymentPreferences.edit()
                .putInt(countdownKey + "_method", checkedId)
                .apply());
    }

    private boolean ensurePaymentMethodSelected() {
        if (paymentMethodGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "请选择支付方式", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loadSearchResults(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            // 加载所有车型
            repository.loadAllCars(cars -> {
                if (cars != null) {
                    carAdapter.submitList(cars);
                    selectedCar = carAdapter.getSelectedCar();
                }
            });
        } else {
            // 搜索特定车型
            repository.searchCars(keyword, cars -> {
                if (cars != null) {
                    carAdapter.submitList(cars);
                    selectedCar = carAdapter.getSelectedCar();
                }
            });
        }
    }

    private void performPayment() {
        if (isCountdownExpired()) {
            handlePaymentTimeout();
            return;
        }
        if (selectedCar == null) {
            Toast.makeText(this, "请选择车辆", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCar.getInventory() <= 0) {
            Toast.makeText(this, "该车辆暂无库存", Toast.LENGTH_SHORT).show();
            return;
        }
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "用户信息失效，请重新登录", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!processingPayment.compareAndSet(false, true)) {
            return;
        }
        paymentButton.setEnabled(false);

        long startDay = truncateToDay(System.currentTimeMillis());
        long endDay = resolveEndDay(startDay);
        if (endDay < startDay) {
            endDay = startDay;
        }
        int totalDays = (int) ((endDay - startDay) / MILLIS_PER_DAY) + 1;
        if (totalDays <= 0) {
            totalDays = 1;
        }
        double amount = selectedCar.getDailyPrice() * totalDays;
        totalAmount = amount;

        RentalOrderEntity entity = new RentalOrderEntity();
        entity.setCarId(selectedCar.getId());
        entity.setUserId(userId);
        entity.setStartDate(startDay);
        entity.setEndDate(endDay + MILLIS_PER_DAY - 1);
        entity.setTotalDays(totalDays);
        entity.setTotalAmount(amount);
        entity.setStatus("进行中");
        entity.setNotes("支付方式:" + resolvePaymentMethodLabel());

        Toast.makeText(this, "支付处理中...", Toast.LENGTH_SHORT).show();
        repository.saveOrder(entity, sessionManager.getUsername(), result -> {
            if (result != null && result > 0) {
                long createdOrderId = result;
                repository.loadOrderById(createdOrderId, loadedOrder -> {
                    String savedOrderCode = loadedOrder != null ? loadedOrder.getOrderCode() : "";
                    updateInventoryAndFinish(createdOrderId, savedOrderCode);
                });
            } else {
                processingPayment.set(false);
                paymentButton.setEnabled(true);
                Toast.makeText(PaymentActivity.this, "订单创建失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performOrderPayment() {
        if (isCountdownExpired()) {
            handlePaymentTimeout();
            return;
        }
        // 订单支付逻辑：支付成功后更新订单状态为"进行中"
        Toast.makeText(this, "支付处理中...", Toast.LENGTH_SHORT).show();

        // 模拟支付成功，实际应调用支付API
        repository.updateOrderStatus(orderId, "进行中", "system", result -> {
            if (result != null && result) {
                Toast.makeText(PaymentActivity.this, "支付成功！订单已开始", Toast.LENGTH_SHORT).show();
                // 返回到RentalManagementActivity
                clearPaymentSession();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(PaymentActivity.this, "支付失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isCountdownExpired() {
        long start = paymentPreferences.getLong(countdownKey, 0L);
        if (start == 0L) {
            return true;
        }
        return System.currentTimeMillis() - start >= PAYMENT_TIMEOUT_MS;
    }

    private void handlePaymentTimeout() {
        cancelTimer();
        paymentButton.setEnabled(false);
        textCountdown.setVisibility(View.VISIBLE);
        textCountdown.setText("支付剩余时间: 00:00");
        Toast.makeText(this, "支付超时，订单已取消", Toast.LENGTH_LONG).show();
        clearPaymentSelection();
        disablePaymentOptions();
        if (orderId > 0) {
            repository.updateOrderStatus(orderId, "已取消", "system", result -> {
                // 即便取消失败也仅提示一次
                if (result == null || !result) {
                    Toast.makeText(PaymentActivity.this, "订单状态更新失败，请刷新重试", Toast.LENGTH_SHORT).show();
                }
            });
        }
        clearPaymentSession();
        finish();
    }

    private void clearPaymentSession() {
        paymentPreferences.edit()
                .remove(countdownKey)
                .remove(countdownKey + "_method")
                .apply();
        cancelTimer();
    }

    private void clearPaymentSelection() {
        paymentPreferences.edit().remove(countdownKey + "_method").apply();
        paymentMethodGroup.clearCheck();
    }

    private void disablePaymentOptions() {
        paymentMethodGroup.setEnabled(false);
        for (int i = 0; i < paymentMethodGroup.getChildCount(); i++) {
            View child = paymentMethodGroup.getChildAt(i);
            child.setEnabled(false);
        }
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private String buildCountdownKey(long orderIdValue, String orderCodeValue) {
        if (orderIdValue > 0) {
            return "order_" + orderIdValue;
        }
        if (!TextUtils.isEmpty(orderCodeValue)) {
            return "order_code_" + orderCodeValue;
        }
        return "general_payment";
    }

    private void updateInventoryAndFinish(long createdOrderId, String savedOrderCode) {
        String operator = sessionManager.getUsername();
        repository.decreaseCarInventory(selectedCar.getId(), operator, result -> {
            if (result == null || !result) {
                Toast.makeText(PaymentActivity.this, "库存更新失败，请联系管理员", Toast.LENGTH_SHORT).show();
            }
            clearPaymentSession();
            processingPayment.set(false);
            Toast.makeText(PaymentActivity.this, "支付成功！订单已开始", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PaymentActivity.this, RentalManagementActivity.class);
            intent.putExtra(RentalManagementActivity.EXTRA_HIGHLIGHT_ORDER_ID, createdOrderId);
            intent.putExtra(RentalManagementActivity.EXTRA_INITIAL_KEYWORD, savedOrderCode);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private long truncateToDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long resolveEndDay(long fallbackDay) {
        if (TextUtils.isEmpty(returnDate)) {
            return fallbackDay;
        }
        try {
            long parsed = DATE_FORMAT.parse(returnDate).getTime();
            long normalized = truncateToDay(parsed);
            return Math.max(normalized, fallbackDay);
        } catch (ParseException e) {
            Toast.makeText(this, "还车日期格式错误，已使用今天", Toast.LENGTH_SHORT).show();
            return fallbackDay;
        }
    }

    private String resolvePaymentMethodLabel() {
        int checkedId = paymentMethodGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioWeChat) {
            return "微信";
        }
        if (checkedId == R.id.radioAlipay) {
            return "支付宝";
        }
        if (checkedId == R.id.radioBankCard) {
            return "银行卡";
        }
        return "未知";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }
}
