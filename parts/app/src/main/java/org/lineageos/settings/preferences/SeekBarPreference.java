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

import android.annotation.IntDef;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.TickVisibilityMode;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.view.HapticFeedbackConstants.SCROLL_LIMIT;
import static android.view.HapticFeedbackConstants.SEGMENT_FREQUENT_TICK;

import static org.lineageos.settings.BuildConfig.DEBUG;

@SuppressLint("RestrictedApi")
public class SeekBarPreference extends Preference
        implements Slider.OnChangeListener,
                   Slider.OnSliderTouchListener,
                   View.OnClickListener, View.OnLongClickListener {
    private final String TAG = getClass().getName();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true, value = {
        HAPTIC_FEEDBACK_MODE_NONE,
        HAPTIC_FEEDBACK_MODE_ON_TICKS,
        HAPTIC_FEEDBACK_MODE_ON_ENDS,
    })
    @interface HapticFeedbackMode {}
    public static final int HAPTIC_FEEDBACK_MODE_NONE = 0;
    public static final int HAPTIC_FEEDBACK_MODE_ON_TICKS = 1 << 0;
    public static final int HAPTIC_FEEDBACK_MODE_ON_ENDS = 1 << 1;

    private Context mContext;

    protected int mInterval = 1;
    protected String mUnits = "";
    protected boolean mContinuousUpdates = false;
    protected boolean mTickVisible;
    protected @HapticFeedbackMode int mHapticFeedbackMode =
        HAPTIC_FEEDBACK_MODE_ON_TICKS | HAPTIC_FEEDBACK_MODE_ON_ENDS;

    protected int mMinValue = 1;
    protected int mMaxValue = 256;
    protected int mDefaultValue;

    protected int mValue;

    protected TextView mValueTextView;
    protected Slider mSlider;

    protected boolean mTrackingTouch = false;
    protected int mTrackingValue;

    // Whether to show the Slider value TextView next to the bar
    private boolean mShowSliderValue;

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
        mTickVisible = mInterval != 0;
        a.recycle();

        a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.ProgressBar, defStyleAttr,
                defStyleRes);
        mMaxValue = a.getInt(com.android.internal.R.styleable.ProgressBar_max, mMaxValue);
        mMinValue = a.getInt(com.android.internal.R.styleable.ProgressBar_min, mMinValue);
        if (mMaxValue < mMinValue)
            mMaxValue = mMinValue;
        a.recycle();

        a = context.obtainStyledAttributes(attrs, androidx.preference.R.styleable.Preference,
                defStyleAttr, defStyleRes);

        mShowSliderValue = a.getBoolean(
                androidx.preference.R.styleable.SeekBarPreference_showSeekBarValue, false);

        final int defaultValueId;
        if (a.hasValue(androidx.preference.R.styleable.Preference_defaultValue)) {
            defaultValueId = androidx.preference.R.styleable.Preference_defaultValue;
        } else {
            defaultValueId = androidx.preference.R.styleable.Preference_android_defaultValue;
        }
        mDefaultValue = a.getInt(defaultValueId, mMinValue);
        mValue = mDefaultValue;
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

        // SeekBarPreference is not clickable under normal conditions.
        holder.itemView.setClickable(false);

        mSlider = (Slider) holder.findViewById(R.id.slider);
        mSlider.setValueFrom(mMinValue);
        mSlider.setValueTo(mMaxValue);
        mSlider.setValue(mValue);
        mSlider.setEnabled(isEnabled());
        mSlider.setClickable(isSelectable());
        if (mInterval != 0) {
            mSlider.setStepSize(mInterval);
            mSlider.setTickVisibilityMode(mTickVisible
                    ? TickVisibilityMode.TICK_VISIBILITY_AUTO_LIMIT
                    : TickVisibilityMode.TICK_VISIBILITY_HIDDEN);
        }
        setHapticFeedbackMode(mHapticFeedbackMode);
        if (mShowSliderValue) {
            mSlider.setLabelBehavior(LabelFormatter.LABEL_FLOATING);
        } else {
            mSlider.setLabelBehavior(LabelFormatter.LABEL_GONE);
        }

        mValueTextView = (TextView) holder.findViewById(R.id.selected_value);

        updateValueViews();

        mSlider.addOnChangeListener(this);
        mSlider.addOnSliderTouchListener(this);
        mSlider.setLabelFormatter((value) -> getTextValue((int) value));
        mValueTextView.setOnClickListener(this);
        mValueTextView.setOnLongClickListener(this);
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
    public void onValueChange(Slider slider, float value, boolean fromUser) {
        int newValue = (int) value;
        if (mTrackingTouch) {
            if ((mHapticFeedbackMode & HAPTIC_FEEDBACK_MODE_ON_ENDS) != 0
                    && (newValue == mMinValue || newValue == mMaxValue)) {
                mSlider.performHapticFeedback(SCROLL_LIMIT);
            } else if ((mHapticFeedbackMode & HAPTIC_FEEDBACK_MODE_ON_TICKS) != 0) {
                mSlider.performHapticFeedback(SEGMENT_FREQUENT_TICK);
            }
        }
        if (mTrackingTouch && !mContinuousUpdates) {
            mTrackingValue = newValue;
            updateValueViews();
        } else if (mValue != newValue) {
            // change rejected, revert to the previous value
            if (!callChangeListener(newValue)) {
                mSlider.setValue(mValue);
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
    public void onStartTrackingTouch(Slider slider) {
        mTrackingValue = mValue;
        mTrackingTouch = true;
    }

    @Override
    public void onStopTrackingTouch(Slider slider) {
        mTrackingTouch = false;
        if (!mContinuousUpdates) {
            onValueChange(mSlider, mTrackingValue, false);
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

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setValue(getPersistedInt(mDefaultValue));
    }

    public void setDefaultValue(int newValue, boolean update) {
        if (mDefaultValue != newValue) {
            mDefaultValue = newValue;
            if (update)
                updateValueViews();
        }
    }

    public void setMax(int max) {
        if (mMaxValue != max) {
            mMaxValue = max;
            if (mSlider != null) {
                mSlider.setValueTo(max);
            }
        }
    }

    public void setMin(int min) {
        if (mMinValue != min) {
            mMinValue = min;
            if (mSlider != null) {
                mSlider.setValueFrom(min);
            }
        }
    }

    public void setValue(int newValue) {
        mValue = newValue;
        if (mSlider != null) {
            mSlider.setValue(mValue);
        }
    }

    public void setValue(int newValue, boolean update) {
        if (mValue != newValue) {
            if (update) {
                if (mSlider != null) {
                    mSlider.setValue(newValue);
                }
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

    public void setShowSliderValue(final boolean showSliderValue) {
        if (showSliderValue != mShowSliderValue) {
            mShowSliderValue = showSliderValue;
            notifyChanged();
        }
    }

    public void setTickVisible(final boolean tickVisible) {
        if (tickVisible != mTickVisible) {
            mTickVisible = tickVisible;
            notifyChanged();
        }
    }

    public void setHapticFeedbackMode(final @HapticFeedbackMode int hapticFeedbackMode) {
        mHapticFeedbackMode = hapticFeedbackMode;
        if (mSlider != null) {
            mSlider.setHapticFeedbackEnabled(hapticFeedbackMode != HAPTIC_FEEDBACK_MODE_NONE);
        }
    }

    // need some methods here to set/get other attrs at runtime,
    // but who really need this ...

    public void refresh(int newValue) {
        // this will ...
        setValue(newValue, mSlider != null);
    }
}
