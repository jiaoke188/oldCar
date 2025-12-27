package com.example.carrentalapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.entity.CarEntity;
import com.example.carrentalapp.data.model.CarCommentWithUser;
import com.example.carrentalapp.ui.adapter.CommentAdapter;
import com.example.carrentalapp.util.FormatUtils;
import com.example.carrentalapp.util.SessionManager;

import java.util.List;

public class CarDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CAR_ID = "extra_car_id";

    private DataRepository repository;
    private SessionManager sessionManager;
    private CommentAdapter commentAdapter;

    private long carId;
    private CarEntity currentCar;
    private boolean isFavorite;

    private TextView nameView;
    private TextView metaView;
    private TextView statusView;
    private TextView descriptionView;
    private Button favoriteButton;
    private ProgressBar progressBar;
    private EditText commentInput;
    private Button commentButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);

        carId = getIntent().getLongExtra(EXTRA_CAR_ID, 0);
        if (carId <= 0) {
            Toast.makeText(this, "车辆信息有误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nameView = findViewById(R.id.textName);
        metaView = findViewById(R.id.textMeta);
        statusView = findViewById(R.id.textStatus);
        descriptionView = findViewById(R.id.textDescription);
        favoriteButton = findViewById(R.id.buttonFavorite);
        progressBar = findViewById(R.id.progressBar);
        commentInput = findViewById(R.id.inputComment);
        commentButton = findViewById(R.id.buttonComment);
        // 管理端同样支持从详情页进入下单流程，保证功能入口一致
        Button orderButton = findViewById(R.id.buttonOrder);

        RecyclerView commentList = findViewById(R.id.recyclerComments);
        commentAdapter = new CommentAdapter();
        commentList.setLayoutManager(new LinearLayoutManager(this));
        commentList.setAdapter(commentAdapter);

        favoriteButton.setOnClickListener(v -> toggleFavorite());
        commentButton.setOnClickListener(v -> submitComment());
        orderButton.setOnClickListener(v -> openOrderEditor());

        loadData();
    }

    private void loadData() {
        setLoading(true);
        repository.loadCarById(carId, new RepositoryCallback<CarEntity>() {
            @Override
            public void onComplete(CarEntity result) {
                setLoading(false);
                if (result == null) {
                    Toast.makeText(CarDetailActivity.this, "车辆不存在", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentCar = result;
                bindCar(result);
                loadComments();
                if (sessionManager.isLoggedIn()) {
                    repository.isFavorite(sessionManager.getUserId(), carId, new RepositoryCallback<Boolean>() {
                        @Override
                        public void onComplete(Boolean result) {
                            isFavorite = Boolean.TRUE.equals(result);
                            refreshFavoriteButton();
                        }
                    });
                }
            }
        });
    }

    private void bindCar(CarEntity car) {
        nameView.setText(car.getName());
        String meta = getString(R.string.car_detail_meta_template, FormatUtils.safe(car.getBrand()), FormatUtils.safe(car.getCategory()), car.getDailyPrice(), car.getInventory());
        metaView.setText(meta);
        statusView.setText(car.getStatus());
        descriptionView.setText(FormatUtils.safe(car.getDescription()));
    }

    private void loadComments() {
        repository.loadCommentsByCar(carId, new RepositoryCallback<List<CarCommentWithUser>>() {
            @Override
            public void onComplete(List<CarCommentWithUser> result) {
                commentAdapter.submitList(result);
            }
        });
    }

    private void toggleFavorite() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        repository.toggleFavorite(sessionManager.getUserId(), carId, sessionManager.getUsername(), new RepositoryCallback<Boolean>() {
            @Override
            public void onComplete(Boolean result) {
                isFavorite = Boolean.TRUE.equals(result);
                refreshFavoriteButton();
                Toast.makeText(CarDetailActivity.this, isFavorite ? "已加入收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshFavoriteButton() {
        favoriteButton.setText(isFavorite ? R.string.action_favorite_added : R.string.action_favorite_add);
    }

    private void submitComment() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        String content = commentInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            return;
        }
        repository.addComment(carId, sessionManager.getUserId(), content, sessionManager.getUsername(), new RepositoryCallback<Long>() {
            @Override
            public void onComplete(Long result) {
                commentInput.setText("");
                loadComments();
                Toast.makeText(CarDetailActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openOrderEditor() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, OrderEditActivity.class);
        intent.putExtra(OrderEditActivity.EXTRA_CAR_ID, carId);
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
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
