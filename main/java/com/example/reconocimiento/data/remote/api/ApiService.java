package com.example.reconocimiento.data.remote.api;

import com.example.reconocimiento.data.remote.dto.OutBoxRequest;
import com.example.reconocimiento.data.remote.dto.RecognitionRequest;
import com.example.reconocimiento.data.remote.dto.Response;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    /**
     * Uploads one recognition to the backend.
     *
     * Example POST /api/recognitions
     */
    @POST("/api/recognitions")
    Call<Response> uploadRecognition(
            @Header("Idempotency-Key") String idempotencyKey,
            @Body OutBoxRequest body
    );

    /**
     * (Optional) Retrieve details of a recognition by ID.
     *
     * Example GET /api/recognitions/{id}
     */
    @GET("/api/recognitions/{id}")
    Call<Response> getRecognitionById(@Path("id") String id);
}