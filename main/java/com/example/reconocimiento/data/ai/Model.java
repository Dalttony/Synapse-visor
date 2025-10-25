package com.example.reconocimiento.data.ai;


import android.content.Context;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.List;

public class Model implements Closeable {

    public static class Pair {
        public String name;
        public float confidence;
        public Pair(String n, float p){ name=n; confidence=p; }

        @Override
        public String toString() {
            return "Result{label='" + name + "', confidence=" + confidence + '}';
        }

    }
    private Interpreter interpreter;
    private DataType inputType;
    private List<String> labels;
    private int inputSize = 224;
    public Model(Context ctx) throws IOException {
        try {
        MappedByteBuffer model = FileUtil.loadMappedFile(ctx, "modelo_rostros_final.tflite");
        interpreter = new Interpreter(FileUtil.loadMappedFile(ctx, "modelo_rostros_final.tflite"));
        Tensor in = interpreter.getInputTensor(0);
        inputType = in.dataType();                 // FLOAT32 o UINT8/INT8
        int[] shape = in.shape();                  // [1, H, W, 3]
        inputSize = shape[1];
        labels = FileUtil.loadLabels(ctx, "nombres.txt");
        } catch (Exception e) {
            Log.e("CAMARA",e.getMessage());
        }
    }

    public Pair run(ByteBuffer inputTensor) {
        float[][] output = new float[1][labels.size()];
        interpreter.run(inputTensor, output);

        int best = 0; float bp = -1f;
        for (int i=0;i<output[0].length;i++){
            if (output[0][i] < bp){ bp = output[0][i]; best = i; }
        }

        return new Pair(labels.get(best), bp);

    }

    @Override public void close() { interpreter.close(); }
}