package com.example.reconocimiento.domain.model;

import org.jspecify.annotations.NonNull;

public class Recognition {

    @NonNull
    private final String id;          // unique UUID (used as idempotency key)
    @NonNull
    private final String label;       // e.g. "person", "cat", "car"
    private final float confidence;   // model confidence [0.0–1.0]
    @NonNull
    private final String photoPath;   // local image path
    private final long timestamp;     // epoch millis of recognition time

    public Recognition(@NonNull String id,
                       @NonNull String label,
                       float confidence,
                       @NonNull String photoPath,
                       long timestamp) {
        this.id = id;
        this.label = label;
        this.confidence = confidence;
        this.photoPath = photoPath;
        this.timestamp = timestamp;
    }

    // --- Getters ---
    @NonNull public String getId() { return id; }
    @NonNull public String getLabel() { return label; }
    public float getConfidence() { return confidence; }
    @NonNull public String getPhotoPath() { return photoPath; }
    public long getTimestamp() { return timestamp; }

    // --- Optional helper ---
    public boolean isConfidentEnough(float threshold) {
        return confidence >= threshold;
    }

    @Override
    public String toString() {
        return "Recognition{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", confidence=" + confidence +
                ", photoPath='" + photoPath + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
