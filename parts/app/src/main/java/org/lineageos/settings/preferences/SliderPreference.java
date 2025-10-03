package org.lineageos.settings.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

public class SliderPreference extends com.android.settingslib.widget.SliderPreference {
    private CharSequence mTextEnd;

    public SliderPreference(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }

    /**
     * Constructor that is called when inflating a preference from XML. This is called when a
     * preference is being constructed from an XML file, supplying attributes that were specified
     * in the XML file. This version uses a default style of 0, so the only attribute values
     * applied are those in the Context's Theme and the given AttributeSet.
     *
     * @param context The Context this is associated with, through which it can access the
     *                current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the preference
     */
    public SliderPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0 /* defStyleAttr */);
    }

    /**
     * Constructor to create a slider preference.
     *
     * @param context The Context this is associated with, through which it can access the
     *                current theme, resources, etc.
     */
    public SliderPreference(@NonNull Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        TextView endText = (TextView) holder.findViewById(android.R.id.text2);
        if (endText != null && mTextEnd != null) {
            endText.setText(mTextEnd);
        }
    }

    public void setEndText(final @Nullable CharSequence text) {
        mTextEnd = text == null ? "" : text;
    }
}
