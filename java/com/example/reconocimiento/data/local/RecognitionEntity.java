package com.example.reconocimiento.data.local;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad que representa un reconocimiento facial almacenado localmente.
 * Guarda resultados de predicciones con su etiqueta, nivel de confianza y estado de sincronización.
 */
@Entity(
        tableName = "recognitions",
        indices = {
                @Index(value = {"timestamp"}),
                @Index(value = {"label"}),
                @Index(value = {"syncStatus"})
        }
)
public class RecognitionEntity {

    @PrimaryKey
    @NonNull
    private String id; // UUID único para cada reconocimiento

    @NonNull
    private String label; // Clase o nombre detectado

    private float confidence; // Nivel de confianza (0 a 1)

    @NonNull
    private String photoPath; // Ruta local de la imagen capturada

    private long timestamp; // Tiempo en milisegundos (System.currentTimeMillis())

    @NonNull
    private String syncStatus; // Estado: "PENDING", "SENT", "FAILED"

    // Constructor
    public RecognitionEntity(@NonNull String id,
                             @NonNull String label,
                             float confidence,
                             @NonNull String photoPath,
                             long timestamp,
                             @NonNull String syncStatus) {
        this.id = id;
        this.label = label;
        this.confidence = confidence;
        this.photoPath = photoPath;
        this.timestamp = timestamp;
        this.syncStatus = syncStatus;
    }

    // Getters y Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getLabel() { return label; }
    public void setLabel(@NonNull String label) { this.label = label; }

    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }

    @NonNull
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(@NonNull String photoPath) { this.photoPath = photoPath; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @NonNull
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(@NonNull String syncStatus) { this.syncStatus = syncStatus; }
}
