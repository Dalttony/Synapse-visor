package com.example.reconocimiento.domain.usecase;

import com.example.reconocimiento.data.repository.RecognitionRepository;
import com.example.reconocimiento.domain.model.Recognition;

public class SaveRecognitionOfflineUseCase {
    private final RecognitionRepository repo;

    public SaveRecognitionOfflineUseCase(RecognitionRepository repo) {
        this.repo = repo;
    }

    public void run(Recognition recognition) {
        repo.saveOffline(recognition);
    }
}
