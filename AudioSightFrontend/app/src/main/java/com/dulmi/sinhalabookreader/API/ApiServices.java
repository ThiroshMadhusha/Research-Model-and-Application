package com.dulmi.sinhalabookreader.API;

import com.dulmi.sinhalabookreader.ResponseModels.CaptionResponse;
import com.dulmi.sinhalabookreader.ResponseModels.ObjectDetectionResponse;
import com.dulmi.sinhalabookreader.ResponseModels.OcrResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiServices {

    @Multipart
    @POST("/api/detection")
    Call<ObjectDetectionResponse> uploadImage(@Part MultipartBody.Part image);

    @Multipart
    @POST("/api/captioning")
    Call<CaptionResponse> getCaptioning(@Part MultipartBody.Part image);

    @Multipart
    @POST("/api/books")
    Call<OcrResponse> getOCR(
            @Part MultipartBody.Part image,
            @Part("user_id") RequestBody user_id,
            @Part("book_id") RequestBody book_id
    );

}
