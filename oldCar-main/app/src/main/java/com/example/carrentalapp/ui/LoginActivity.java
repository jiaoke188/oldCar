package com.example.carrentalapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.entity.UserEntity;
import com.example.carrentalapp.util.FormatUtils;
import com.example.carrentalapp.util.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private ProgressBar progressBar;
    private Button loginButton;

    private SessionManager sessionManager;
    private DataRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        repository = DataRepository.getInstance(this);

        if (sessionManager.isLoggedIn()) {
            openHome();
            finish();
            return;
        }

        usernameInput = findViewById(R.id.inputUsername);
        passwordInput = findViewById(R.id.inputPassword);
        progressBar = findViewById(R.id.loginProgress);
        loginButton = findViewById(R.id.buttonLogin);
        Button registerButton = findViewById(R.id.buttonRegister);

        // 登录按钮点击逻辑
        loginButton.setOnClickListener(v -> attemptLogin());
        // 跳转注册界面
        registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        repository.login(username, password, new RepositoryCallback<UserEntity>() {
            @Override
            public void onComplete(UserEntity result) {
                setLoading(false);
                if (result == null) {
                    Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                } else {
                    sessionManager.saveUser(result.getId(), result.getUsername(), result.getRole(), FormatUtils.safe(result.getDisplayName()));
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    openHome();
                    finish();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
    }

    private void openHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
