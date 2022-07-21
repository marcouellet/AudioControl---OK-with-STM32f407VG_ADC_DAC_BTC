package com.example.audiocontrol.util;
import com.example.audiocontrol.AudioControl;
import com.example.audiocontrol.data.SoundProfile;

import java.util.ArrayList;
import java.util.List;

public class NotificationWidgetUpdateTracker {
    private int lastNotificationHashCode = Integer.MIN_VALUE;

    public void onNotificationShow(AudioControl control, List<Integer> profilesToShow, SoundProfile[] profiles) {
        lastNotificationHashCode = calculateHashCode(control, profilesToShow, profiles);
    }

    public boolean shouldShow(AudioControl control, List<Integer> profilesToShow, SoundProfile[] profiles) {
        return lastNotificationHashCode != calculateHashCode(control, profilesToShow, profiles);
    }


    private int calculateHashCode(AudioControl control, List<Integer> profilesToShow, SoundProfile[] profiles) {
        List<Object> result = new ArrayList<>();

        if (profilesToShow != null) {
            for (Integer id : profilesToShow) {
                result.add(id.toString() + " " + control.getCurrentLevel(id));
            }
        } else {
            result.add("no_volume_profiles");
        }

        for (SoundProfile soundProfile : profiles) {
            result.add(soundProfile);
        }

        System.out.println(result.hashCode());
        return result.hashCode();
    }
}
