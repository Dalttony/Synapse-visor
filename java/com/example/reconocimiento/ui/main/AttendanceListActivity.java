package com.example.reconocimiento.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconocimiento.R;
import com.example.reconocimiento.data.local.AttendanceEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;

public class AttendanceListActivity extends AppCompatActivity {

    private AttendanceListViewModel viewModel;
    private RecyclerView recyclerView;
    private AttendanceAdapter adapter;
    private TextView tvEmptyMessage;
    private TextView tvTotalCount;
    private TextView tvActiveCount;
    private FloatingActionButton fabRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_list);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvActiveCount = findViewById(R.id.tvActiveCount);
        fabRefresh = findViewById(R.id.fabRefresh);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Control de Asistencia");
        }
    }

    private void setupRecyclerView() {
        adapter = new AttendanceAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AttendanceListViewModel.class);

        viewModel.getAttendanceList().observe(this, attendanceList -> {
            if (attendanceList != null && !attendanceList.isEmpty()) {
                adapter.setAttendanceList(attendanceList);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmptyMessage.setVisibility(View.GONE);
                updateStatistics(attendanceList);
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

    private void updateStatistics(List<AttendanceEntity> attendanceList) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            tvTotalCount.setText("0\nRegistros Totales");
            tvActiveCount.setText("0\nActivos Hoy");
            return;
        }

        int totalRecords = attendanceList.size();
        int activeToday = countActiveToday(attendanceList);

        tvTotalCount.setText(totalRecords + "\nRegistros Totales");
        tvActiveCount.setText(activeToday + "\nActivos Hoy");
    }

    private int countActiveToday(List<AttendanceEntity> attendanceList) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayStart = today.getTimeInMillis();

        today.add(Calendar.DAY_OF_MONTH, 1);
        long tomorrowStart = today.getTimeInMillis();

        int count = 0;
        for (AttendanceEntity attendance : attendanceList) {
            if (attendance.getEntryDate() != null &&
                attendance.getEntryDate() >= todayStart &&
                attendance.getEntryDate() < tomorrowStart &&
                attendance.getExitDate() == null) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}