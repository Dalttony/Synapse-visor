package com.example.reconocimiento.data.ai;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Postprocessor {

    private Postprocessor() {}

    // ---------------- Labels ----------------

    /** Load labels (one per line) from assets, e.g. "ml/labels.txt". */
    @NonNull
    public static List<String> loadLabels(@NonNull Context ctx, @NonNull String assetPath) throws Exception {
        AssetManager am = ctx.getAssets();
        List<String> labels = new ArrayList<>();
        try (InputStream is = am.open(assetPath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) labels.add(line.trim());
            }
        }
        return labels;
    }

    // ---------------- Float outputs ----------------

    /** If your model returns logits, convert to probabilities with softmax. */
    @NonNull
    public static float[] softmax(@NonNull float[] logits) {
        float max = Float.NEGATIVE_INFINITY;
        for (float v : logits) if (v > max) max = v;

        double sum = 0.0;
        double[] exps = new double[logits.length];
        for (int i = 0; i < logits.length; i++) {
            double e = Math.exp(logits[i] - max);
            exps[i] = e;
            sum += e;
        }
        float[] probs = new float[logits.length];
        for (int i = 0; i < logits.length; i++) {
            probs[i] = (float) (exps[i] / sum);
        }
        return probs;
    }

    /** Top-K from probabilities (already softmaxed). */
    @NonNull
    public static List<Result> topKFromProbabilities(@NonNull float[] probs,
                                                     @NonNull List<String> labels,
                                                     int k,
                                                     float minConfidence) {
        int n = probs.length;
        List<Result> all = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            float p = probs[i];
            if (p >= minConfidence) {
                String label = (i < labels.size()) ? labels.get(i) : ("class_" + i);
                all.add(new Result(i, label, p));
            }
        }
        all.sort(Comparator.comparing(Result::getConfidence).reversed());
        if (k > 0 && all.size() > k) return new ArrayList<>(all.subList(0, k));
        return all;
    }

    /** Top-K directly from logits (applies softmax internally). */
    @NonNull
    public static List<Result> topKFromLogits(@NonNull float[] logits,
                                              @NonNull List<String> labels,
                                              int k,
                                              float minConfidence) {
        float[] probs = softmax(logits);
        return topKFromProbabilities(probs, labels, k, minConfidence);
    }

    // ---------------- Quantized (UINT8) outputs ----------------

    /**
     * For quantized heads: dequantize bytes → probabilities using scale/zeroPoint.
     * If your model outputs logits quantized, you may still want softmax afterwards.
     */
    @NonNull
    public static float[] dequantizeToFloat(@NonNull byte[] q, float scale, int zeroPoint) {
        float[] f = new float[q.length];
        for (int i = 0; i < q.length; i++) {
            int u = q[i] & 0xFF; // uint8
            f[i] = (u - zeroPoint) * scale;
        }
        return f;
    }

    /** Top-K from quantized PROBABILITIES (already in [0,1] domain). */
    @NonNull
    public static List<Result> topKFromQuantizedProb(@NonNull byte[] q,
                                                     float scale,
                                                     int zeroPoint,
                                                     @NonNull List<String> labels,
                                                     int k,
                                                     float minConfidence) {
        float[] probs = dequantizeToFloat(q, scale, zeroPoint);
        // If these are actual probabilities in [0,1], proceed directly:
        return topKFromProbabilities(probs, labels, k, minConfidence);
    }

    /** Top-K from quantized LOGITS (apply softmax after dequantization). */
    @NonNull
    public static List<Result> topKFromQuantizedLogits(@NonNull byte[] q,
                                                       float scale,
                                                       int zeroPoint,
                                                       @NonNull List<String> labels,
                                                       int k,
                                                       float minConfidence) {
        float[] logits = dequantizeToFloat(q, scale, zeroPoint);
        return topKFromLogits(logits, labels, k, minConfidence);
    }

    // ---------------- ByteBuffer helpers (if your output is a buffer) ----------------

    /** Read a float[] from a direct Float buffer (size = numClasses). */
    @NonNull
    public static float[] readFloatArray(@NonNull ByteBuffer buffer, int numClasses) {
        buffer.rewind();
        float[] out = new float[numClasses];
        for (int i = 0; i < numClasses; i++) out[i] = buffer.getFloat();
        buffer.rewind();
        return out;
    }

    /** Read a byte[] (uint8) from buffer (size = numClasses). */
    @NonNull
    public static byte[] readByteArray(@NonNull ByteBuffer buffer, int numClasses) {
        buffer.rewind();
        byte[] out = new byte[numClasses];
        buffer.get(out);
        buffer.rewind();
        return out;
    }

    // ---------------- Convenience: return only the best class ----------------

    /** Best class from probabilities. */
    @NonNull
    public static Result bestFromProbabilities(@NonNull float[] probs, @NonNull List<String> labels) {
        int idx = 0;
        float best = -1f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > best) { best = probs[i]; idx = i; }
        }
        String label = (idx < labels.size()) ? labels.get(idx) : ("class_" + idx);
        return new Result(idx, label, best);
    }

    /** Best class from logits. */
    @NonNull
    public static Result bestFromLogits(@NonNull float[] logits, @NonNull List<String> labels) {
        return bestFromProbabilities(softmax(logits), labels);
    }

    // ---------------- Result POJO ----------------

    public static final class Result {
        private final int index;
        private final String label;
        private final float confidence;

        public Result(int index, @NonNull String label, float confidence) {
            this.index = index;
            this.label = label;
            this.confidence = confidence;
        }

        public int getIndex() { return index; }
        @NonNull public String getLabel() { return label; }
        public float getConfidence() { return confidence; }

        @NonNull
        @Override public String toString() {
            return "Result{index=" + index + ", label='" + label + "', confidence=" + confidence + '}';
        }
    }
}
