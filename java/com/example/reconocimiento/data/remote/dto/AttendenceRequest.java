package com.example.reconocimiento.data.remote.dto;

import androidx.annotation.Nullable;

public class AttendenceRequest {

    private int id;
    private String workerName;

    private Long entryDate;  // Ejemplo: "2025-10-24"
    private Long exitDate;   // Ejemplo: "2025-10-24"



    // Constructor
    public AttendenceRequest(String workerName, Long entryDate,
                            @Nullable Long exitDate) {
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
