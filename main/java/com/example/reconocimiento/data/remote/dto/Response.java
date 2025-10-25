package com.example.reconocimiento.data.remote.dto;

public class Response {

    private boolean success;       // true if upload succeeded
    private String message;        // e.g. "Recognition saved successfully"
    private long receivedAt;       // server timestamp (epoch millis or seconds)
    private String status;         // optional: "processed", "queued", "error"

    public Response() {
        // Empty constructor required by Gson
    }

    // --- Getters and setters ---
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }


    public long getReceivedAt() { return receivedAt; }
    public void setReceivedAt(long receivedAt) { this.receivedAt = receivedAt; }



    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "RecognitionResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", receivedAt=" + receivedAt +
                ", status='" + status + '\'' +
                '}';
    }
}