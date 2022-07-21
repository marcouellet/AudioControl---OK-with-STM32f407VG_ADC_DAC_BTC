package com.example.audiocontrol.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.audiocontrol.R;

public class AudioControlSliderView extends FrameLayout {

    private TextView mTitle;
    private TextView mCurrentValue;
    private IntegerSeekBar seekBar;

    private AudioControlSliderChangeListener audioControlListener;
    private SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (audioControlListener != null) {
                IntegerSeekBar bar = (IntegerSeekBar) seekBar;
                audioControlListener.onChange(bar.getValue(), fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    public AudioControlSliderView(Context context) {
        super(context);
        init();
    }

    public AudioControlSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioControlSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AudioControlSliderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.audio_type_view, this, false);
        mTitle = view.findViewById(R.id.title);
        mCurrentValue = view.findViewById(R.id.current_value);
        seekBar = view.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(listener);
        addView(view);
    }


    public void setMaxValue(int maxValue) {
        seekBar.setMaxValue(maxValue);
    }

    public void setMinValue(int minValue) {
        seekBar.setMinValue(minValue);
    }

    public void setName(CharSequence string) {
        mTitle.setText(string);
        seekBar.setContentDescription(getContext().getString(R.string.volume_switch, string));
    }

     public void updateProgressText(int progress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mCurrentValue.setText("" + (progress - seekBar.getMin()) + "/" + (seekBar.getMax() - seekBar.getMin()));
        } else {
            mCurrentValue.setText("" + progress + "/" + seekBar.getMax());
        }
    }

    public void setCurrentValue(int progress) {
        seekBar.setValue(progress);
        updateProgressText(progress);
    }

    public void setListener(AudioControlSliderChangeListener audioControlListener) {
        this.audioControlListener = audioControlListener;
    }

    public interface AudioControlSliderChangeListener {
        void onChange(int level, boolean fromUser);
    }
}
