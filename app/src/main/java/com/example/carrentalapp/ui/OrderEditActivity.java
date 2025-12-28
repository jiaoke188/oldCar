package com.example.carrentalapp.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.UserRole;
import com.example.carrentalapp.data.entity.CarEntity;
import com.example.carrentalapp.data.entity.RentalOrderEntity;
import com.example.carrentalapp.data.entity.UserEntity;
import com.example.carrentalapp.util.FormatUtils;
import com.example.carrentalapp.util.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OrderEditActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_CAR_ID = "extra_car_id";

    private static final String[] STATUS_OPTIONS = new String[] { "已预定", "进行中", "已完成", "已取消" };

    private DataRepository repository;
    private SessionManager sessionManager;

    private Spinner carSpinner;
    private Spinner customerSpinner;
    private Spinner statusSpinner;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText notesInput;
    private TextView summaryView;
    private ProgressBar progressBar;
    private Button saveButton;

    private List<CarEntity> carList = new ArrayList<>();
    private List<UserEntity> customerList = new ArrayList<>();
    private RentalOrderEntity currentOrder;

    private long orderId;
    private long preselectCarId;
    private long startTimestamp;
    private long endTimestamp;

    private Button paymentButton; // 支付按钮
    private Button earlyReturnButton; // 提前还车按钮

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);

        carSpinner = findViewById(R.id.spinnerCar);
        customerSpinner = findViewById(R.id.spinnerCustomer);
        statusSpinner = findViewById(R.id.spinnerStatus);
        startDateInput = findViewById(R.id.inputStartDate);
        endDateInput = findViewById(R.id.inputEndDate);
        notesInput = findViewById(R.id.inputNotes);
        summaryView = findViewById(R.id.textSummary);
        progressBar = findViewById(R.id.progressBar);
        saveButton = findViewById(R.id.buttonSave);
        paymentButton = findViewById(R.id.buttonPayment);
        earlyReturnButton = findViewById(R.id.buttonEarlyReturn);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                STATUS_OPTIONS);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // 禁用状态手动修改
        statusSpinner.setEnabled(false);

        startDateInput.setOnClickListener(v -> showDatePicker(true));
        endDateInput.setOnClickListener(v -> showDatePicker(false));

        saveButton.setOnClickListener(v -> saveOrder());

        // 支付按钮点击事件
        if (paymentButton != null) {
            paymentButton.setOnClickListener(v -> processPayment());
        }

        // 提前还车按钮点击事件
        if (earlyReturnButton != null) {
            earlyReturnButton.setOnClickListener(v -> processEarlyReturn());
        }

        carSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                refreshSummary();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {

            }
        });

        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, 0);
        preselectCarId = getIntent().getLongExtra(EXTRA_CAR_ID, 0);

        loadCarList();
        loadCustomerList();
        if (orderId > 0) {
            loadOrder();
        } else {
            statusSpinner.setSelection(0);
            if (preselectCarId > 0) {
                selectCar(preselectCarId);
            }
        }
    }

    private void loadCarList() {
        repository.loadAllCars(new RepositoryCallback<List<CarEntity>>() {
            @Override
            public void onComplete(List<CarEntity> result) {
                carList = result == null ? new ArrayList<>() : result;
                List<String> names = new ArrayList<>();
                for (CarEntity car : carList) {
                    names.add(car.getName());
                }
                ArrayAdapter<String> carAdapter = new ArrayAdapter<>(OrderEditActivity.this,
                        android.R.layout.simple_spinner_item, names);
                carAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                carSpinner.setAdapter(carAdapter);
                if (preselectCarId > 0) {
                    selectCar(preselectCarId);
                }
                if (currentOrder != null) {
                    selectCar(currentOrder.getCarId());
                }
            }
        });
    }

    private void loadCustomerList() {
        repository.loadAllUsers(new RepositoryCallback<List<UserEntity>>() {
            @Override
            public void onComplete(List<UserEntity> result) {
                customerList = new ArrayList<>();
                if (result != null) {
                    for (UserEntity user : result) {
                        if (UserRole.CUSTOMER.equals(user.getRole())) {
                            customerList.add(user);
                        }
                    }
                }
                // 如果登录者是客户，则只允许自己下单
                if (UserRole.CUSTOMER.equals(sessionManager.getRole())) {
                    customerList.clear();
                    UserEntity current = new UserEntity();
                    current.setId(sessionManager.getUserId());
                    current.setUsername(sessionManager.getUsername());
                    current.setDisplayName(sessionManager.getDisplayName());
                    current.setRole(UserRole.CUSTOMER);
                    customerList.add(current);
                    customerSpinner.setEnabled(false);
                }
                List<String> names = new ArrayList<>();
                for (UserEntity user : customerList) {
                    String display = user.getDisplayName();
                    if (display == null || display.isEmpty()) {
                        display = user.getUsername();
                    }
                    names.add(display);
                }
                ArrayAdapter<String> userAdapter = new ArrayAdapter<>(OrderEditActivity.this,
                        android.R.layout.simple_spinner_item, names);
                userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                customerSpinner.setAdapter(userAdapter);
                if (currentOrder != null) {
                    selectCustomer(currentOrder.getUserId());
                } else if (UserRole.CUSTOMER.equals(sessionManager.getRole())) {
                    selectCustomer(sessionManager.getUserId());
                }
            }
        });
    }

    private void loadOrder() {
        setLoading(true);
        repository.loadOrderById(orderId, new RepositoryCallback<RentalOrderEntity>() {
            @Override
            public void onComplete(RentalOrderEntity result) {
                setLoading(false);
                if (result == null) {
                    Toast.makeText(OrderEditActivity.this, "订单不存在", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentOrder = result;
                startTimestamp = result.getStartDate();
                endTimestamp = result.getEndDate();
                startDateInput.setText(FormatUtils.formatDate(startTimestamp));
                endDateInput.setText(FormatUtils.formatDate(endTimestamp));
                notesInput.setText(result.getNotes());
                // 自动设置状态而不是从用户选择
                updateOrderStatus(result);
                refreshSummary();
                selectCar(result.getCarId());
                selectCustomer(result.getUserId());

                // 根据订单状态显示或隐藏支付按钮
                if (paymentButton != null) {
                    paymentButton.setVisibility("已预定".equals(result.getStatus()) ? View.VISIBLE : View.GONE);
                }

                // 根据订单状态显示或隐藏提前还车按钮（仅当状态为"进行中"时）
                if (earlyReturnButton != null) {
                    earlyReturnButton.setVisibility("进行中".equals(result.getStatus()) ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    private void selectCar(long carId) {
        for (int i = 0; i < carList.size(); i++) {
            if (carList.get(i).getId() == carId) {
                carSpinner.setSelection(i);
                return;
            }
        }
    }

    private void selectCustomer(long userId) {
        for (int i = 0; i < customerList.size(); i++) {
            if (customerList.get(i).getId() == userId) {
                customerSpinner.setSelection(i);
                return;
            }
        }
    }

    private void showDatePicker(boolean start) {
        Calendar calendar = Calendar.getInstance();
        long target = start ? startTimestamp : endTimestamp;
        if (target > 0) {
            calendar.setTimeInMillis(target);
        }
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            long time = c.getTimeInMillis();
            if (start) {
                startTimestamp = time;
                startDateInput.setText(FormatUtils.formatDate(time));
            } else {
                endTimestamp = time;
                endDateInput.setText(FormatUtils.formatDate(time));
            }
            refreshSummary();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void refreshSummary() {
        if (startTimestamp > 0 && endTimestamp >= startTimestamp && carSpinner.getSelectedItemPosition() >= 0
                && carSpinner.getSelectedItemPosition() < carList.size()) {
            long diff = endTimestamp - startTimestamp;
            int days = (int) (diff / (24 * 60 * 60 * 1000L)) + 1;
            if (days <= 0) {
                days = 1;
            }
            double price = carList.get(carSpinner.getSelectedItemPosition()).getDailyPrice();
            double amount = price * days;
            summaryView.setText(getString(R.string.order_summary_template, days, amount));
        } else {
            summaryView.setText("");
        }
    }

    private void saveOrder() {
        if (carList.isEmpty()) {
            Toast.makeText(this, "请先添加车辆", Toast.LENGTH_SHORT).show();
            return;
        }
        if (customerList.isEmpty()) {
            Toast.makeText(this, "请先添加客户", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startTimestamp <= 0 || endTimestamp <= 0) {
            Toast.makeText(this, "请选择起止日期", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endTimestamp < startTimestamp) {
            Toast.makeText(this, "结束日期不能早于开始日期", Toast.LENGTH_SHORT).show();
            return;
        }

        int carIndex = carSpinner.getSelectedItemPosition();
        int customerIndex = customerSpinner.getSelectedItemPosition();
        CarEntity car = carList.get(carIndex);
        UserEntity customer = customerList.get(customerIndex);

        int days = (int) ((endTimestamp - startTimestamp) / (24 * 60 * 60 * 1000L)) + 1;
        if (days <= 0) {
            days = 1;
        }
        double amount = car.getDailyPrice() * days;

        setLoading(true);
        RentalOrderEntity entity = currentOrder != null ? currentOrder : new RentalOrderEntity();
        entity.setId(orderId);
        entity.setCarId(car.getId());
        entity.setUserId(customer.getId());
        entity.setStartDate(startTimestamp);
        entity.setEndDate(endTimestamp);
        entity.setTotalDays(days);
        entity.setTotalAmount(amount);
        entity.setNotes(notesInput.getText().toString().trim());

        // 自动设置订单状态
        if (currentOrder == null) {
            // 新建订单，初始状态为"已预定"
            entity.setStatus("已预定");
        } else {
            // 更新现有订单，保持原状态
            entity.setStatus(currentOrder.getStatus());
        }

        repository.saveOrder(entity, sessionManager.getUsername(), new RepositoryCallback<Long>() {
            @Override
            public void onComplete(Long result) {
                setLoading(false);
                if (result != null && result > 0) {
                    Toast.makeText(OrderEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OrderEditActivity.this, RentalManagementActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(OrderEditActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int getStatusIndex(String status) {
        for (int i = 0; i < STATUS_OPTIONS.length; i++) {
            if (STATUS_OPTIONS[i].equals(status)) {
                return i;
            }
        }
        return 0;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateOrderStatus(RentalOrderEntity order) {
        // 根据订单的日期自动更新状态
        long now = System.currentTimeMillis();
        String newStatus;

        if (now < order.getStartDate()) {
            // 还未开始，状态为"已预定"
            newStatus = "已预定";
        } else if (now >= order.getStartDate() && now <= order.getEndDate()) {
            // 在租赁期间，状态为"进行中"
            newStatus = "进行中";
        } else {
            // 已过结束日期，状态为"已完成"
            newStatus = "已完成";
        }

        // 更新 UI 显示状态
        statusSpinner.setSelection(getStatusIndex(newStatus));
        order.setStatus(newStatus);
    }

    private void processPayment() {
        if (currentOrder == null || orderId <= 0) {
            Toast.makeText(this, "订单不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"已预定".equals(currentOrder.getStatus())) {
            Toast.makeText(this, "只有待支付的订单才能进行支付", Toast.LENGTH_SHORT).show();
            return;
        }

        // 跳转到支付页面
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("orderCode", currentOrder.getOrderCode());
        intent.putExtra("totalAmount", currentOrder.getTotalAmount());
        intent.putExtra("returnDate", FormatUtils.formatDate(currentOrder.getEndDate()));
        startActivity(intent);
    }

    private void processEarlyReturn() {
        if (currentOrder == null || orderId <= 0) {
            Toast.makeText(this, "订单不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"进行中".equals(currentOrder.getStatus())) {
            Toast.makeText(this, "只有进行中的订单才能提前还车", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示确认对话框
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("提前还车确认")
                .setMessage("确认提前还车吗？车辆库存将自动增加 1。")
                .setPositiveButton("确认", (dialog, which) -> {
                    setLoading(true);
                    long actualReturnTime = System.currentTimeMillis();
                    currentOrder.setActualReturnDate(actualReturnTime);
                    currentOrder.setStatus("已完成");

                    // 更新订单状态为已完成
                    repository.updateOrderStatus(orderId, "已完成", sessionManager.getUsername(),
                            new RepositoryCallback<Boolean>() {
                                @Override
                                public void onComplete(Boolean result) {
                                    if (result) {
                                        // 增加车辆库存
                                        repository.increaseCarInventory(currentOrder.getCarId(),
                                                sessionManager.getUsername(),
                                                new RepositoryCallback<Boolean>() {
                                                    @Override
                                                    public void onComplete(Boolean inventoryResult) {
                                                        setLoading(false);
                                                        if (inventoryResult) {
                                                            // 也需要更新订单的 actualReturnDate
                                                            repository.saveOrder(currentOrder,
                                                                    sessionManager.getUsername(),
                                                                    new RepositoryCallback<Long>() {
                                                                        @Override
                                                                        public void onComplete(Long saveResult) {
                                                                            Toast.makeText(OrderEditActivity.this,
                                                                                    "提前还车成功，库存已更新",
                                                                                    Toast.LENGTH_SHORT).show();
                                                                            finish();
                                                                        }
                                                                    });
                                                        } else {
                                                            Toast.makeText(OrderEditActivity.this,
                                                                    "库存更新失败", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        setLoading(false);
                                        Toast.makeText(OrderEditActivity.this, "订单状态更新失败",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
