/*
 * Copyright (C) 2016-2019,2022 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

public class SeekBarPreference extends Preference
        implements SeekBar.OnSeekBarChangeListener,
                   View.OnClickListener, View.OnLongClickListener {
    private final String TAG = getClass().getName();
    private final boolean DEBUG = false;

    private Context mContext;

    protected int mInterval = 1;
    protected String mUnits = "";
    protected boolean mContinuousUpdates = false;

    protected int mMinValue = 1;
    protected int mMaxValue = 256;
    protected int mDefaultValue;

    protected int mValue;

    protected TextView mValueTextView;
    protected SeekBar mSeekBar;

    protected boolean mTrackingTouch = false;
    protected int mTrackingValue;

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
        String units = a.getString(R.styleable.SeekBarPreference_units);
        if (units != null) {
            mUnits = units;
        }
        mContinuousUpdates = a.getBoolean(R.styleable.SeekBarPreference_continuousUpdates, mContinuousUpdates);
        mInterval = a.getInt(R.styleable.SeekBarPreference_interval, mInterval);
        a.recycle();

        a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.Preference, defStyleAttr,
                defStyleRes);
        mMaxValue = a.getInt(com.android.internal.R.styleable.ProgressBar_max, mMaxValue);
        mMinValue = a.getInt(com.android.internal.R.styleable.ProgressBar_min, mMinValue);
        if (mMaxValue < mMinValue)
            mMaxValue = mMinValue;
        mDefaultValue = a.getInt(com.android.internal.R.styleable.Preference_defaultValue, mMinValue);
        mDefaultValue = mValue = getLimitedValue(mDefaultValue);
        a.recycle();

        a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);
        final int layoutResId = a.getResourceId(
                com.android.internal.R.styleable.SeekBarPreference_layout,
                com.android.internal.R.layout.preference_widget_seekbar);
        a.recycle();

        setLayoutResource(layoutResId);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context,
                androidx.preference.R.attr.seekBarPreferenceStyle,
                com.android.internal.R.attr.seekBarPreferenceStyle));
    }

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mSeekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        mSeekBar.setMin(getSeekValue(mMinValue));
        mSeekBar.setMax(getSeekValue(mMaxValue));
        mSeekBar.setProgress(getSeekValue(mValue));
        mSeekBar.setEnabled(isEnabled());

        mValueTextView = (TextView) holder.findViewById(R.id.selected_value);

        updateValueViews();

        mSeekBar.setOnSeekBarChangeListener(this);
        mValueTextView.setOnClickListener(this);
        mValueTextView.setOnLongClickListener(this);
    }

    protected int getLimitedValue(int v) {
        return v < mMinValue ? mMinValue : (v > mMaxValue ? mMaxValue : v);
    }

    protected int getSeekValue(int v) {
        return 0 - Math.floorDiv(mMinValue - v, mInterval);
    }

    protected String getTextValue(int v) {
        return String.valueOf(v) + mUnits;
    }

    protected void updateValueViews() {
        if (mValueTextView != null) {
            int progress = !mTrackingTouch || mContinuousUpdates ? mValue : mTrackingValue;
            mValueTextView.setText(getTextValue(progress));
        }
    }

    protected void changeValue(int newValue) {
        // for subclasses
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int newValue = getLimitedValue(mMinValue + (progress * mInterval));
        if (mTrackingTouch && !mContinuousUpdates) {
            mTrackingValue = newValue;
            updateValueViews();
        } else if (mValue != newValue) {
            // change rejected, revert to the previous value
            if (!callChangeListener(newValue)) {
                mSeekBar.setProgress(getSeekValue(mValue));
                return;
            }
            // change accepted, store it
            changeValue(newValue);
            persistInt(newValue);

            mValue = newValue;
            updateValueViews();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mTrackingValue = mValue;
        mTrackingTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mTrackingTouch = false;
        if (!mContinuousUpdates) {
            onProgressChanged(mSeekBar, getSeekValue(mTrackingValue), false);
        }
        notifyChanged();
    }

    @Override
    public void onClick(View v) {
        Context ctx = mContext;
        String value = getTextValue(mDefaultValue);
        String message = ctx.getString(R.string.custom_seekbar_default_value_to_set, value);
        PartsUtils.createToast(ctx, message);
    }

    @Override
    public boolean onLongClick(View v) {
        setValue(getDefaultValue(), true);

        Context ctx = mContext;
        String message = ctx.getString(R.string.custom_seekbar_default_value_is_set);
        PartsUtils.createToast(ctx, message);

        return true;
    }

    // Don't need too much shit about initial and default values
    // its all done in constructor already
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue)
            mValue = getPersistedInt(mValue);
    }

    public void setDefaultValue(int newValue, boolean update) {
        newValue = getLimitedValue(newValue);
        if (mDefaultValue != newValue) {
            mDefaultValue = newValue;
            if (update)
                updateValueViews();
        }
    }

    public void setMax(int max) {
        if (mMaxValue != max) {
            mMaxValue = max;
            if (mSeekBar != null) {
                mSeekBar.setMax(mMaxValue - mMinValue);
            }
        }
    }

    public void setMin(int min) {
        if (mMinValue != min) {
            mMinValue = min;
            if (mSeekBar != null) {
                mSeekBar.setMax(mMaxValue - mMinValue);
            }
        }
    }

    public void setValue(int newValue) {
        mValue = getLimitedValue(newValue);
        if (mSeekBar != null) {
            mSeekBar.setProgress(getSeekValue(mValue));
        }
    }

    public void setValue(int newValue, boolean update) {
        newValue = getLimitedValue(newValue);
        if (mValue != newValue) {
            if (update) {
                mSeekBar.setProgress(getSeekValue(newValue));
            } else {
                mValue = newValue;
            }
        }
    }

    public int getValue() {
        return mValue;
    }

    public int getDefaultValue() {
        return mDefaultValue;
    }

    // need some methods here to set/get other attrs at runtime,
    // but who really need this ...

    public void refresh(int newValue) {
        // this will ...
        setValue(newValue, mSeekBar != null);
    }
}
