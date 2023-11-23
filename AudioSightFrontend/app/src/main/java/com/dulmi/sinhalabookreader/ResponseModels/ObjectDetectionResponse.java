package com.dulmi.sinhalabookreader.ResponseModels;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ObjectDetectionResponse {

    @SerializedName("detected_image")
    private String detected_image;

    @SerializedName("objects_detected")
    private List<String>objects_detected;

    public ObjectDetectionResponse() {

    }

    public ObjectDetectionResponse(String detected_image, List<String> objects_detected) {
        this.detected_image = detected_image;
        this.objects_detected = objects_detected;
    }

    public String getDetected_image() {
        return detected_image;
    }

    public List<String> getObjects_detected() {
        return objects_detected;
    }
}
