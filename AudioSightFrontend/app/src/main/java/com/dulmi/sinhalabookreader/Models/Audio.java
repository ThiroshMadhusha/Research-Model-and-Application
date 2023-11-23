package com.dulmi.sinhalabookreader.Models;

public class Audio {

    private int audioFileName;
    private String audioContent;

    public Audio() {
    }

    public Audio(int audioFileName, String audioContent) {
        this.audioFileName = audioFileName;
        this.audioContent = audioContent;
    }

    public int getAudioFileName() {
        return audioFileName;
    }

    public String getAudioContent() {
        return audioContent;
    }
}
