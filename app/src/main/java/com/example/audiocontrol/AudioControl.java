package  com.example.audiocontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by punksta on 19.06.16.
 * wrapper over AudioManager to easy control
 */
public class AudioControl {

    public static final String VOLUME_CHANGED_ACTION = "com.example.audiocontrol.VOLUME_CHANGED";
    public static final String BASS_CHANGED_ACTION = "com.example.audiocontrol.BASS_CHANGED";
    public static final String TREBLE_CHANGED_ACTION = "com.example.audiocontrol.TREBLE_CHANGED";
    public static final String BALANCE_CHANGED_ACTION = "com.example.audiocontrol.BALANCE_CHANGED";

    private final DSPManager dspManager;
    private final Context context;

    private final Map<Integer, Set<AudioControlListener>> listenerSet = new HashMap<>();

    private final IntentFilter intentFilter;
    private final Handler handler;
    private AudioObserver audioObserver;
    private boolean ignoreUpdates = false;

    public AudioControl(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;

        this.dspManager = new DSPManager();

        intentFilter = new IntentFilter();
        intentFilter.addAction(VOLUME_CHANGED_ACTION);
        intentFilter.addAction(BASS_CHANGED_ACTION);
        intentFilter.addAction(TREBLE_CHANGED_ACTION);
        intentFilter.addAction(BALANCE_CHANGED_ACTION);
     }

    public Context getContext() {
        return context;
    }

    public void setCurrentLevel(int type, int level) {
        dspManager.setCurrentLevel(type, level);
    }

    public int getMaxLevel(int type) {
        return dspManager.getMaxLevel(type);
    }

    public int getMinLevel(int type) {
        return dspManager.getMinLevel(type);
    }

    public int getCurrentLevel(int type) {
        return dspManager.getCurrentLevel(type);
    }

    public void registerAudioControlListener(int type, final AudioControlListener audioListener, boolean sendCurrentValue) {
        boolean firstAudioType = listenerSet.isEmpty();
        boolean isFirstListener = !listenerSet.containsKey(type);
        if (isFirstListener) {
            Set<AudioControlListener> listeners = new HashSet<>();
            listeners.add(audioListener);
            listenerSet.put(type, listeners);
        } else {
            listenerSet.get(type).add(audioListener);
        }
        if (firstAudioType) {
            if (audioObserver == null) {
                audioObserver = new AudioObserver();
            }
            context.registerReceiver(audioObserver, intentFilter);
        }

        if (sendCurrentValue)
            audioListener.onChangeIndex(type, getCurrentLevel(type), getMaxLevel(type));
    }

    public void unRegisterAudioControlListener(int type, AudioControlListener audioControlListener) {
        Set<AudioControlListener> audioControlListeners = listenerSet.get(type);
        if (audioControlListeners != null) {
            audioControlListeners.remove(audioControlListener);
            if (audioControlListeners.size() == 0) {
                listenerSet.remove(type);
            }
        }

        if (listenerSet.isEmpty() && audioObserver != null) {
            context.unregisterReceiver(audioObserver);
            audioObserver = null;
        }
    }

    public interface AudioControlListener {
        void onChangeIndex(int id, int currentLevel, int max);
    }

    private class AudioObserver extends BroadcastReceiver {

        //last levels for each AudioType
        private Runnable updateRunnable = () -> {
            update();
            ignoreUpdates = false;
        };

        private void notifyListeners(Integer type, int newLevel) {
            int max = getMaxLevel(type);
            for (AudioControlListener audioListener : listenerSet.get(type))
                audioListener.onChangeIndex(type, newLevel, max);
        }

        private void update() {
            for (Map.Entry<Integer, Set<AudioControlListener>> entry : listenerSet.entrySet()) {
                int current = getCurrentLevel(entry.getKey());
                notifyListeners(entry.getKey(), current);
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ignoreUpdates) {
                handler.removeCallbacks(updateRunnable);
                handler.postDelayed(updateRunnable, 500);
                return;
            }

            ignoreUpdates = true;
            handler.postDelayed(updateRunnable, 500);
        }
    }
}
