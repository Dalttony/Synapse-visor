package com.example.reconocimiento.data.mapper;

import com.example.reconocimiento.data.local.AttendanceEntity;
import com.example.reconocimiento.data.local.OutboxEntity;
import com.example.reconocimiento.data.local.RecognitionEntity;
import com.example.reconocimiento.domain.model.Attendance;
import com.example.reconocimiento.domain.model.OutBox;
import com.example.reconocimiento.domain.model.Recognition;

public class Mapper {

    public static final class OutBoxMapper {
        private OutBoxMapper() {}

        public static OutboxEntity toEntity(OutBox d) {
            return OutboxEntity.newPending(
                    d.getPayloadJson()
            );
        }

        public static OutBox fromEntity(OutboxEntity e) {
            return new OutBox(
                    e.getId(), e.getPayloadJson(), e.getCreatedAt(), e.getAttempts()
            );
        }
    }


    public static final class AttendanceMapper {
        private AttendanceMapper() {}

        public static AttendanceEntity toEntity(Attendance d) {
            return new AttendanceEntity(
                    d.getWorkerName(),
                    d.getEntryDate(),
                    d.getExitDate()

            );
        }

        public static Attendance fromEntity(AttendanceEntity e) {
            return new Attendance(
                    e.getId(), e.getWorkerName(), e.getEntryDate(), e.getExitDate()
            );
        }
    }
}
