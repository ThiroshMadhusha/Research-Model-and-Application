package com.dulmi.sinhalabookreader.ResponseModels;

import com.google.gson.annotations.SerializedName;

public class OcrResponse {

    @SerializedName("audio_book_path")
    private String encodedAudio;

    public OcrResponse() {
    }

    public OcrResponse(String encodedAudio) {
        this.encodedAudio = encodedAudio;
    }

    public String getEncodedAudio() {
        return encodedAudio;
    }
}
