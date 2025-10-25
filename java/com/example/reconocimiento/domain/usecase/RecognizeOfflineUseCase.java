package com.example.reconocimiento.domain.usecase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.reconocimiento.data.ai.Model;
import com.example.reconocimiento.data.ai.PhotoPreprocessor;
import com.example.reconocimiento.domain.model.Recognition;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class RecognizeOfflineUseCase {

    private final Model interpreter;

    public RecognizeOfflineUseCase(Model interpreter) {
        this.interpreter = interpreter;
    }

    public Recognition run(Bitmap bitmap) throws IOException {
       // Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

        ByteBuffer input = PhotoPreprocessor.preprocess(bitmap);


        Model.Pair r = interpreter.run(input);

        return new Recognition(
                UUID.randomUUID().toString(),
                r.name,
                r.confidence,
               "Pru",
                System.currentTimeMillis()
        );
    }
}
