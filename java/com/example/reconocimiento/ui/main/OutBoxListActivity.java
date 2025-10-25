package com.example.reconocimiento.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconocimiento.R;
import com.example.reconocimiento.data.local.OutboxEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class OutBoxListActivity extends AppCompatActivity implements OutBoxAdapter.OnOutBoxActionListener {

    private OutBoxListViewModel viewModel;
    private RecyclerView recyclerView;
    private OutBoxAdapter adapter;
    private TextView tvEmptyMessage;
    private TextView tvTotalCount;
    private TextView tvPendingCount;
    private TextView tvFailedCount;
    private FloatingActionButton fabRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outbox_list);
        try {
            initViews();
            setupToolbar();
            setupRecyclerView();
            setupViewModel();
            setupClickListeners();
            Log.e("MAINACTIVITY_", "Cargado");
        }catch (Exception e){
            Log.e("MAINACTIVITY_", e.getMessage());
        }

    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvFailedCount = findViewById(R.id.tvFailedCount);
        fabRefresh = findViewById(R.id.fabRefresh);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cola de Sincronización");
        }
    }

    private void setupRecyclerView() {
        adapter = new OutBoxAdapter();
        adapter.setOnOutBoxActionListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(OutBoxListViewModel.class);

        viewModel.getOutBoxList().observe(this, outBoxList -> {
            if (outBoxList != null && !outBoxList.isEmpty()) {
                adapter.setOutBoxList(outBoxList);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmptyMessage.setVisibility(View.GONE);
                updateStatistics(outBoxList);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvEmptyMessage.setVisibility(View.VISIBLE);
                updateStatistics(null);
            }
        });
    }

    private void setupClickListeners() {
        fabRefresh.setOnClickListener(v -> viewModel.refreshData());
    }

    private void updateStatistics(List<OutboxEntity> outBoxList) {
        if (outBoxList == null || outBoxList.isEmpty()) {
            tvTotalCount.setText("0\nTotal Registros");
            tvPendingCount.setText("0\nPendientes");
            tvFailedCount.setText("0\nFallidos");
            return;
        }

        int totalRecords = outBoxList.size();
        int pendingCount = 0;
        int failedCount = 0;

        for (OutboxEntity record : outBoxList) {
            switch (record.getStatus()) {
                case "PENDING":
                case "SENDING":
                    pendingCount++;
                    break;
                case "FAILED":
                    failedCount++;
                    break;
            }
        }

        tvTotalCount.setText(totalRecords + "\nTotal Registros");
        tvPendingCount.setText(pendingCount + "\nPendientes");
        tvFailedCount.setText(failedCount + "\nFallidos");
    }

    @Override
    public void onRetryRecord(OutboxEntity record) {
        new AlertDialog.Builder(this)
            .setTitle("Reintentar Envío")
            .setMessage("¿Deseas reintentar el envío de este registro?")
            .setPositiveButton("Sí", (dialog, which) -> {
                viewModel.retryRecord(record);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    @Override
    public void onDeleteRecord(OutboxEntity record) {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar Registro")
            .setMessage("¿Estás seguro de que deseas eliminar este registro? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                viewModel.deleteRecord(record);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}