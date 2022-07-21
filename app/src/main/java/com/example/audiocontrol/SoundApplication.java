package  com.example.audiocontrol;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.audiocontrol.model.SettingsStorage;
import com.example.audiocontrol.model.SoundProfileStorage;
import com.example.audiocontrol.util.TestLabUtils;

public class SoundApplication extends Application {
    private AudioControl audioControl;
    private SoundProfileStorage profileStorage;
    private SettingsStorage settingsStorage;

    public static AudioControl getAudioControl(Context context) {
        return ((SoundApplication) context.getApplicationContext()).getAudioControl();
    }

    public static SoundProfileStorage getSoundProfileStorage(Context context) {
        return ((SoundApplication) context.getApplicationContext()).getProfileStorage();
    }

    public static SettingsStorage getSettingsStorage(Context context) {
        return ((SoundApplication) context.getApplicationContext()).getSettingsStorage();
    }

    public AudioControl getAudioControl() {
        if (audioControl == null) {
            audioControl = new AudioControl(this, new Handler());
        }
        return audioControl;
    }

    public SoundProfileStorage getProfileStorage() {
        if (profileStorage == null) {
            profileStorage = SoundProfileStorage.getInstance(this);
        }
        return profileStorage;
    }

    public SettingsStorage getSettingsStorage() {
        if (settingsStorage == null) {
            settingsStorage = new SettingsStorage(PreferenceManager.getDefaultSharedPreferences(this));
        }
        return settingsStorage;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        if (TestLabUtils.isInTestLab(this)) {
            Toast.makeText(this, "in testlab", Toast.LENGTH_LONG).show();
        }
    }
}
