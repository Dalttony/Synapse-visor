package com.example.reconocimiento.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.example.reconocimiento.data.local.AttendanceEntity;
import com.example.reconocimiento.di.AppContainer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceListViewModel extends AndroidViewModel implements AttendanceListViewModelInterface {

    private final AppContainer appContainer;
    private final ExecutorService executor;
    private final MutableLiveData<List<AttendanceEntity>> attendanceList = new MutableLiveData<>();

    public AttendanceListViewModel(@NonNull Application application) {
        super(application);
        this.appContainer = ((com.example.reconocimiento.Application) application).getAppContainer();
        this.executor = Executors.newSingleThreadExecutor();
        loadAllAttendances();
    }

    public LiveData<List<AttendanceEntity>> getAttendanceList() {
        return attendanceList;
    }

    private void loadAllAttendances() {
        executor.execute(() -> {
            List<AttendanceEntity> list = appContainer.getDatabase().attendanceDao().getAllAttendances();
            attendanceList.postValue(list);
        });
    }

    public void refreshData() {
        loadAllAttendances();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}