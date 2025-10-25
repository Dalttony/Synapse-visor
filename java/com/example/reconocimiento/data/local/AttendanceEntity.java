package com.example.reconocimiento.data.local;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "attendance",
        indices = { @Index(value = {"worker_name", "entry_date"}) }

)
public class AttendanceEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "worker_name")
    private String workerName;

    @ColumnInfo(name = "entry_date")
    private Long entryDate;  // Ejemplo: "2025-10-24"

    @ColumnInfo(name = "exit_date")
    private Long exitDate;   // Ejemplo: "2025-10-24"



    // Constructor
    public AttendanceEntity(String workerName, Long entryDate,
                     @Nullable  Long exitDate) {
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
