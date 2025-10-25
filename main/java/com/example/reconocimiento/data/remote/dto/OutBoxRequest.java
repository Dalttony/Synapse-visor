package com.example.reconocimiento.data.remote.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.PrimaryKey;

import com.example.reconocimiento.data.local.OutboxEntity;

public class OutBoxRequest {


    public String id;
    public String payloadJson;
    public long createdAt;
    public int attempts;
    public String status; // PENDING, SENDING, SENT, FAILED

    public String lastError;

    public OutBoxRequest(String id,
                 String payloadJson,
                  long createdAt,
                  int attempts
                  ) {
        this.id = id;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
        this.attempts = attempts;
    }

    public String getPayloadJson() {
        return payloadJson;
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
