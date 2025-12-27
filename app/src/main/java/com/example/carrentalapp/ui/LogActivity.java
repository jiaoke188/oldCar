package com.example.carrentalapp.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.UserRole;
import com.example.carrentalapp.data.entity.AppLogEntity;
import com.example.carrentalapp.ui.adapter.LogAdapter;
import com.example.carrentalapp.util.SessionManager;

import java.util.List;

public class LogActivity extends AppCompatActivity {

    private DataRepository repository;
    private SessionManager sessionManager;
    private LogAdapter adapter;
    private View emptyView;
    private EditText searchInput;
    private Button clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        searchInput = findViewById(R.id.inputSearch);
        Button searchButton = findViewById(R.id.buttonSearch);
        clearButton = findViewById(R.id.buttonClear);

        adapter = new LogAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> loadLogs(searchInput.getText().toString().trim()));
        clearButton.setOnClickListener(v -> clearLogs());

        if (!UserRole.SUPER_ADMIN.equals(sessionManager.getRole())) {
            clearButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLogs(searchInput.getText().toString().trim());
    }

    private void loadLogs(String keyword) {
        repository.searchLogs(keyword, new RepositoryCallback<List<AppLogEntity>>() {
            @Override
            public void onComplete(List<AppLogEntity> result) {
                adapter.submitList(result);
                emptyView.setVisibility(result == null || result.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void clearLogs() {
        repository.clearLogs(sessionManager.getUsername(), new RepositoryCallback<Boolean>() {
            @Override
            public void onComplete(Boolean result) {
                Toast.makeText(LogActivity.this, "日志已清空", Toast.LENGTH_SHORT).show();
                loadLogs("");
            }
        });
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
