package com.example.audiocontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.audiocontrol.data.Settings;
import com.example.audiocontrol.model.SettingsStorage;

import java.util.Arrays;
import java.util.List;


public class BootReceiver extends BroadcastReceiver {

    private static List<String> actionsToStartService = Arrays.asList(
            "android.intent.action.QUICKBOOT_POWERON",
            "android.intent.action.BOOT_COMPLETED"
    );

    @Override
    public void onReceive(Context context, Intent intent) {
        if (actionsToStartService.contains(intent.getAction())) {
            SettingsStorage settingsStorage = com.example.audiocontrol.SoundApplication.getSettingsStorage(context);
            Settings settings = settingsStorage.settings();
//            if (settings.isNotificationWidgetEnabled) {
//                Intent i = com.example.audiocontrol.SoundService.getIntentForForeground(context, settings);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    context.startForegroundService(i);
//                } else {
//                    context.startService(i);
//                }
//            }
        }
    }
}
