package com.example.reconocimiento.domain.model;

import androidx.annotation.Nullable;

public class Attendance {

    private int id;
    private String workerName;

    private Long entryDate;
    private Long exitDate;



    // Constructor
    public Attendance(int id, String workerName, Long entryDate,
                             @Nullable Long exitDate) {
        this.id = id;
        this.workerName = workerName;
        this.entryDate = entryDate;
        this.exitDate = exitDate;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public Long getEntryDate() { return entryDate; }
    public void setEntryDate(Long entryDate) { this.entryDate = entryDate; }

    public Long getExitDate() { return exitDate; }
    public void setExitDate(Long exitDate) { this.exitDate = exitDate; }

}
