package com.example.reconocimiento.data.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "outbox")
public class OutboxEntity {
    @PrimaryKey
    @NonNull
    public String id;
    @NonNull public String payloadJson;
    public long createdAt;
    public int attempts;
    @NonNull public String status; // PENDING, SENDING, SENT, FAILED
    @Nullable
    public String lastError;

    public static OutboxEntity newPending(String json) {
        OutboxEntity e = new OutboxEntity();
        e.id =  UUID.randomUUID().toString();
        e.payloadJson = json;
        e.createdAt = System.currentTimeMillis();
        e.attempts = 0;
        e.status = "PENDING";
        return e;
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
