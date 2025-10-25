package com.example.reconocimiento.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.reconocimiento.Application;
import com.example.reconocimiento.data.local.AppDatabase;
import com.example.reconocimiento.data.local.RecognitionEntity;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CleanupWorker extends Worker {

    private static final String TAG = "CleanupWorker";
    private final AppDatabase db;

    public CleanupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = Application.getAppContainer().getDatabase();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Running cleanup task...");

        long now = System.currentTimeMillis();

        // Define cutoff: older than 7 days
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long cutoff = cal.getTimeInMillis();

        List<RecognitionEntity> all = db.recognitionDao().getAllRecognitions();
        for (RecognitionEntity e : all) {
            if ("SENT".equals(e.getSyncStatus()) && e.getTimestamp() < cutoff) {
                // delete file if exists
                File f = new File(e.getPhotoPath());
                if (f.exists()) {
                    boolean deleted = f.delete();
                    Log.d(TAG, "Deleted old photo " + f.getName() + ": " + deleted);
                }
                db.recognitionDao().updateStatus(e.getId(), "DELETED");
            }
        }

        // Optionally remove DELETED records permanently
        // db.recognitionDao().deleteByStatus("DELETED");

        Log.d(TAG, "Cleanup finished.");
        return Result.success();
    }

    /** Schedule this cleanup to run once every 24 hours */
    public static void schedule(Context context) {
        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(CleanupWorker.class, 24, TimeUnit.HOURS)
                        .addTag(TAG)
                        .build();

        WorkManager.getInstance(context).enqueue(request);
    }
}
