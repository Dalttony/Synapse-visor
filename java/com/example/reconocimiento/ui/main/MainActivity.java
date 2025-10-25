package com.example.reconocimiento.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.reconocimiento.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {


    private MainViewModel viewModel;

    private PreviewView previewView;
    private TextView tvResult;
    private FloatingActionButton btnCapture;
    // Camera / analysis
    private ImageCapture imageCapture;
    private Bitmap lastBitmap;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;

    // TFLite
    private Interpreter tflite;
    private List<String> labels;
    private int inputSize = 224; // se sobrescribe con la del modelo
    private DataType inputType;
    private final String TAG = "MAINACTIVITY_LOG";
     private final ActivityResultLauncher<String> requestCamPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> { if (granted) startCamera(); });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        previewView = findViewById(R.id.preview);
        tvResult = findViewById(R.id.tvResult);
        btnCapture = findViewById(R.id.btnCapture);

        // Observe predictions
        viewModel.getPrediction().observe(this, result -> {
            if (result != null) {
                tvResult.setText(String.format("Bienvenido: %s", result.name));
            }
        });

        btnCapture.setOnClickListener(v -> {
            // Captura la imagen actual y la procesa
            captureAndRecognizeFace();
        });






        // 1) Cargar modelo y labels
        initTflite();

        // 2) ML Kit Face detector (modo rápido)
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        faceDetector = FaceDetection.getClient(options);

        // 3) Permiso de cámara y CameraX
        cameraExecutor = Executors.newSingleThreadExecutor();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCamPerm.launch(Manifest.permission.CAMERA);
        }
    }

    private void initTflite() {
        try {
            tflite = new Interpreter(FileUtil.loadMappedFile(this, "modelo_rostros_final.tflite"));
            Tensor in = tflite.getInputTensor(0);
            inputType = in.dataType();                 // FLOAT32 o UINT8/INT8
            int[] shape = in.shape();                  // [1, H, W, 3]
            inputSize = shape[1];
            labels = FileUtil.loadLabels(this, "nombres.txt");
            tvResult.setText("Modelo cargado. Clases: " + labels.size());
        } catch (Exception e) {
            Log.e("CAMARA",e.getMessage());
            tvResult.setText("Error cargando modelo: " + e.getMessage());
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(inputSize, inputSize))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview, analysis, imageCapture
                );
            } catch (Exception e) {
                tvResult.setText("Error cámara: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeFrame(@NonNull ImageProxy imageProxy) {
        Image media = imageProxy.getImage();
        if (media == null) { imageProxy.close(); return; }

        int rotation = imageProxy.getImageInfo().getRotationDegrees();
        InputImage img = InputImage.fromMediaImage(media, rotation);

        faceDetector.process(img)
                .addOnSuccessListener(faces -> {
                    if (faces.isEmpty()) {
                        tvResult.post(() -> tvResult.setText("Por favor, hacercate para hacr la identificación"));
                    } else {
                        Face face = Collections.max(faces, (a,b) ->
                                Integer.compare(a.getBoundingBox().width()*a.getBoundingBox().height(),
                                        b.getBoundingBox().width()*b.getBoundingBox().height()));

                        Bitmap frameBmp = toBitmap(media, rotation);
                        Bitmap faceBmp = cropWithPadding(frameBmp, face.getBoundingBox(), 0.25f);
                        Bitmap resized = Bitmap.createScaledBitmap(faceBmp, inputSize, inputSize, true);
                        lastBitmap = resized;
                        imageProxy.close();
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    // ======= Inferencia TFLite =======
    private static class Pair { String name; float prob; Pair(String n, float p){ name=n; prob=p; } }

    private Pair infer(Bitmap bmp) {
        // preparar input según tipo del modelo
        ByteBuffer inputBuffer;
        if (inputType == DataType.FLOAT32) {
            inputBuffer = bitmapToFloatBuffer(bmp, true); // (x/127.5)-1
        } else if (inputType == DataType.UINT8) {
            inputBuffer = bitmapToUInt8Buffer(bmp);
        } else if (inputType == DataType.INT8) {
            // cuantizado int8: centramos en 0 (aprox). Para calibración exacta usar scale/zeroPoint
            inputBuffer = bitmapToInt8Buffer(bmp);
        } else {
            throw new IllegalStateException("Tipo de entrada no soportado: " + inputType);
        }

        float[][] out = new float[1][labels.size()];
        tflite.run(inputBuffer, out);
        int best = 0; float bp = -1f;
        for (int i=0;i<out[0].length;i++){
            if (out[0][i] < bp){ bp = out[0][i]; best = i; }
        }
        return new Pair(labels.get(best), bp);
    }

    private ByteBuffer bitmapToFloatBuffer(Bitmap bmp, boolean mobilenetV2Norm) {
        int w = bmp.getWidth(), h = bmp.getHeight();
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * w * h * 3);
        bb.order(ByteOrder.nativeOrder());
        int[] px = new int[w*h];
        bmp.getPixels(px, 0, w, 0, 0, w, h);
        int idx = 0;
        for (int y=0;y<h;y++){
            for (int x=0;x<w;x++){
                int p = px[idx++];
                float r = ((p>>16)&0xFF);
                float g = ((p>>8)&0xFF);
                float b = (p&0xFF);
                if (mobilenetV2Norm) {
                    r = r/127.5f - 1f;
                    g = g/127.5f - 1f;
                    b = b/127.5f - 1f;
                }
                bb.putFloat(r); bb.putFloat(g); bb.putFloat(b);
            }
        }
        bb.rewind();
        return bb;
    }

    private ByteBuffer bitmapToUInt8Buffer(Bitmap bmp) {
        int w = bmp.getWidth(), h = bmp.getHeight();
        ByteBuffer bb = ByteBuffer.allocateDirect(w*h*3);
        int[] px = new int[w*h];
        bmp.getPixels(px, 0, w, 0, 0, w, h);
        int idx = 0;
        for (int y=0;y<h;y++){
            for (int x=0;x<w;x++){
                int p = px[idx++];
                bb.put((byte)((p>>16)&0xFF));
                bb.put((byte)((p>>8)&0xFF));
                bb.put((byte)(p&0xFF));
            }
        }
        bb.rewind();
        return bb;
    }

    private ByteBuffer bitmapToInt8Buffer(Bitmap bmp) {
        int w = bmp.getWidth(), h = bmp.getHeight();
        ByteBuffer bb = ByteBuffer.allocateDirect(w*h*3);
        int[] px = new int[w*h];
        bmp.getPixels(px, 0, w, 0, 0, w, h);
        int idx = 0;
        for (int y=0;y<h;y++){
            for (int x=0;x<w;x++){
                int p = px[idx++];
                // aproximación: 0..255 -> -128..127
                bb.put((byte)(((p>>16)&0xFF) - 128));
                bb.put((byte)(((p>>8)&0xFF)  - 128));
                bb.put((byte)((p&0xFF)       - 128));
            }
        }
        bb.rewind();
        return bb;
    }

    // ======= Utilidades imagen =======
    private Bitmap cropWithPadding(Bitmap src, Rect box, float pad) {
        int w = src.getWidth(), h = src.getHeight();
        int px = (int)(box.width() * pad);
        int py = (int)(box.height() * pad);
        int x0 = Math.max(box.left - px, 0);
        int y0 = Math.max(box.top - py, 0);
        int x1 = Math.min(box.right + px, w);
        int y1 = Math.min(box.bottom + py, h);
        int cw = Math.max(1, x1 - x0);
        int ch = Math.max(1, y1 - y0);
        return Bitmap.createBitmap(src, x0, y0, cw, ch);
    }

    private Bitmap toBitmap(Image image, int rotationDegrees) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21(image), ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        yuv.compressToJpeg(new Rect(0,0,image.getWidth(), image.getHeight()), 50, out);
        byte[] jbytes = out.toByteArray();
        Bitmap bmp = BitmapFactory.decodeByteArray(jbytes, 0, jbytes.length);
        if (rotationDegrees != 0) {
            Matrix m = new Matrix();
            m.postRotate(rotationDegrees);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        }
        return bmp;
    }

    private byte[] nv21(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) tflite.close();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (faceDetector != null) faceDetector.close();
    }

    private void captureAndRecognizeFace() {
        Log.e(TAG, String.valueOf(lastBitmap));
        if (lastBitmap == null) {
            tvResult.setText("Not datos");
            return;
        }
        viewModel.recognizeOffline(lastBitmap);


        // Observe predictions
        viewModel.getPrediction().observe(this, result -> {
            if (result != null) {
               Log.e(TAG,result.toString());
            }
        });

        // Infiere
        /*Pair pred = infer(lastBitmap);
        tvResult.post(() ->
                tvResult.setText("Predicción: " + pred.name + "  " + String.format("%.2f", pred.prob*100) + "%")
        );*/


        // Tomar una foto en memoria (sin guardar en archivo)
        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    @OptIn(markerClass = ExperimentalGetImage.class)
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                       Log.e("CAMARA", exception.getMessage());
                    }
                });
    }



}
