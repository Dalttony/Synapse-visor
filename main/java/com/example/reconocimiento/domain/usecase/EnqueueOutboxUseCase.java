package com.example.reconocimiento.domain.usecase;

import androidx.annotation.NonNull;

import com.example.reconocimiento.data.local.AttendanceEntity;
import com.example.reconocimiento.data.local.RecognitionEntity;
import com.example.reconocimiento.data.repository.OutboxRepository;
import com.example.reconocimiento.domain.model.Attendance;
import com.example.reconocimiento.domain.model.Recognition;

public class EnqueueOutboxUseCase {
    private final OutboxRepository outboxRepository;

    public EnqueueOutboxUseCase(@NonNull OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }


    public void run(@NonNull Attendance attendance) {
        // Repository will map to entity, set syncStatus=PENDING, insert, and trigger worker.
        outboxRepository.enqueue(attendance);
    }


    public void run(@NonNull AttendanceEntity entity) {
        outboxRepository.enqueueEntity(entity); // ensures PENDING + kicks worker
    }
    public void kickSender() {
        outboxRepository.kickSender();
    }
}
