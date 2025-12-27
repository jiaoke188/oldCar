package com.example.carrentalapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.model.FavoriteWithCar;
import com.example.carrentalapp.ui.adapter.FavoriteAdapter;
import com.example.carrentalapp.util.SessionManager;

import java.util.List;

public class FavoriteActivity extends AppCompatActivity implements FavoriteAdapter.OnFavoriteActionListener {

    private DataRepository repository;
    private SessionManager sessionManager;
    private FavoriteAdapter adapter;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        adapter = new FavoriteAdapter();
        adapter.setListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        repository.loadFavorites(sessionManager.getUserId(), new RepositoryCallback<List<FavoriteWithCar>>() {
            @Override
            public void onComplete(List<FavoriteWithCar> result) {
                adapter.submitList(result);
                emptyView.setVisibility(result == null || result.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onRemove(FavoriteWithCar favorite) {
        repository.toggleFavorite(sessionManager.getUserId(), favorite.getCarId(), sessionManager.getUsername(), new RepositoryCallback<Boolean>() {
            @Override
            public void onComplete(Boolean result) {
                Toast.makeText(FavoriteActivity.this, "已取消收藏", Toast.LENGTH_SHORT).show();
                loadFavorites();
            }
        });
    }

    @Override
    public void onOpen(FavoriteWithCar favorite) {
        Intent intent = new Intent(this, CarDetailActivity.class);
        intent.putExtra(CarDetailActivity.EXTRA_CAR_ID, favorite.getCarId());
        startActivity(intent);
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
