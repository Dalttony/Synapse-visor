package com.example.reconocimiento.ui.main;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.reconocimiento.data.remote.api.ApiService;
import com.example.reconocimiento.data.remote.dto.ModelResponse;
import com.example.reconocimiento.di.AppContainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModelUpdateViewModel extends AndroidViewModel {

    private static final String TAG = "ModelUpdateViewModel";

    private final AppContainer appContainer;
    private final ApiService apiService;

    private final MutableLiveData<ModelResponse> latestModel = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Integer> downloadProgress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> downloadEnabled = new MutableLiveData<>();

    public ModelUpdateViewModel(@NonNull Application application) {
        super(application);
        this.appContainer = ((com.example.reconocimiento.Application) application).getAppContainer();
        this.apiService = appContainer.getApiService();

        // Initialize default values
        statusMessage.setValue("Presiona 'Verificar' para comprobar actualizaciones");
        isLoading.setValue(false);
        downloadProgress.setValue(0);
        downloadEnabled.setValue(false);
    }

    public LiveData<ModelResponse> getLatestModel() {
        return latestModel;
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Integer> getDownloadProgress() {
        return downloadProgress;
    }

    public LiveData<Boolean> getDownloadEnabled() {
        return downloadEnabled;
    }

    public void checkForUpdates() {
        isLoading.setValue(true);
        statusMessage.setValue("Verificando actualizaciones...");
        downloadEnabled.setValue(false);

        Call<ModelResponse> call = apiService.getLatestModel();
        call.enqueue(new Callback<ModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<ModelResponse> call, @NonNull Response<ModelResponse> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    ModelResponse model = response.body();
                    latestModel.setValue(model);

                    // Check if update is available (compare with current version)
                    String currentVersion = getCurrentModelVersion();
                    if (!model.getVersion().equals(currentVersion)) {
                        statusMessage.setValue("Nueva versión disponible: " + model.getVersion());
                        downloadEnabled.setValue(true);
                    } else {
                        statusMessage.setValue("Tu modelo está actualizado");
                        downloadEnabled.setValue(false);
                    }
                } else {
                    statusMessage.setValue("Error al verificar actualizaciones: " + response.code());
                    Log.e(TAG, "Error checking updates: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ModelResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                statusMessage.setValue("Error de conexión. Verifica tu internet.");
                Log.e(TAG, "Network error checking updates", t);
            }
        });
    }

    public void downloadModel() {
        ModelResponse model = latestModel.getValue();
        if (model == null) {
            statusMessage.setValue("No hay modelo disponible para descargar");
            return;
        }

        downloadEnabled.setValue(false);
        statusMessage.setValue("Iniciando descarga...");
        downloadProgress.setValue(0);

        // Extract model ID from archivo field or use version
        String modelId = extractModelId(model.getArchivo());

        Call<ResponseBody> call = apiService.downloadModel(modelId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Start download in background thread
                    new Thread(() -> downloadFile(response.body(), model)).start();
                } else {
                    statusMessage.postValue("Error al descargar: " + response.code());
                    downloadEnabled.postValue(true);
                    Log.e(TAG, "Download error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                statusMessage.postValue("Error de conexión durante la descarga");
                downloadEnabled.postValue(true);
                Log.e(TAG, "Download network error", t);
            }
        });
    }

    private void downloadFile(ResponseBody body, ModelResponse model) {
        try {
            File modelDir = new File(getApplication().getFilesDir(), "models");
            if (!modelDir.exists()) {
                modelDir.mkdirs();
            }

            File modelFile = new File(modelDir, "modelo_rostros_final.tflite");
            File labelsFile = new File(modelDir, "nombres.txt");

            InputStream inputStream = body.byteStream();
            FileOutputStream outputStream = new FileOutputStream(modelFile);

            byte[] buffer = new byte[4096];
            long total = 0;
            long fileSize = body.contentLength();
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                total += bytesRead;
                outputStream.write(buffer, 0, bytesRead);

                // Update progress
                if (fileSize > 0) {
                    int progress = (int) ((total * 100) / fileSize);
                    downloadProgress.postValue(progress);
                }
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Also save labels if available
            if (model.getTxt_archivo() != null && !model.getTxt_archivo().isEmpty()) {
                // Here you could download the labels file as well
                // For now, we'll just update the status
            }

            statusMessage.postValue("Modelo descargado exitosamente");
            downloadProgress.postValue(100);

            // Update current version
            saveCurrentModelVersion(model.getVersion());

        } catch (IOException e) {
            statusMessage.postValue("Error al guardar el archivo");
            downloadEnabled.postValue(true);
            Log.e(TAG, "File save error", e);
        }
    }

    private String extractModelId(String archivo) {
        if (archivo != null && !archivo.isEmpty()) {
            // Extract ID from filename or path
            String[] parts = archivo.split("/");
            String filename = parts[parts.length - 1];
            return filename.replace(".tflite", "");
        }
        // Fallback to using version as ID
        ModelResponse model = latestModel.getValue();
        return model != null ? model.getVersion() : "latest";
    }

    private String getCurrentModelVersion() {
        // Get current version from shared preferences or file
        return getApplication().getSharedPreferences("model_prefs", 0)
                .getString("current_version", "1.0.0");
    }

    private void saveCurrentModelVersion(String version) {
        // Save current version to shared preferences
        getApplication().getSharedPreferences("model_prefs", 0)
                .edit()
                .putString("current_version", version)
                .apply();
    }
}