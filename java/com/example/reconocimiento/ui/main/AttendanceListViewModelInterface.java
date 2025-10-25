package com.example.reconocimiento.ui.main;

import androidx.lifecycle.LiveData;

import com.example.reconocimiento.data.local.AttendanceEntity;

import java.util.List;

public interface AttendanceListViewModelInterface {

    /**
     * Gets the observable list of all attendance records
     * @return LiveData containing list of AttendanceEntity
     */
    LiveData<List<AttendanceEntity>> getAttendanceList();

    /**
     * Refreshes the attendance data from the database
     */
    void refreshData();
}