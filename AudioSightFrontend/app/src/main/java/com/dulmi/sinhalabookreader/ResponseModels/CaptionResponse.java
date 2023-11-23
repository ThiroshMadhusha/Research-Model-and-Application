package com.dulmi.sinhalabookreader.ResponseModels;

import com.google.gson.annotations.SerializedName;

public class CaptionResponse {

    @SerializedName("caption")
    private String caption;

    public CaptionResponse() {

    }

    public CaptionResponse(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }
}
