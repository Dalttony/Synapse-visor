package com.example.reconocimiento.data.mapper;

import com.example.reconocimiento.data.local.RecognitionEntity;
import com.example.reconocimiento.domain.model.Recognition;

public final class RecognitionMapper {
    private RecognitionMapper() {}

    public static RecognitionEntity toEntity(Recognition d) {
        return new RecognitionEntity(
                d.getId(),
                d.getLabel(),
                d.getConfidence(),
                d.getPhotoPath(),
                d.getTimestamp(),
                "PENDING"
        );
    }

    public static Recognition fromEntity(RecognitionEntity e) {
        return new Recognition(
                e.getId(), e.getLabel(), e.getConfidence(), e.getPhotoPath(), e.getTimestamp()
        );
    }
}