package com.dulmi.sinhalabookreader.Content;

import com.dulmi.sinhalabookreader.Models.Audio;
import com.dulmi.sinhalabookreader.R;

import java.util.ArrayList;
import java.util.List;

public class AudioContents {

    public List<Audio>captionAudios() {

        List<Audio>audioList = new ArrayList<>();

        audioList.add(new Audio(R.raw.fish, "Fish"));
        audioList.add(new Audio(R.raw.rabbit, "Rabbit"));
        audioList.add(new Audio(R.raw.deer, "Deer"));
        audioList.add(new Audio(R.raw.heron_and_fish, "Heron and fish"));
        audioList.add(new Audio(R.raw.herons_in_the_sky, "Herons in the sky"));


        return audioList;

    }

    public List<Audio>objectAudios() {

        List<Audio>audioList = new ArrayList<>();

        audioList.add(new Audio(R.raw.bed, "bed"));
        audioList.add(new Audio(R.raw.bench, "bench"));
        audioList.add(new Audio(R.raw.book, "book"));
        audioList.add(new Audio(R.raw.bottle, "bottle"));
        audioList.add(new Audio(R.raw.cup, "cup"));
        audioList.add(new Audio(R.raw.keyboard, "keyboard"));
        audioList.add(new Audio(R.raw.umbrella, "umbrella"));
        audioList.add(new Audio(R.raw.scissors, "scissors"));
        audioList.add(new Audio(R.raw.spoon, "spoon"));
        audioList.add(new Audio(R.raw.mouse, "mouse"));
        audioList.add(new Audio(R.raw.chair, "chair"));
        audioList.add(new Audio(R.raw.cell_phone, "cell phone"));
        audioList.add(new Audio(R.raw.handbag, "backpack"));
        audioList.add(new Audio(R.raw.laptop, "laptop"));




        return audioList;

    }

}
