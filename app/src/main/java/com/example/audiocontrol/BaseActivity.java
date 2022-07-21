package com.example.audiocontrol;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audiocontrol.data.Settings;
import com.example.audiocontrol.logger.Log;
import com.example.audiocontrol.logger.LogWrapper;
import com.example.audiocontrol.model.SettingsStorage;

abstract public class BaseActivity extends AppCompatActivity {
    protected Settings settings;
    protected AudioControl control;
    private SettingsStorage settingsStorage;
    public static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        control =  com.example.audiocontrol.SoundApplication.getAudioControl(this);
        settingsStorage = com.example.audiocontrol.SoundApplication.getSettingsStorage(this);

        settings = settingsStorage.settings();

        if (settings.isDarkThemeEnabled) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
    }

    protected void setThemeAndRecreate(boolean isDarkTheme) {
        this.settings.isDarkThemeEnabled = isDarkTheme;
        settingsStorage.save(this.settings);
        recreateActivity();
    }

    protected void recreateActivity() {
        this.recreate();
    }

    protected boolean isDarkTheme() {
        return settings.isDarkThemeEnabled;
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean currentTheme = settingsStorage.settings().isDarkThemeEnabled;
        if (currentTheme != this.settings.isDarkThemeEnabled) {
            recreateActivity();
        }
        initializeLogging();
    }

    /** Set up targets to receive log data */
    public void initializeLogging() {
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        // Wraps Android's native log framework
        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);

        Log.i(TAG, "Ready");
    }
}
