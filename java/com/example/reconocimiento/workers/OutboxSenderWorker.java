package com.example.reconocimiento.workers;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.reconocimiento.Application;
import com.example.reconocimiento.data.local.AppDatabase;
import com.example.reconocimiento.data.local.OutboxEntity;
import com.example.reconocimiento.data.local.RecognitionEntity;
import com.example.reconocimiento.data.remote.api.ApiService;
import com.example.reconocimiento.data.remote.dto.OutBoxRequest;
import com.example.reconocimiento.data.remote.dto.RecognitionRequest;
import com.example.reconocimiento.domain.model.Recognition;
import com.example.reconocimiento.data.mapper.RecognitionMapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

/**
 * Sends pending recognitions to the server when network is available.
 */
public class OutboxSenderWorker extends Worker {

    public static final String UNIQUE = "send_outbox";
    public static final String TAG = "OutboxSenderWorker";

    private final com.example.reconocimiento.data.local.AppDatabase db;
    private final ApiService api;

    public OutboxSenderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);

        // Access singletons from AppContainer
        db = Application.getAppContainer().getDatabase();
        api = Application.getAppContainer().getApiService();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting outbox sync...");

        List<OutboxEntity> pendingList =
                db.outboxDao().nextBatch( 25);

        if (pendingList.isEmpty()) {
            Log.d(TAG, "No pending recognitions to upload.");
            return Result.success();
        }

        boolean transientError = false;

        for (OutboxEntity entity : pendingList) {
            OutBoxRequest req = new OutBoxRequest(
                    entity.getId(),
                    entity.getPayloadJson(),
                    entity.getCreatedAt(),
                    entity.getAttempts()
            );

            try {
                Response<com.example.reconocimiento.data.remote.dto.Response> resp =
                        api.uploadRecognition(entity.getId(), req).execute();

                if (resp.isSuccessful() && resp.body() != null) {
                    db.outboxDao().updateStatus(entity.getId(), "SENT");
                    Log.d(TAG, "Uploaded recognition: " + entity.getId());
                } else {
                    transientError = (resp.code() >= 500);
                    db.outboxDao().updateStatus(entity.getId(), "FAILED");
                    Log.e(TAG, "Upload failed for " + entity.getId() + " code: " + resp.code());
                }

            } catch (IOException e) {
                transientError = true;
                db.outboxDao().updateStatus(entity.getId(), "FAILED");
                Log.e(TAG, "Network error: " + e.getMessage());
            }
        }



        // Retry later if we had transient network/server errors
        if (transientError) {
            Log.d(TAG, "Transient errors detected — scheduling retry.");
            return Result.retry();
        }

        db.outboxDao().deleteSent();

        Log.d(TAG, "Outbox sync completed.");
        return Result.success();
    }

    /**
     * Helper to schedule this worker from anywhere in the app.
     */
    public static void enqueue(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(OutboxSenderWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .addTag(TAG)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE, ExistingWorkPolicy.KEEP, work);
    }
}

