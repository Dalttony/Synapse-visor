package com.example.reconocimiento.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reconocimiento.R;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnReconocer;
    private Button btnVerAsistencias;
    private Button btnOutBox;
    private Button btnModelUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnReconocer = findViewById(R.id.btnReconocer);
        btnVerAsistencias = findViewById(R.id.btnVerAsistencias);
        btnOutBox = findViewById(R.id.btnOutBox);
        btnModelUpdate = findViewById(R.id.btnModelUpdate);
    }

    private void setupClickListeners() {
        btnReconocer.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnVerAsistencias.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, AttendanceListActivity.class);
            startActivity(intent);
        });

        btnOutBox.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, OutBoxListActivity.class);
            startActivity(intent);
        });

        btnModelUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, ModelUpdateActivity.class);
            startActivity(intent);
        });
    }
}