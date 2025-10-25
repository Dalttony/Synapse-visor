package com.example.reconocimiento.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.reconocimiento.data.local.OutboxEntity;
import com.example.reconocimiento.di.AppContainer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OutBoxListViewModel extends AndroidViewModel implements OutBoxListViewModelInterface {

    private final AppContainer appContainer;
    private final ExecutorService executor;
    private final MutableLiveData<List<OutboxEntity>> outBoxList = new MutableLiveData<>();

    public OutBoxListViewModel(@NonNull Application application) {
        super(application);
        this.appContainer = ((com.example.reconocimiento.Application) application).getAppContainer();
        this.executor = Executors.newSingleThreadExecutor();
        loadAllOutBoxRecords();
    }

    @Override
    public LiveData<List<OutboxEntity>> getOutBoxList() {
        return outBoxList;
    }

    @Override
    public void refreshData() {
        loadAllOutBoxRecords();
    }

    @Override
    public void retryRecord(OutboxEntity outboxEntity) {
        executor.execute(() -> {
            try {
                // Reset status to PENDING to allow retry
                appContainer.getDatabase().outboxDao().updateStatus(outboxEntity.getId(), "PENDING");
                // Refresh the list to show updated status
                loadAllOutBoxRecords();
            } catch (Exception e) {
                // Handle error silently for now
            }
        });
    }

    @Override
    public void deleteRecord(OutboxEntity outboxEntity) {
        executor.execute(() -> {
            try {
                appContainer.getDatabase().outboxDao().deleteById(outboxEntity.getId());
                // Refresh the list to show updated data
                loadAllOutBoxRecords();
            } catch (Exception e) {
                // Handle error silently for now
            }
        });
    }

    private void loadAllOutBoxRecords() {
        executor.execute(() -> {
            List<OutboxEntity> list = appContainer.getDatabase().outboxDao().getAllOutboxRecords();
            outBoxList.postValue(list);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}