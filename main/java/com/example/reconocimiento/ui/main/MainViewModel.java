package com.example.reconocimiento.ui.main;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.reconocimiento.data.ai.Model;
import com.example.reconocimiento.domain.model.Attendance;
import com.example.reconocimiento.domain.model.Recognition;
import com.example.reconocimiento.domain.usecase.AttendanceUseCase;
import com.example.reconocimiento.domain.usecase.EnqueueOutboxUseCase;
import com.example.reconocimiento.domain.usecase.RecognizeOfflineUseCase;

import java.io.File;
import java.nio.ByteBuffer;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<Model.Pair> prediction = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    private final RecognizeOfflineUseCase recognizeUseCase;
    private final AttendanceUseCase attendanceUseCase;
    private final EnqueueOutboxUseCase enqueueOutboxUseCase;

    public MainViewModel(@NonNull Application app) {
        super(app);
        // Ideally injected via AppContainer
        recognizeUseCase = com.example.reconocimiento.Application.getAppContainer().getRecognizeOfflineUseCase();
        attendanceUseCase = com.example.reconocimiento.Application.getAppContainer().getAttendanceUseCase();
        enqueueOutboxUseCase = com.example.reconocimiento.Application.getAppContainer().getEnqueueOutboxUseCase();
    }

    public LiveData<Model.Pair> getPrediction() { return prediction; }
    public LiveData<String> getMessage() { return message; }

    public void recognizeOffline(Bitmap file) {
        new Thread(() -> {
            try {
                Recognition recognition = recognizeUseCase.run(file); // offline ML
                Log.e("MAINACTIVITY_",recognition.getLabel() +" "+recognition.getConfidence());
                prediction.postValue(new Model.Pair(recognition.getLabel(), recognition.getConfidence()));
                Attendance a = attendanceUseCase.run(recognition.getLabel(), recognition.getTimestamp(), null);
                enqueueOutboxUseCase.run(a);
            } catch (Exception e) {
                //message.postValue("Error en reconocimiento: " + e.getMessage());
                Log.e("MAINACTIVITY_",e.getMessage() +" "+e.getStackTrace()[0].toString());
            }
        }).start();
    }

    private String currentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    private Long currentDateTimestamp() {
        return System.currentTimeMillis();
    }

    private String currentTime() {
        return new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    // Optional: convert ImageProxy to Bitmap
    public Bitmap imageProxyToBitmap(@NonNull ImageProxy imageProxy) {
        try {
            ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            int rotation = imageProxy.getImageInfo().getRotationDegrees();
            if (rotation != 0) {
                android.graphics.Matrix m = new android.graphics.Matrix();
                m.postRotate(rotation);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
            }
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }
}