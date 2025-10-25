package com.example.reconocimiento.data.ai;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.*;
import android.media.ExifInterface;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PhotoPreprocessor {

    // ==== Configure these for your model ====
    public static final int MODEL_INPUT_WIDTH  = 224;
    public static final int MODEL_INPUT_HEIGHT = 224;
    public static final int MODEL_INPUT_CHANNELS = 3; // RGB
    public static final boolean NORMALIZE_MINUS1_TO_1 = false; // true -> [-1,1], false -> [0,1]

    private PhotoPreprocessor() {}

    // ---------- Public API ----------

    /** Preprocess from a File → float32 NHWC ByteBuffer. */
    @NonNull
    public static ByteBuffer preprocess(@NonNull Context ctx, @NonNull File imageFile) throws IOException {
        Bitmap bmp = decodeBitmapFixRotation(imageFile.getAbsolutePath());
        return preprocess(bmp);
    }

    /** Preprocess from a Uri (e.g., camera result) → float32 NHWC ByteBuffer. */
    @NonNull
    public static ByteBuffer preprocess(@NonNull Context ctx, @NonNull Uri imageUri) throws IOException {
        Bitmap bmp = decodeBitmapFixRotation(ctx.getContentResolver(), imageUri);
        return preprocess(bmp);
    }

    /** Preprocess from a Bitmap you already have → float32 NHWC ByteBuffer. */
    @NonNull
    public static ByteBuffer preprocess(@NonNull Bitmap src) {
        Bitmap square = centerCropSquare(src);
        Bitmap resized = Bitmap.createScaledBitmap(square, MODEL_INPUT_WIDTH, MODEL_INPUT_HEIGHT, true);
        if (square != src) square.recycle();

        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT * MODEL_INPUT_CHANNELS);
        buffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT];
        resized.getPixels(pixels, 0, MODEL_INPUT_WIDTH, 0, 0, MODEL_INPUT_WIDTH, MODEL_INPUT_HEIGHT);

        // Normalize & write as float32 RGB
        final float scale = NORMALIZE_MINUS1_TO_1 ? (2f / 255f) : (1f / 255f);
        final float offset = NORMALIZE_MINUS1_TO_1 ? -1f : 0f;

        for (int y = 0; y < MODEL_INPUT_HEIGHT; y++) {
            int rowOffset = y * MODEL_INPUT_WIDTH;
            for (int x = 0; x < MODEL_INPUT_WIDTH; x++) {
                int c = pixels[rowOffset + x];
                float r = ((c >> 16) & 0xFF) * scale + offset;
                float g = ((c >> 8)  & 0xFF) * scale + offset;
                float b = ( c        & 0xFF) * scale + offset;
                buffer.putFloat(r);
                buffer.putFloat(g);
                buffer.putFloat(b);
            }
        }

        resized.recycle();
        buffer.rewind();
        return buffer;
    }

    // ---------- Decoding helpers ----------

    /** Decode bitmap from file path and apply EXIF rotation if present. */
    @NonNull
    public static Bitmap decodeBitmapFixRotation(@NonNull String filePath) throws IOException {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bmp = BitmapFactory.decodeFile(filePath, opts);
        if (bmp == null) throw new IOException("Failed to decode image: " + filePath);

        int rotation = exifRotationDegrees(filePath);
        return rotation == 0 ? bmp : rotate(bmp, rotation);
    }

    /** Decode from Uri via ContentResolver and try to honor EXIF rotation. */
    @NonNull
    public static Bitmap decodeBitmapFixRotation(@NonNull ContentResolver cr, @NonNull Uri uri) throws IOException {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bmp;
        try (InputStream is = cr.openInputStream(uri)) {
            if (is == null) throw new IOException("Cannot open URI: " + uri);
            bmp = BitmapFactory.decodeStream(is, null, opts);
        }
        if (bmp == null) throw new IOException("Failed to decode URI: " + uri);

        // Try to read EXIF by first resolving to a temp JPEG in memory if needed
        int rotation = 0;
        try (InputStream is2 = cr.openInputStream(uri)) {
            if (is2 != null) {
                rotation = exifRotationDegrees(is2);
            }
        } catch (Exception ignore) {}

        return rotation == 0 ? bmp : rotate(bmp, rotation);
    }

    // ---------- Image ops ----------

    /** Center-crop to a square Bitmap. */
    @NonNull
    public static Bitmap centerCropSquare(@NonNull Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int size = Math.min(w, h);
        int x = (w - size) / 2;
        int y = (h - size) / 2;
        return Bitmap.createBitmap(src, x, y, size, size);
    }

    /** Rotate a bitmap by degrees. Recycles the input. */
    @NonNull
    public static Bitmap rotate(@NonNull Bitmap src, int degrees) {
        Matrix m = new Matrix();
        m.postRotate(degrees);
        Bitmap out = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
        if (out != src) src.recycle();
        return out;
    }

    // ---------- EXIF helpers ----------

    /** Read EXIF rotation from a file path. */
    private static int exifRotationDegrees(@NonNull String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return exifToDegrees(orientation);
        } catch (IOException e) {
            return 0;
        }
    }

    /** Read EXIF rotation from an input stream (fallback for content URIs). */
    private static int exifRotationDegrees(@NonNull InputStream is) {
        try {
            // Need a copy because ExifInterface requires a seekable stream; buffer it in memory.
            byte[] bytes = readAllBytes(is);
            ExifInterface exif = new ExifInterface(new java.io.ByteArrayInputStream(bytes));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return exifToDegrees(orientation);
        } catch (Exception e) {
            return 0;
        }
    }

    private static int exifToDegrees(int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:  return 90;
            case ExifInterface.ORIENTATION_ROTATE_180: return 180;
            case ExifInterface.ORIENTATION_ROTATE_270: return 270;
            default: return 0;
        }
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[16 * 1024];
        int n;
        while ((n = in.read(buf)) > 0) baos.write(buf, 0, n);
        return baos.toByteArray();
    }

    // ---------- Optional: UINT8 (quantized) variant ----------

    /**
     * For quantized models (UINT8 input). Returns NHWC ByteBuffer with bytes 0..255.
     */
    @NonNull
    public static ByteBuffer preprocessUint8(@NonNull Bitmap src) {
        Bitmap square = centerCropSquare(src);
        Bitmap resized = Bitmap.createScaledBitmap(square, MODEL_INPUT_WIDTH, MODEL_INPUT_HEIGHT, true);
        if (square != src) square.recycle();

        ByteBuffer buffer = ByteBuffer.allocateDirect(MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT * MODEL_INPUT_CHANNELS);
        buffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[MODEL_INPUT_WIDTH * MODEL_INPUT_HEIGHT];
        resized.getPixels(pixels, 0, MODEL_INPUT_WIDTH, 0, 0, MODEL_INPUT_WIDTH, MODEL_INPUT_HEIGHT);

        for (int y = 0; y < MODEL_INPUT_HEIGHT; y++) {
            int rowOffset = y * MODEL_INPUT_WIDTH;
            for (int x = 0; x < MODEL_INPUT_WIDTH; x++) {
                int c = pixels[rowOffset + x];
                buffer.put((byte) ((c >> 16) & 0xFF)); // R
                buffer.put((byte) ((c >> 8)  & 0xFF)); // G
                buffer.put((byte) ( c        & 0xFF)); // B
            }
        }

        resized.recycle();
        buffer.rewind();
        return buffer;
    }
}