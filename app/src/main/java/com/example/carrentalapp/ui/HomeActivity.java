package com.example.carrentalapp.ui;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.UserRole;
import com.example.carrentalapp.data.entity.CarEntity;
import com.example.carrentalapp.ui.adapter.CarAdapter;
import com.example.carrentalapp.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DataRepository repository;
    private RecyclerView carRecyclerView;
    private CarAdapter carAdapter;
    private EditText searchCarInput;
    private Button searchCarButton;
    private Button goPaymentButton;
    private TextView textReturnDateReminder;
    private TextView textNoReturnTip;

    private String returnDate = null; // 还车日期

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);
        repository = DataRepository.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            finish();
            return;
        }

        TextView welcomeText = findViewById(R.id.textWelcome);
        String display = sessionManager.getDisplayName();
        if (display == null || display.isEmpty()) {
            display = sessionManager.getUsername();
        }
        String welcome = getString(R.string.home_welcome_message, display, sessionManager.getUsername(),
                com.example.carrentalapp.data.UserRole.toDisplay(sessionManager.getRole()));
        welcomeText.setText(welcome);

        Button userManageButton = findViewById(R.id.buttonUserManage);
        Button carManageButton = findViewById(R.id.buttonCarManage);
        Button rentalManageButton = findViewById(R.id.buttonRentalManage);
        Button marketButton = findViewById(R.id.buttonMarket);
        Button favoriteButton = findViewById(R.id.buttonFavorite);
        Button logButton = findViewById(R.id.buttonLog);

        if ("CUSTOMER".equals(sessionManager.getRole())) {
            carManageButton.setVisibility(View.GONE);
        } else {
            carManageButton.setVisibility(View.VISIBLE);
        }

        if (UserRole.CUSTOMER.equals(sessionManager.getRole())) {
            logButton.setVisibility(View.GONE);
        }

        if (UserRole.MERCHANT.equals(sessionManager.getRole())) {
            logButton.setVisibility(View.GONE);
        }
        // 模块导航事件
        userManageButton.setOnClickListener(v -> startActivity(new Intent(this, UserManagementActivity.class)));
        carManageButton.setOnClickListener(v -> startActivity(new Intent(this, CarManagementActivity.class)));
        rentalManageButton.setOnClickListener(v -> startActivity(new Intent(this, RentalManagementActivity.class)));
        marketButton.setOnClickListener(v -> startActivity(new Intent(this, CarMarketActivity.class)));
        favoriteButton.setOnClickListener(v -> startActivity(new Intent(this, FavoriteActivity.class)));
        logButton.setOnClickListener(v -> startActivity(new Intent(this, LogActivity.class)));

        // 非管理员隐藏系统用户管理
        if (!UserRole.SUPER_ADMIN.equals(sessionManager.getRole())) {
            userManageButton.setVisibility(View.GONE);
        }

        // 初始化搜索框和列表
        initCarListView();
        loadAllCars();

        // 加载并显示用户的进行中的订单信息
        loadAndDisplayRentalInfo();
    }

    private void loadAndDisplayRentalInfo() {
        // 获取当前用户的所有订单，查找"进行中"的订单中最近的还车日期
        repository.searchOrders("", orders -> {
            if (orders != null && !orders.isEmpty()) {
                // 找到最近的还车日期（最急迫的订单）
                long now = System.currentTimeMillis();
                com.example.carrentalapp.data.model.OrderWithDetail closestOrder = null;
                long closestEndTime = Long.MAX_VALUE;

                for (com.example.carrentalapp.data.model.OrderWithDetail order : orders) {
                    if ("进行中".equals(order.getStatus())) {
                        if (order.getEndDate() < closestEndTime) {
                            closestEndTime = order.getEndDate();
                            closestOrder = order;
                        }
                    }
                }

                if (closestOrder != null) {
                    // 计算还车倒计时
                    long daysRemaining = (closestOrder.getEndDate() - now) / (24 * 60 * 60 * 1000L);

                    if (daysRemaining < 0) {
                        daysRemaining = 0;
                    }

                    // 显示提醒文本
                    textReturnDateReminder.setText(String.format("还车倒计时: 订单 %s 还有 %d 天",
                            closestOrder.getOrderCode(), daysRemaining));

                    // 如果少于3天，以红色显示
                    if (daysRemaining <= 3) {
                        textReturnDateReminder.setTextColor(android.graphics.Color.RED);
                    } else {
                        textReturnDateReminder.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    }
                } else {
                    // 没有进行中的订单，显示提示信息
                    textReturnDateReminder.setText("还车日期: 无需还车");
                    textNoReturnTip.setText("当前无进行中的订单");
                    textNoReturnTip.setVisibility(View.VISIBLE);
                }
            } else {
                // 订单列表为空
                textReturnDateReminder.setText("还车日期: 无需还车");
                textNoReturnTip.setText("当前无进行中的订单");
                textNoReturnTip.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回首页时，自动更新还车提醒和车辆列表
        loadAndDisplayRentalInfo();
        loadAllCars();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            sessionManager.clear();
            redirectToLogin();
            finish();
            return true;
        } else if (id == R.id.action_logs) {
            startActivity(new Intent(this, LogActivity.class));
            return true;
        } else if (id == R.id.action_favorite) {
            startActivity(new Intent(this, FavoriteActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initCarListView() {
        carRecyclerView = findViewById(R.id.carRecyclerView);
        searchCarInput = findViewById(R.id.searchCarInput);
        searchCarButton = findViewById(R.id.searchCarButton);
        goPaymentButton = findViewById(R.id.goPaymentButton);
        textReturnDateReminder = findViewById(R.id.textReturnDateReminder);
        textNoReturnTip = findViewById(R.id.textNoReturnTip);

        // 设置RecyclerView
        carRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        carAdapter = new CarAdapter();
        carAdapter.setOnItemClickListener(new CarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CarEntity entity) {
                // 点击查看详情时的处理
            }

            @Override
            public void onItemLongClick(CarEntity entity) {
                // 长按处理
            }
        });
        carRecyclerView.setAdapter(carAdapter);

        // 搜索按钮点击事件
        searchCarButton.setOnClickListener(v -> performSearch());

        // 搜索框回车事件
        searchCarInput.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        // 支付按钮点击事件 - 跳转到搜索结果支付界面
        goPaymentButton.setOnClickListener(v -> goToPayment());

        // 还车日期提醒点击，弹出日期选择器
        textReturnDateReminder.setOnClickListener(v -> showReturnDatePicker());

        // 初始化还车日期提醒
        updateReturnDateReminder();
    }

    private void goToPayment() {
        String keyword = searchCarInput.getText().toString().trim();
        // 创建 Intent 跳转到支付界面
        Intent intent = new Intent(this, PaymentActivity.class);
        // 传递搜索关键字和还车日期
        intent.putExtra("searchKeyword", keyword);
        intent.putExtra("returnDate", returnDate);
        startActivity(intent);
    }

    private void showReturnDatePicker() {
        // 使用 DatePickerDialog 让用户选择还车日期
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                returnDate = sdf.format(selectedDate.getTime());
                updateReturnDateReminder();

                // 设置还车日期提醒
                setReturnDateReminder(selectedDate);

                android.widget.Toast
                        .makeText(HomeActivity.this, "已设置还车日期: " + returnDate, android.widget.Toast.LENGTH_SHORT)
                        .show();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setReturnDateReminder(Calendar returnDateCalendar) {
        // 获取提醒时间（还车日期前一天的上午9点）
        Calendar reminderTime = (Calendar) returnDateCalendar.clone();
        reminderTime.add(Calendar.DAY_OF_MONTH, -1);
        reminderTime.set(Calendar.HOUR_OF_DAY, 9);
        reminderTime.set(Calendar.MINUTE, 0);
        reminderTime.set(Calendar.SECOND, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReturnDateReminderReceiver.class);
        intent.putExtra("returnDate", returnDate);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(),
                        pendingIntent);
            } catch (Exception e) {
                android.util.Log.e("HomeActivity", "设置提醒失败: " + e.getMessage());
            }
        }
    }

    private void updateReturnDateReminder() {
        if (returnDate == null || returnDate.isEmpty()) {
            textReturnDateReminder.setText("还车日期: 未设置");
            textNoReturnTip.setVisibility(View.VISIBLE);
        } else {
            textReturnDateReminder.setText("还车日期: " + returnDate);
            textNoReturnTip.setVisibility(View.GONE);
        }
    }

    private void performSearch() {
        String keyword = searchCarInput.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            loadAllCars();
        } else {
            repository.searchCars(keyword, cars -> {
                if (cars != null) {
                    carAdapter.submitList(cars);
                }
            });
        }
    }

    private void loadAllCars() {
        repository.loadAllCars(cars -> {
            if (cars != null) {
                carAdapter.submitList(cars);
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
