package com.example.carrentalapp.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.entity.CarEntity;
import com.example.carrentalapp.util.SessionManager;

public class CarEditActivity extends AppCompatActivity {

    public static final String EXTRA_CAR_ID = "extra_car_id";

    private DataRepository repository;
    private SessionManager sessionManager;

    private EditText nameInput;
    private EditText brandInput;
    private EditText categoryInput;
    private EditText priceInput;
    private EditText inventoryInput;
    private EditText descriptionInput;
    private Spinner statusSpinner;
    private ProgressBar progressBar;
    private Button saveButton;

    private long carId;
    private CarEntity currentCar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);

        nameInput = findViewById(R.id.inputName);
        brandInput = findViewById(R.id.inputBrand);
        categoryInput = findViewById(R.id.inputCategory);
        priceInput = findViewById(R.id.inputPrice);
        inventoryInput = findViewById(R.id.inputInventory);
        descriptionInput = findViewById(R.id.inputDescription);
        statusSpinner = findViewById(R.id.spinnerStatus);
        progressBar = findViewById(R.id.progressBar);
        saveButton = findViewById(R.id.buttonSave);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.car_status_entries, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        carId = getIntent().getLongExtra(EXTRA_CAR_ID, 0);
        if (carId > 0) {
            loadCar();
        }

        saveButton.setOnClickListener(v -> saveCar());
    }

    private void loadCar() {
        setLoading(true);
        repository.loadCarById(carId, new RepositoryCallback<CarEntity>() {
            @Override
            public void onComplete(CarEntity result) {
                setLoading(false);
                if (result == null) {
                    Toast.makeText(CarEditActivity.this, "车辆不存在", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentCar = result;
                nameInput.setText(result.getName());
                brandInput.setText(result.getBrand());
                categoryInput.setText(result.getCategory());
                priceInput.setText(String.valueOf(result.getDailyPrice()));
                inventoryInput.setText(String.valueOf(result.getInventory()));
                descriptionInput.setText(result.getDescription());
                statusSpinner.setSelection(getStatusIndex(result.getStatus()));
            }
        });
    }

    private void saveCar() {
        String name = nameInput.getText().toString().trim();
        String brand = brandInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String inventoryStr = inventoryInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || inventoryStr.isEmpty()) {
            Toast.makeText(this, "请完善必填信息", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int inventory;
        try {
            price = Double.parseDouble(priceStr);
            inventory = Integer.parseInt(inventoryStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请填写正确的价格与库存", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        CarEntity entity = currentCar != null ? currentCar : new CarEntity();
        entity.setId(carId);
        entity.setName(name);
        entity.setBrand(brand);
        entity.setCategory(category);
        entity.setDailyPrice(price);
        entity.setInventory(inventory);
        entity.setDescription(description);
        entity.setStatus(getStatusValue(statusSpinner.getSelectedItemPosition()));
        if (entity.getOwnerId() == null) {
            entity.setOwnerId(sessionManager.getUserId());
        }

        repository.saveCar(entity, sessionManager.getUsername(), new RepositoryCallback<Long>() {
            @Override
            public void onComplete(Long result) {
                setLoading(false);
                if (result != null && result > 0) {
                    Toast.makeText(CarEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CarEditActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int getStatusIndex(String status) {
        if ("维护中".equals(status)) {
            return 1;
        }
        if ("已下线".equals(status)) {
            return 2;
        }
        return 0;
    }

    private String getStatusValue(int index) {
        switch (index) {
            case 1:
                return "维护中";
            case 2:
                return "已下线";
            default:
                return "可出租";
        }
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
}
