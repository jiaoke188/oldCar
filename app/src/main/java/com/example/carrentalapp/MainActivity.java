package com.example.carrentalapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carrentalapp.ui.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 兼容旧入口，直接跳转至登录界面
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}