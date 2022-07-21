package com.example.audiocontrol;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.audiocontrol.bluetooth.BluetoothFragment;
import com.example.audiocontrol.data.SoundProfile;
import com.example.audiocontrol.logger.Log;
import com.example.audiocontrol.logger.LogWrapper;
import com.example.audiocontrol.logger.MessageOnlyLogFilter;
import com.example.audiocontrol.model.SoundProfileStorage;
import com.example.audiocontrol.util.DynamicShortcutManager;
import com.example.audiocontrol.view.AudioControlProfileView;
import com.example.audiocontrol.view.AudioControlSliderView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.example.audiocontrol.util.ProfileApplier.applyProfile;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";
    public static final String PROFILE_ID = "PROFILE_ID";
    private List<AudioControlListener> audioControlListeners = new ArrayList<>();
    private SoundProfileStorage profileStorage;
    private boolean goingGoFinish = false;
    private BluetoothFragment mBluetoothFragment;

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
             switch (msg.what) {
                case com.example.audiocontrol.bluetooth.Constants.MESSAGE_DEVICE_UPDATE_LEVELS:
                    sendAllLevels();
                    break;
            }
        }
    };

    public static Intent createOpenProfileIntent(Context context, SoundProfile profile) {
        Intent intent1 = new Intent(context.getApplicationContext(), MainActivity.class);
        intent1.setAction(Intent.ACTION_VIEW);
        intent1.putExtra(PROFILE_ID, profile.id);
        return intent1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        profileStorage = SoundApplication.getSoundProfileStorage(this);
        buildUi();
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mBluetoothFragment = new BluetoothFragment(mHandler);
            transaction.replace(R.id.sample_content_fragment, mBluetoothFragment);
            transaction.commit();

            if (handleIntent(getIntent())) {
                goingGoFinish = true;
                finish();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!handleIntent(intent)) {
            super.onNewIntent(intent);
        }
    }

    private boolean handleIntent(Intent intent) {
        if (intent.hasExtra(PROFILE_ID)) {
            int profileId = intent.getIntExtra(PROFILE_ID, 0);
            try {
                SoundProfile profile = profileStorage.loadById(profileId);
                if (profile != null) {
                    applyProfile(control, profile);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setIntent(null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create a chain of targets that will receive log data
     */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        /*LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);*/
        //msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        //super.onSaveInstanceState(outState, outPersistentState);
    }

    private void renderProfileItems() {
        View title = findViewById(R.id.audio_types_holder_title);
        ViewGroup titlesGroup = findViewById(R.id.linearLayout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            titlesGroup.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }

        int indexOfTitle = titlesGroup.indexOfChild(title);

        List<AudioType> audioTypes = AudioType.getAudioTypes();

        if (!Boolean.TRUE.equals(title.getTag())) {
            for (int i = 0; i < audioTypes.size(); i++) {
                AudioType type = audioTypes.get(i);

                final AudioControlSliderView audioControlSliderView = new AudioControlSliderView(this);

                audioControlSliderView.setTag(type.name);
                titlesGroup.addView(audioControlSliderView, indexOfTitle + i + 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                audioControlSliderView.setName(getString(type.nameId));
                audioControlSliderView.setMaxValue(control.getMaxLevel(type.nameId));
                audioControlSliderView.setMinValue(control.getMinLevel(type.nameId));
                audioControlSliderView.setCurrentValue(control.getCurrentLevel(type.nameId));


                final AudioControlListener audioControlListener = new AudioControlListener(type.nameId) {
                    @Override
                    public void onChangeIndex(int audioType, int currentLevel, int max) {
                        if (currentLevel < control.getMinLevel(type)) {
                            audioControlSliderView.setCurrentValue(control.getMinLevel(type));
                        } else {
                            audioControlSliderView.setCurrentValue(currentLevel);
                        }
                    }
                };

                audioControlListeners.add(audioControlListener);

                audioControlSliderView.setListener((level, fromUser) -> {
                    if (fromUser) {
                        requireChangeLevel(type, level);
                    }
                });
            }
            title.setTag(Boolean.TRUE);
        }
    }

    private void buildUi() {

        Switch s = findViewById(R.id.dark_theme_switcher);

        s.setChecked(isDarkTheme());
        s.setOnCheckedChangeListener((buttonView, isChecked) -> setThemeAndRecreate(isChecked));

        renderProfileItems();

        try {
            renderProfiles();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopSoundService() {
        Intent i = SoundService.getStopIntent(this);
        startService(i);
    }

    private void startSoundService() {
        Intent i = SoundService.getIntentForForeground(this, settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (goingGoFinish) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DynamicShortcutManager.setShortcuts(this, profileStorage.loadAll());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void renderProfile(final SoundProfile profile) {
        final LinearLayout profiles = findViewById(R.id.profile_list);
        final AudioControlProfileView view = new AudioControlProfileView(this);
        String tag = "profile_" + profile.id;
        profiles.removeView(profiles.findViewWithTag(tag));
        view.setTag(tag);


        view.setProfileTitle(profile.name);
        view.setOnActivateClickListener(() -> applyProfile(control, profile));
    }

    private void renderProfiles() throws JSONException {
        LinearLayout profiles = findViewById(R.id.profile_list);
        profiles.removeAllViews();

        for (final SoundProfile profile : profileStorage.loadAll()) {
            renderProfile(profile);
        }
    }

    private void requireChangeLevel(AudioType audioType, int level) {
        try {
            control.setCurrentLevel(audioType.nameId, level);
            broadcastUpdate(audioType);
            sendUpdate(audioType, level);
        } catch (Throwable throwable) {
            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            throwable.printStackTrace();
        }
    }

    private void sendAllLevels() {
        try {
            if (mBluetoothFragment!= null && mBluetoothFragment.IsBluetoothServiceConnected()) {
                String message = "";
                String prefix = "";
                List<AudioType> audioTypes = AudioType.getAudioTypes();
                for (int i = 0; i < audioTypes.size(); i++) {
                    AudioType audioType = audioTypes.get(i);
                    int level = control.getCurrentLevel(audioType.nameId);
                    message += prefix + formatLevelUpdate(audioType, level);
                    prefix = ",";
                }
                mBluetoothFragment.sendMessage(message + "\n");
            }

        } catch (Throwable throwable) {
            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            throwable.printStackTrace();
        }
    }


    private void broadcastUpdate(AudioType audioType) {
        String action = null;

        switch (audioType.nameId) {
            case R.string.audioType_volume:
                action = AudioControl.VOLUME_CHANGED_ACTION;
                break;
            case R.string.audioType_bass:
                action = AudioControl.BASS_CHANGED_ACTION;
                break;
            case R.string.audioType_treble:
                action = AudioControl.TREBLE_CHANGED_ACTION;
                break;
            case R.string.audioType_balance:
                action = AudioControl.BALANCE_CHANGED_ACTION;
                break;
        }
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void sendUpdate(AudioType audioType, int level) {
        if (mBluetoothFragment!= null && mBluetoothFragment.IsBluetoothServiceConnected()) {
            mBluetoothFragment.sendMessage(formatLevelUpdate(audioType, level) + "\n");
        }
    }

    private String formatLevelUpdate(AudioType audioType, int level)  {
        String message;

        switch (audioType.nameId) {
            case R.string.audioType_volume:
                message = "VOLUME";
                break;
            case R.string.audioType_bass:
                message = "BASS";
                break;
            case R.string.audioType_treble:
                message = "TREBLE";
                break;
            case R.string.audioType_balance:
                message = "BALANCE";
                break;
            default:
                message = "OTHER";
        }

        message+= " " + level;

        return message;
    }

    @Override
    protected void onStart() {
        super.onStart();

        for (AudioControlListener listener : audioControlListeners)
            control.registerAudioControlListener(listener.type, listener, true);
    }

    @Override
    protected void recreateActivity() {
        Intent intent = new Intent(this, this.getClass());
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);

    }

    @Override
    protected void onStop() {
        super.onStop();
        for (AudioControlListener listener : audioControlListeners)
            control.unRegisterAudioControlListener(listener.type, listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioControlListeners.clear();
    }

    static abstract class AudioControlListener implements AudioControl.AudioControlListener {
        final int type;

        AudioControlListener(int type) {
            this.type = type;
        }
    }
}
