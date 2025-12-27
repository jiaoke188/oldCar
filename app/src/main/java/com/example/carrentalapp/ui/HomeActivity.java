package com.example.carrentalapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.UserRole;
import com.example.carrentalapp.util.SessionManager;

public class HomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);
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
        String welcome = getString(R.string.home_welcome_message, display, sessionManager.getUsername(), com.example.carrentalapp.data.UserRole.toDisplay(sessionManager.getRole()));
        welcomeText.setText(welcome);

        Button userManageButton = findViewById(R.id.buttonUserManage);
        Button carManageButton = findViewById(R.id.buttonCarManage);
        Button rentalManageButton = findViewById(R.id.buttonRentalManage);
        Button marketButton = findViewById(R.id.buttonMarket);
        Button favoriteButton = findViewById(R.id.buttonFavorite);
        Button logButton = findViewById(R.id.buttonLog);

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

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
