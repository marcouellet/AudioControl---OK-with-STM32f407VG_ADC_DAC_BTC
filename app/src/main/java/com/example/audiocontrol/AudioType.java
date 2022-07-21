package com.example.audiocontrol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by punksta on 19.06.16.
 */
public class AudioType {
    public static AudioType VOLUME = new AudioType(R.string.audioType_volume, "Volume");
    public static AudioType BASS = new AudioType(R.string.audioType_bass, "Bass");
    public static AudioType TREBLE = new AudioType(R.string.audioType_treble, "Treble");
    public static AudioType BALANCE = new AudioType(R.string.audioType_balance, "Balance");
    public final int nameId;
    public final String name;

     AudioType(int nameId, String name) {
        this.nameId = nameId;
        this.name = name;
    }

    public static List<AudioType> getAudioTypes() {
        List<AudioType> result = new ArrayList<>();

        result.add(VOLUME);
        result.add(BASS);
        result.add(TREBLE);
        result.add(BALANCE);

        return result;
    }
}
