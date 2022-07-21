package com.example.audiocontrol.model;

import android.content.SharedPreferences;

import com.example.audiocontrol.data.Settings;

public class SettingsStorage {
    private static final String KEY_DARK_THEME = "DARK_THEME";
    private final SharedPreferences preferences;

    public SettingsStorage(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void save(Settings settings) {
        preferences.edit()
                .putBoolean(KEY_DARK_THEME, settings.isDarkThemeEnabled)
                 .apply();
    }

    private static Settings defaultSettings = new Settings();

    public Settings settings() {
        return new Settings(
                preferences.getBoolean(KEY_DARK_THEME, defaultSettings.isDarkThemeEnabled)
        );
    }

}
