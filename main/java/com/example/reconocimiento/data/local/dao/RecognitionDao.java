package com.example.reconocimiento.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.reconocimiento.data.local.RecognitionEntity;
import com.example.reconocimiento.data.local.RecognitionEntity;

import java.util.List;

@Dao
public interface RecognitionDao {

    @Insert
    long insertRecognition(com.example.reconocimiento.data.local.RecognitionEntity recognition);

    @Query("SELECT * FROM recognitions ORDER BY timestamp DESC")
    List<com.example.reconocimiento.data.local.RecognitionEntity> getAllRecognitions();

    @Query("SELECT * FROM recognitions WHERE syncStatus = :status")
    List<com.example.reconocimiento.data.local.RecognitionEntity> getRecognitionsByStatus(String status);

    @Query("SELECT * FROM recognitions WHERE label = :label")
    List<com.example.reconocimiento.data.local.RecognitionEntity> getRecognitionsByLabel(String label);

    @Update
    void updateRecognition(com.example.reconocimiento.data.local.RecognitionEntity recognition);

    @Delete
    void deleteRecognition(com.example.reconocimiento.data.local.RecognitionEntity recognition);

    @Query("SELECT * FROM recognitions WHERE syncStatus = :status ORDER BY timestamp ASC LIMIT :limit")
    List<RecognitionEntity> getBySyncStatus(String status, int limit);
    @Query("DELETE FROM recognitions WHERE id = :id")
    void deleteById(String id);
    @Query("UPDATE recognitions SET syncStatus = :newStatus WHERE id = :id")
    void updateStatus(String id, String newStatus);

    @Query("DELETE FROM recognitions WHERE syncStatus = 'SENT'")
    void deleteSynced();

}
