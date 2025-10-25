package com.example.reconocimiento.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reconocimiento.data.local.OutboxEntity;

import java.util.List;
@Dao
public interface OutboxDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(OutboxEntity e);

    @Query("SELECT * FROM outbox WHERE status IN ('PENDING','FAILED') ORDER BY createdAt ASC LIMIT :limit")
    List<OutboxEntity> nextBatch(int limit);

    @Update
    void update(OutboxEntity... e);

    /** Update the status (PENDING, SENT, FAILED, etc.) for a specific item. */
    @Query("UPDATE outbox SET status = :newStatus, attempts = attempts + 1 WHERE id = :id")
    void updateStatus(String id, String newStatus);


    /** Delete a specific item by ID. */
    @Query("DELETE FROM outbox WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM outbox WHERE status='SENT'")
    void deleteSent();
}
