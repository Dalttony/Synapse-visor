package com.example.reconocimiento.domain.usecase;

import androidx.annotation.Nullable;

import com.example.reconocimiento.data.mapper.Mapper;
import com.example.reconocimiento.data.repository.AttendanceRepository;
import com.example.reconocimiento.domain.model.Attendance;

public class AttendanceUseCase {

    private final AttendanceRepository repo;

    public AttendanceUseCase(AttendanceRepository repo) {
        this.repo = repo;
    }

    public Attendance run(String workerName, Long dateStr, @Nullable Long timeStr) {
       return Mapper.AttendanceMapper.fromEntity(repo.onRecognition(workerName, dateStr, timeStr));
    }
}
