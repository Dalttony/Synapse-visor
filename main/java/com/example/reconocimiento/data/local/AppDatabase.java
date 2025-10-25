    package com.example.reconocimiento.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.reconocimiento.data.local.dao.AttendanceDao;
import com.example.reconocimiento.data.local.dao.OutboxDao;
import com.example.reconocimiento.data.local.dao.RecognitionDao;



@Database(entities = {
        AttendanceEntity.class,
        RecognitionEntity.class,
        OutboxEntity.class// 👈 Nueva entidad agregada
}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract OutboxDao outboxDao();
    private static volatile AppDatabase INSTANCE;

    public abstract AttendanceDao attendanceDao();
    public abstract RecognitionDao recognitionDao(); // 👈 Nuevo DAO

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "face_recognition_db"
                            )
                            .fallbackToDestructiveMigration() // recrea DB si hay cambios estructurales
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

