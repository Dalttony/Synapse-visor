// app/src/main/java/com/example/photox/data/remote/dto/RecognitionRequest.java
package com.example.reconocimiento.data.remote.dto;

public class RecognitionRequest {

    private String id;
    private String label;
    private float confidence;
    private String photoPath;
    private long timestamp;

    public RecognitionRequest(String id, String label, float confidence, String photoPath, long timestamp) {
        this.id = id;
        this.label = label;
        this.confidence = confidence;
        this.photoPath = photoPath;
        this.timestamp = timestamp;
    }

    // --- Getters and setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}