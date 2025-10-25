package com.example.reconocimiento.di;

import android.content.Context;

import androidx.work.WorkManager;

import com.example.reconocimiento.data.ai.Model;
import com.example.reconocimiento.data.local.AppDatabase;
import com.example.reconocimiento.data.remote.api.ApiFactory;
import com.example.reconocimiento.data.remote.api.ApiService;
import com.example.reconocimiento.data.repository.AttendanceRepository;
import com.example.reconocimiento.data.repository.RecognitionRepository;
import com.example.reconocimiento.data.repository.impl.OutboxRepositoryImpl;
import com.example.reconocimiento.data.repository.impl.RecognitionRepositoryImpl;
import com.example.reconocimiento.domain.usecase.AttendanceUseCase;
import com.example.reconocimiento.domain.usecase.EnqueueOutboxUseCase;
import com.example.reconocimiento.domain.usecase.RecognizeOfflineUseCase;
import com.example.reconocimiento.domain.usecase.SaveRecognitionOfflineUseCase;

import java.io.IOException;


/**
 * Simple manual dependency container.
 * All app-wide singletons are created and kept here.
 */
public class AppContainer {

    // --- Singletons ---
    private final AppDatabase database;
    private final ApiService apiService;
    private final WorkManager workManager;

    // --- Repositories ---
    public final RecognitionRepository recognitionRepository;
    public final OutboxRepositoryImpl outboxRepository;
    public  final  AttendanceRepository attendanceRepository;
    // --- UseCases ---
    public final RecognizeOfflineUseCase recognizeOfflineUseCase;
    public final SaveRecognitionOfflineUseCase saveRecognitionOfflineUseCase;
    private  final AttendanceUseCase attendanceUseCase;
    public final EnqueueOutboxUseCase enqueueOutboxUseCase;

    public AppContainer(Context context) throws IOException {
        // 1) Core singletons
        database = AppDatabase.getDatabase(context);
        apiService = ApiFactory.create();
        workManager = WorkManager.getInstance(context);

        // 2) Repositories
        recognitionRepository = new RecognitionRepository(database);
        outboxRepository = new OutboxRepositoryImpl(context, database, workManager);
        attendanceRepository = new AttendanceRepository(database.attendanceDao());
        // 3) ML interpreter (for offline recognition)
        Model interpreter = new Model(context);

        // 4) Use cases
        recognizeOfflineUseCase = new RecognizeOfflineUseCase(interpreter);
        saveRecognitionOfflineUseCase = new SaveRecognitionOfflineUseCase(recognitionRepository);
        enqueueOutboxUseCase = new EnqueueOutboxUseCase(outboxRepository);
        attendanceUseCase = new AttendanceUseCase(attendanceRepository);
    }

    public AppDatabase getDatabase() { return database; }
    public ApiService getApiService() { return apiService; }

    public RecognizeOfflineUseCase getReconiceUseCase(){
        return  recognizeOfflineUseCase;
    };
    public AttendanceUseCase getAttendanceUseCase(){
        return attendanceUseCase;
    }

    public RecognizeOfflineUseCase getRecognizeOfflineUseCase(){
        return  recognizeOfflineUseCase;
    }
    public EnqueueOutboxUseCase getEnqueueOutboxUseCase(){
        return  enqueueOutboxUseCase;
    }
}