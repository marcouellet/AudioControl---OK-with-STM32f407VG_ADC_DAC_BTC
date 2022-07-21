package com.example.audiocontrol.util;

import android.widget.Toast;

import com.example.audiocontrol.AudioControl;
import com.example.audiocontrol.data.SoundProfile;

import java.util.Map;

public class ProfileApplier {
    public static void applyProfile(AudioControl control, SoundProfile profile) {
        for (Map.Entry<Integer, Integer> nameAndValue : profile.settings.entrySet()) {
            control.setCurrentLevel(nameAndValue.getKey(), nameAndValue.getValue());
        }
        Toast.makeText(control.getContext(), "Sounds profile " + profile.name + " applied", Toast.LENGTH_SHORT).show();
    }
}
