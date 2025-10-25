package com.example.reconocimiento.data.repository;


import com.example.reconocimiento.data.local.AttendanceEntity;
import com.example.reconocimiento.data.local.OutboxEntity;
import com.example.reconocimiento.data.local.RecognitionEntity;
import com.example.reconocimiento.domain.model.Attendance;
import com.example.reconocimiento.domain.model.Recognition;

import java.util.List;

public interface OutboxRepository {
        void enqueue(Attendance recognition);           // save as PENDING and trigger worker
        void enqueueEntity(AttendanceEntity entity);    // same but receives an entity
        void kickSender();                               // trigger OutboxSenderWorker

        // worker-facing helpers
        List<OutboxEntity> getPendingBatch(int limit);
        void markSent(String id);
        void markFailed(String id);
        void cleanupSynced();
    }

