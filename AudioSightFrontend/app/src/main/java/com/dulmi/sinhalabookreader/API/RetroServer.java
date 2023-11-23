package com.dulmi.sinhalabookreader.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroServer {

    private static final String BASE_URL = "https://ba2f-2402-d000-a400-7778-8008-d673-1a9d-b60e.ngrok-free.app";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {

        if (retrofit == null) {

            // Create an OkHttp client with logging interceptor
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // Set a 5-minute (300,000 milliseconds) connection and read timeout
            httpClient.connectTimeout(300000, TimeUnit.MILLISECONDS);
            httpClient.readTimeout(300000, TimeUnit.MILLISECONDS);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Set log level to BODY for full logging
            httpClient.addInterceptor(logging);

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

}
