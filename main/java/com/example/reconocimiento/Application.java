package com.example.reconocimiento;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.example.reconocimiento.data.local.AppDatabase;
import com.example.reconocimiento.data.repository.RecognitionRepository;
import com.example.reconocimiento.di.AppContainer;
import com.example.reconocimiento.sync.NetworkWatcher;

import java.io.IOException;

public class Application extends android.app.Application  implements Configuration.Provider {

    public static final String CHANNEL_SYNC = "sync_status";
    private static AppContainer appContainer;
    private AppDatabase db;
    private RecognitionRepository recognitionRepo;
    private NetworkWatcher networkWatcher;

    private static final String STATION = "central";
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            appContainer = new AppContainer(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 1) Create notification channel for background sync (optional but nice UX)
        createSyncChannel();

        // 2) Warm up singletons
        db = AppDatabase.getDatabase(this);
        recognitionRepo = new RecognitionRepository(db);

       /* // 3) (Optional) tweak WorkManager logging
        WorkManager.initialize(this, new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
        );
*/
        // 4) Register network watcher to kick outbox when connectivity returns
        networkWatcher = new NetworkWatcher(
                this,
                // If you also use an OutboxRepository, pass it here; else you can
                // create a small wrapper that calls WorkManager.enqueueUniqueWork(...)
                new com.example.reconocimiento.data.repository.impl.OutboxRepositoryImpl(
                        this.getApplicationContext(),
                        db,
                        WorkManager.getInstance(this)
                )
        );
        networkWatcher.register();

        // 5) Optional: Warm-up DB/Model on a background thread
        new Thread(() -> {
            db.getOpenHelper().getWritableDatabase(); // touch DB
            // If you have a TFLite model, you could lazy-init here.
        }).start();
    }

    private void createSyncChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_SYNC,
                    "Background Sync",
                    NotificationManager.IMPORTANCE_LOW
            );
            ch.setDescription("Shows sync status and retries");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }

    public static AppContainer getAppContainer() {
        return appContainer;
    }

    // Expose stuff if you want easy access without DI
    public AppDatabase getDb() { return db; }
    public RecognitionRepository getRecognitionRepo() { return recognitionRepo; }
    public NetworkWatcher getNetworkWatcher() { return networkWatcher; }


    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                // .setWorkerFactory(yourFactory)   // if you use a custom WorkerFactory
                .build();
    }
}