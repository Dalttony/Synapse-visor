package com.example.reconocimiento.data.repository;

import com.example.reconocimiento.data.local.AttendanceEntity;
import com.example.reconocimiento.data.local.dao.AttendanceDao;

public class AttendanceRepository {

    private final AttendanceDao dao;

    public AttendanceRepository(AttendanceDao dao) {
        this.dao = dao;
    }

    /**
     * Called when a face/photo is recognized as `workerName`.
     * If there is an open record (no exit) → set exit now.
     * Else → create a new entry (entry now, exit null).
     */
    public AttendanceEntity onRecognition(String workerName, Long dateStr, Long exitDate) {
        AttendanceEntity open = dao.findOpenForWorker(workerName);
        if (open == null) {
            // Create entry
            AttendanceEntity a = new AttendanceEntity(workerName, dateStr, null);
            dao.insertAttendance(a);
        } else {
            // Close existing entry
            dao.setExitForId(open.getId(), exitDate);
        }
        open = dao.findOpenForWorker(workerName);
        return  open;
    }
}
