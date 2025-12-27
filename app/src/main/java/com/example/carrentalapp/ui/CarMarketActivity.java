package com.example.carrentalapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.entity.CarEntity;
import com.example.carrentalapp.ui.adapter.CarAdapter;

import java.util.ArrayList;
import java.util.List;

public class CarMarketActivity extends AppCompatActivity implements CarAdapter.OnItemClickListener {

    public static final String EXTRA_CAR_ID = "extra_car_id";

    private DataRepository repository;
    private CarAdapter adapter;
    private EditText searchInput;
    private View emptyView;
    private List<CarEntity> allCars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_market);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DataRepository.getInstance(this);

        searchInput = findViewById(R.id.inputSearch);
        Button searchButton = findViewById(R.id.buttonSearch);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);

        adapter = new CarAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> applyFilter(searchInput.getText().toString().trim()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCars();
    }

    private void loadCars() {
        repository.loadAllCars(new RepositoryCallback<List<CarEntity>>() {
            @Override
            public void onComplete(List<CarEntity> result) {
                allCars = result == null ? new ArrayList<>() : result;
                applyFilter(searchInput.getText().toString().trim());
            }
        });
    }

    private void applyFilter(String keyword) {
        List<CarEntity> filtered = new ArrayList<>();
        for (CarEntity car : allCars) {
            if (!"可出租".equals(car.getStatus())) {
                continue;
            }
            if (keyword == null || keyword.isEmpty()) {
                filtered.add(car);
            } else {
                boolean match = car.getName().contains(keyword) || (car.getBrand() != null && car.getBrand().contains(keyword));
                if (match) {
                    filtered.add(car);
                }
            }
        }
        adapter.submitList(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(CarEntity entity) {
        Intent intent = new Intent(this, CarDetailActivity.class);
        intent.putExtra(CarDetailActivity.EXTRA_CAR_ID, entity.getId());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(CarEntity entity) {
        // 市场浏览界面忽略长按事件
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
