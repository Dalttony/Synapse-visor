package com.example.reconocimiento.ui.main;

import androidx.lifecycle.LiveData;

import com.example.reconocimiento.data.local.OutboxEntity;

import java.util.List;

public interface OutBoxListViewModelInterface {

    /**
     * Gets the observable list of all outbox records
     * @return LiveData containing list of OutboxEntity
     */
    LiveData<List<OutboxEntity>> getOutBoxList();

    /**
     * Refreshes the outbox data from the database
     */
    void refreshData();

    /**
     * Retries sending a failed outbox record
     * @param outboxEntity The record to retry
     */
    void retryRecord(OutboxEntity outboxEntity);

    /**
     * Deletes a specific outbox record
     * @param outboxEntity The record to delete
     */
    void deleteRecord(OutboxEntity outboxEntity);
}