package com.example.reconocimiento.data.repository.impl;


import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.reconocimiento.data.local.AppDatabase;
import com.example.reconocimiento.data.local.AttendanceEntity;
import com.example.reconocimiento.data.local.OutboxEntity;
import com.example.reconocimiento.data.local.RecognitionEntity;
import com.example.reconocimiento.data.local.dao.AttendanceDao;
import com.example.reconocimiento.data.local.dao.OutboxDao;
import com.example.reconocimiento.data.local.dao.RecognitionDao;
import com.example.reconocimiento.data.mapper.Mapper;
import com.example.reconocimiento.data.mapper.RecognitionMapper;
import com.example.reconocimiento.data.repository.OutboxRepository;
import com.example.reconocimiento.domain.model.Attendance;
import com.example.reconocimiento.domain.model.OutBox;
import com.example.reconocimiento.domain.model.Recognition;
import com.example.reconocimiento.workers.OutboxSenderWorker;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class OutboxRepositoryImpl implements OutboxRepository {

        private final Context appContext;
        private final OutboxDao dao;
         private final AttendanceDao attendanceDao;
        private final WorkManager workManager;
    private final Gson gson = new Gson();
        public OutboxRepositoryImpl(Context context, AppDatabase db, WorkManager wm) {
            this.appContext = context.getApplicationContext();
            this.dao = db.outboxDao();
            this.attendanceDao = db.attendanceDao();
            this.workManager = wm;
        }

        /** Save domain object as PENDING and trigger the sender worker */
        @Override
        public void enqueue(Attendance a) {
           // sets syncStatus="PENDING"
            // Convert to JSON payload
            String payload = gson.toJson(a);
            OutboxEntity outboxEntity = OutboxEntity.newPending(payload);
            dao.insert(outboxEntity);
            kickSender();
        }

        /** Same as above but receives a RecognitionEntity already built */
        @Override
        public void enqueueEntity(AttendanceEntity entity) {
            Attendance attendance = Mapper.AttendanceMapper.fromEntity(entity);
            String payload = gson.toJson(attendance);
            OutboxEntity outboxEntity = OutboxEntity.newPending(payload);
            dao.insert(outboxEntity);
            kickSender();
        }

        /** Ask WorkManager to drain the outbox when we have network */
        @Override
        public void kickSender() {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(OutboxSenderWorker.class)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 8, TimeUnit.HOURS)
                    .addTag(OutboxSenderWorker.TAG)
                    .build();

            workManager.enqueueUniqueWork(
                    OutboxSenderWorker.UNIQUE,
                    ExistingWorkPolicy.REPLACE,
                    work
            );
        }

        // ---------- Worker-facing helpers ----------

        @Override
        public List<OutboxEntity> getPendingBatch(int limit) {
            return dao.nextBatch(limit);
        }

        @Override
        public void markSent(String id) {
            dao.updateStatus(id, "SENT");
        }

        @Override
        public void markFailed(String id) {
            dao.updateStatus(id, "FAILED");
        }

        @Override
        public void cleanupSynced() {
            dao.deleteSent();
        }
    }

