package com.example.reconocimiento.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.PrimaryKey;

import com.example.reconocimiento.data.local.OutboxEntity;

public class OutBox {

    @NonNull
    public String id;
    @NonNull public String payloadJson;
    public long createdAt;
    public int attempts;
    @NonNull public String status; // PENDING, SENDING, SENT, FAILED
    @Nullable
    public String lastError;

    public OutBox(@NonNull String id,
                       @NonNull String payloadJson,
                       long createdAt,
                       int attempts
                      ) {
        this.id = id;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
        this.attempts = attempts;
    }


    public int getAttempts() {
        return attempts;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @Nullable
    public String getLastError() {
        return lastError;
    }

    @NonNull
    public String getPayloadJson() {
        return payloadJson;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setLastError(@Nullable String lastError) {
        this.lastError = lastError;
    }

    public void setPayloadJson(@NonNull String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    @NonNull
    @Override
    public String toString() {
        return "OutboxItem{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", retryCount=" + attempts +
                ", timestamp=" + createdAt +
                '}';
    }
}
