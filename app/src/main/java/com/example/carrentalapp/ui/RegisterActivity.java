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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.UserRole;
import com.example.carrentalapp.data.entity.UserEntity;

public class RegisterActivity extends AppCompatActivity {

    private Spinner roleSpinner;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText displayNameInput;
    private EditText phoneInput;
    private EditText emailInput;
    private ProgressBar progressBar;
    private Button submitButton;

    private DataRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DataRepository.getInstance(this);

        roleSpinner = findViewById(R.id.spinnerRole);
        usernameInput = findViewById(R.id.inputUsername);
        passwordInput = findViewById(R.id.inputPassword);
        displayNameInput = findViewById(R.id.inputDisplayName);
        phoneInput = findViewById(R.id.inputPhone);
        emailInput = findViewById(R.id.inputEmail);
        progressBar = findViewById(R.id.registerProgress);
        submitButton = findViewById(R.id.buttonSubmit);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this, R.array.user_role_entries, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // 提交注册信息
        submitButton.setOnClickListener(v -> attemptRegister());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptRegister() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String displayName = displayNameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);

        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setDisplayName(displayName);
        entity.setPhone(phone);
        entity.setEmail(email);
        entity.setRole(mapRole(roleSpinner.getSelectedItemPosition()));

        repository.registerUser(entity, new RepositoryCallback<Long>() {
            @Override
            public void onComplete(Long result) {
                setLoading(false);
                if (result != null && result > 0) {
                    Toast.makeText(RegisterActivity.this, "注册成功，请返回登录", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "账号已存在或注册失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!loading);
    }

    private String mapRole(int position) {
        switch (position) {
            case 0:
                return UserRole.CUSTOMER;
            case 1:
                return UserRole.MERCHANT;
            case 2:
                return UserRole.SUPER_ADMIN;
            default:
                return UserRole.CUSTOMER;
        }
    }
}
