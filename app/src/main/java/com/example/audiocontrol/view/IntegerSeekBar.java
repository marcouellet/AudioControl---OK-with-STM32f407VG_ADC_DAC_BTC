package com.example.audiocontrol.view;

import android.content.Context;
import android.util.AttributeSet;
import android.content.res.TypedArray;
import android.widget.SeekBar;

import com.example.audiocontrol.R;

public class IntegerSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {
    private int max = 1;
    private int min = 0;

    public IntegerSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyAttrs(attrs);
    }

    public IntegerSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttrs(attrs);
    }

    public IntegerSeekBar(Context context) {
        super(context);
    }

    public int getValue() {
        return (int) ((max - min) * ((float) getProgress() / (float) getMax()) + min);
    }

    public void setValue(int value) {
        setProgress((int) ((float)(value - min) / (float)(max - min) * getMax()));
    }

    public int getMinValue() {
        return this.min;
    }

    public void setMinValue(int value) {
        this.min = value;
    }

    public int getMaxValue() {
        return this.max;
    }


    public void setMaxValue(int value) {
        this.max = value;
    }

    private void applyAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.IntegerSeekBar);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.IntegerSeekBar_intMax:
                    this.max = a.getInt(attr, 1);
                    break;
                case R.styleable.IntegerSeekBar_intMin:
                    this.min = a.getInt(attr, 0);
                    break;
            }
        }
        a.recycle();
    }
}
