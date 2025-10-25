package com.example.reconocimiento.data.repository;

import com.example.reconocimiento.data.local.AppDatabase;
import com.example.reconocimiento.data.local.RecognitionEntity;
import com.example.reconocimiento.data.local.dao.RecognitionDao;
import com.example.reconocimiento.data.mapper.RecognitionMapper;
import com.example.reconocimiento.domain.model.Recognition;

import java.util.ArrayList;
import java.util.List;

public class RecognitionRepository {

    private final RecognitionDao dao;

    public RecognitionRepository(AppDatabase db) {
        this.dao = db.recognitionDao();
    }

    /** Save recognition offline as PENDING (no network required) */
    public void saveOffline(Recognition recognition) {
        RecognitionEntity e = RecognitionMapper.toEntity(recognition);
        if (e.getSyncStatus().isEmpty()) {
            e.setSyncStatus("PENDING");
        }
        dao.insertRecognition(e);
    }

    public List<Recognition> getAll() {
        List<RecognitionEntity> rows = dao.getAllRecognitions();
        List<Recognition> out = new ArrayList<>(rows.size());
        for (RecognitionEntity e : rows) out.add(RecognitionMapper.fromEntity(e));
        return out;
    }

    /** For sync worker: fetch pending batch */
    public List<RecognitionEntity> getPendingBatch(int limit) {
        return dao.getBySyncStatus("PENDING", limit);
    }

    public void markSent(String id) { dao.updateStatus(id, "SENT"); }
    public void markFailed(String id) { dao.updateStatus(id, "FAILED"); }
    public void cleanupSynced() { dao.deleteSynced(); }

    // ---------- helpers ----------
    private static List<Recognition> mapList(List<RecognitionEntity> rows) {
        List<Recognition> out = new ArrayList<>(rows.size());
        for (RecognitionEntity e : rows) out.add(RecognitionMapper.fromEntity(e));
        return out;
    }
}