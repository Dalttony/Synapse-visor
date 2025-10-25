package com.example.reconocimiento.data.local.dao;

import android.util.Log;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.reconocimiento.data.local.AttendanceEntity;

import java.util.List;

@Dao
public interface AttendanceDao {

    @Insert
    long insertAttendance(AttendanceEntity attendance);

    @Query("SELECT * FROM attendance")
    List<AttendanceEntity> getAllAttendances();

    @Query("SELECT * FROM attendance WHERE worker_name = :name")
    List<AttendanceEntity> getAttendanceByName(String name);

    @Update
    void updateAttendance(AttendanceEntity attendance);
    // The “open” attendance (no exit yet) for this worker, latest first
    @Query("SELECT * FROM attendance " +
            "WHERE worker_name = :name " +
            "ORDER BY id DESC LIMIT 1")
    AttendanceEntity findOpenForWorker(String name);

    @Query("UPDATE attendance SET exit_date = :exitDate WHERE id = :id")
    void setExitForId(int id, Long exitDate);

    @Delete
    void deleteAttendance(AttendanceEntity attendance);
}
