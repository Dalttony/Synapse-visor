package com.example.reconocimiento.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.reconocimiento.R;
import com.example.reconocimiento.data.remote.dto.ModelResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ModelUpdateActivity extends AppCompatActivity {

    private ModelUpdateViewModel viewModel;

    // UI Components
    private TextView tvCurrentVersion;
    private TextView tvLatestVersion;
    private TextView tvReleaseDate;
    private TextView tvStatus;
    private TextView tvProgress;
    private LinearLayout progressSection;
    private ProgressBar progressBar;
    private Button btnCheckUpdate;
    private Button btnDownload;

    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_update);

        initViews();
        setupToolbar();
        setupViewModel();
        setupClickListeners();
    }

    private void initViews() {
        tvCurrentVersion = findViewById(R.id.tvCurrentVersion);
        tvLatestVersion = findViewById(R.id.tvLatestVersion);
        tvReleaseDate = findViewById(R.id.tvReleaseDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvProgress = findViewById(R.id.tvProgress);
        progressSection = findViewById(R.id.progressSection);
        progressBar = findViewById(R.id.progressBar);
        btnCheckUpdate = findViewById(R.id.btnCheckUpdate);
        btnDownload = findViewById(R.id.btnDownload);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Actualización del Modelo");
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ModelUpdateViewModel.class);

        // Set current version
        String currentVersion = getSharedPreferences("model_prefs", 0)
                .getString("current_version", "1.0.0");
        tvCurrentVersion.setText("v" + currentVersion);

        // Observe latest model info
        viewModel.getLatestModel().observe(this, this::updateModelInfo);

        // Observe status messages
        viewModel.getStatusMessage().observe(this, status -> {
            if (status != null) {
                tvStatus.setText(status);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                btnCheckUpdate.setEnabled(!isLoading);
                if (isLoading) {
                    btnCheckUpdate.setText("Verificando...");
                } else {
                    btnCheckUpdate.setText("Verificar");
                }
            }
        });

        // Observe download progress
        viewModel.getDownloadProgress().observe(this, progress -> {
            if (progress != null) {
                progressBar.setProgress(progress);
                tvProgress.setText(progress + "%");

                if (progress > 0) {
                    progressSection.setVisibility(View.VISIBLE);
                }

                if (progress >= 100) {
                    progressSection.setVisibility(View.GONE);
                    // Refresh current version display
                    viewModel.getLatestModel().getValue();
                    if (viewModel.getLatestModel().getValue() != null) {
                        tvCurrentVersion.setText("v" + viewModel.getLatestModel().getValue().getVersion());
                    }
                }
            }
        });

        // Observe download button state
        viewModel.getDownloadEnabled().observe(this, enabled -> {
            if (enabled != null) {
                btnDownload.setEnabled(enabled);
                if (enabled) {
                    btnDownload.setText("Descargar");
                } else {
                    btnDownload.setText("Descargando...");
                }
            }
        });
    }

    private void setupClickListeners() {
        btnCheckUpdate.setOnClickListener(v -> viewModel.checkForUpdates());

        btnDownload.setOnClickListener(v -> {
            viewModel.downloadModel();
            progressSection.setVisibility(View.VISIBLE);
        });
    }

    private void updateModelInfo(ModelResponse model) {
        if (model != null) {
            tvLatestVersion.setText("v" + model.getVersion());

            // Format and display release date
            try {
                Date date = inputDateFormat.parse(model.getFecha());
                if (date != null) {
                    tvReleaseDate.setText(outputDateFormat.format(date));
                }
            } catch (ParseException e) {
                // If parsing fails, show the raw date
                tvReleaseDate.setText(model.getFecha().substring(0, 10)); // Just the date part
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}