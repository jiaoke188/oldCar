package com.example.carrentalapp.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.UserRole;
import com.example.carrentalapp.data.entity.UserEntity;
import com.example.carrentalapp.util.SessionManager;

public class UserEditActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "extra_user_id";

    private DataRepository repository;
    private SessionManager sessionManager;

    private EditText usernameInput;
    private EditText passwordInput;
    private EditText displayNameInput;
    private EditText phoneInput;
    private EditText emailInput;
    private Spinner roleSpinner;
    private Switch activeSwitch;
    private ProgressBar progressBar;
    private Button saveButton;

    private long userId;
    private UserEntity currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);

        usernameInput = findViewById(R.id.inputUsername);
        passwordInput = findViewById(R.id.inputPassword);
        displayNameInput = findViewById(R.id.inputDisplayName);
        phoneInput = findViewById(R.id.inputPhone);
        emailInput = findViewById(R.id.inputEmail);
        roleSpinner = findViewById(R.id.spinnerRole);
        activeSwitch = findViewById(R.id.switchActive);
        progressBar = findViewById(R.id.progressBar);
        saveButton = findViewById(R.id.buttonSave);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this, R.array.user_role_entries, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        userId = getIntent().getLongExtra(EXTRA_USER_ID, 0);

        if (userId > 0) {
            loadUser();
        } else {
            activeSwitch.setChecked(true);
        }

        saveButton.setOnClickListener(v -> saveUser());
    }

    private void loadUser() {
        setLoading(true);
        repository.loadUserById(userId, new RepositoryCallback<UserEntity>() {
            @Override
            public void onComplete(UserEntity result) {
                setLoading(false);
                if (result == null) {
                    Toast.makeText(UserEditActivity.this, "未找到用户", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                usernameInput.setText(result.getUsername());
                usernameInput.setEnabled(false);
                displayNameInput.setText(result.getDisplayName());
                phoneInput.setText(result.getPhone());
                emailInput.setText(result.getEmail());
                activeSwitch.setChecked(result.isActive());
                roleSpinner.setSelection(getRoleSelection(result.getRole()));
                currentUser = result;
            }
        });
    }

    private void saveUser() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String displayName = displayNameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId == 0 && password.isEmpty()) {
            Toast.makeText(this, "请设置密码", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        UserEntity entity = currentUser != null ? currentUser : new UserEntity();
        entity.setId(userId);
        entity.setUsername(username);
        if (!password.isEmpty()) {
            entity.setPassword(password);
        } else if (currentUser != null) {
            entity.setPassword(currentUser.getPassword());
        }
        entity.setDisplayName(displayName);
        entity.setPhone(phone);
        entity.setEmail(email);
        entity.setRole(mapRole(roleSpinner.getSelectedItemPosition()));
        entity.setActive(activeSwitch.isChecked());

        repository.saveUser(entity, sessionManager.getUsername(), new RepositoryCallback<Long>() {
            @Override
            public void onComplete(Long result) {
                setLoading(false);
                if (result != null && result > 0) {
                    Toast.makeText(UserEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UserEditActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading);
    }

    private int getRoleSelection(String role) {
        if (UserRole.MERCHANT.equals(role)) {
            return 1;
        }
        if (UserRole.SUPER_ADMIN.equals(role)) {
            return 2;
        }
        return 0;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
