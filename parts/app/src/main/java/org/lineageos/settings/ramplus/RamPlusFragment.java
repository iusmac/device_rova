/*
 * Copyright (C) 2026 iusmac <iusico.maxim@libero.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.lineageos.settings.ramplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.collection.SparseArrayCompat;
import androidx.core.text.HtmlCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import dagger.hilt.android.AndroidEntryPoint;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;
import org.lineageos.settings.preferences.SliderPreference;

import javax.inject.Inject;

@AndroidEntryPoint(SettingsBasePreferenceFragment.class)
public class RamPlusFragment extends Hilt_RamPlusFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int[] SLIDER_PERCENTAGE_PREF_KEY_RESIDS = {
        R.string.ramplus_slider_slight_mode_percentage_key,
        R.string.ramplus_slider_moderate_mode_percentage_key,
        R.string.ramplus_slider_extreme_mode_percentage_key
    };

    private final SparseArrayCompat<RamPlusMode> mSliderModeMap = new SparseArrayCompat<>(3);
    private SliderPreference mModePref;

    @Inject
    RamPlusManager mRamPlusManager;

    @Inject
    SharedPreferences mSharedPrefs;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        final var res = getResources();
        mSliderModeMap.put(res.getInteger(R.integer.ramplus_slider_slight_mode_index),
                RamPlusMode.SLIGHT);
        mSliderModeMap.put(res.getInteger(R.integer.ramplus_slider_moderate_mode_index),
                RamPlusMode.MODERATE);
        mSliderModeMap.put(res.getInteger(R.integer.ramplus_slider_extreme_mode_index),
                RamPlusMode.EXTREME);
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.ramplus_settings);

        final var res = getResources();
        mModePref = (SliderPreference) findPreference(res.getString(R.string.ramplus_key_mode));
        mModePref.setTickVisible(true);
        mModePref.setHapticFeedbackMode(SliderPreference.HAPTIC_FEEDBACK_MODE_ON_TICKS);
        mModePref.setOnPreferenceChangeListener((pref, newValue) -> {
            final int modeIdx = (Integer) newValue;
            final var mode = mSliderModeMap.get(modeIdx);
            if (mode == null) {
                throw new IllegalArgumentException("Unhandled mode index: " + modeIdx);
            }
            mRamPlusManager.setMode(mode);
            return true;
        });
        // Since we're not persisting the preference, default to the current mode value
        mModePref.setDefaultValue(getCurrentSliderMode());

        for (final var prefKey : SLIDER_PERCENTAGE_PREF_KEY_RESIDS) {
            final var pref = (SliderPreference) findPreference(res.getString(prefKey));
            pref.setTickVisible(true);
            pref.setHapticFeedbackMode(SliderPreference.HAPTIC_FEEDBACK_MODE_ON_TICKS);
            pref.setStartText(PartsUtils.formatPercentage(pref.getMin() * 10));
            pref.setEndText(PartsUtils.formatPercentage(pref.getMax() * 10));
            pref.setLabelFormater((level) -> PartsUtils.formatPercentage(((int) level) * 10));
        }

        final var aboutPref = (Preference) findPreference("footer_preference");
        aboutPref.setTitle(HtmlCompat.fromHtml(getString(R.string.ramplus_about),
                    HtmlCompat.FROM_HTML_MODE_COMPACT));
    }

    private int getCurrentSliderMode() {
        return mSliderModeMap.keyAt(mSliderModeMap.indexOfValue(mRamPlusManager.getCurrentMode()));
    }

    private void updateModePreference() {
        final var res = getResources();
        final var modeIdx = mRamPlusManager.getCurrentMode().ordinal();
        final var icons = res.obtainTypedArray(R.array.ramplus_mode_icons);
        final int iconResId = icons.getResourceId(modeIdx, -1);
        icons.recycle();
        // Unconditionally refresh the mode icon due to a bug in SliderPreference.setIconStart
        final var pos = ((PreferenceGroupAdapter) getListView().getAdapter())
            .getPreferenceAdapterPosition(mModePref);
        final var holder = (PreferenceViewHolder)
            getListView().findViewHolderForAdapterPosition(pos);
        if (holder != null) {
            final var iconStartView = (ImageView) holder.findViewById(
                    com.android.settingslib.widget.preference.slider.R.id.icon_start);
            if (iconStartView != null) {
                getListView().post(() -> getListView().getItemAnimator().isRunning(() ->
                            iconStartView.setImageResource(iconResId)));
            }
        } else { // lazily initialize when not laid out yet
            mModePref.setIconStart(iconResId);
        }

        final var names = res.obtainTypedArray(R.array.ramplus_mode_names);
        final int nameResId = names.getResourceId(modeIdx, -1);
        names.recycle();
        mModePref.setIconStartContentDescription(nameResId);
        mModePref.setSummary(nameResId);
        mModePref.setValue(getCurrentSliderMode());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final var res = getResources();
        if (key.equals(res.getString(R.string.ramplus_key_mode))) {
            mRamPlusManager.refreshTileService();
            updateModePreference();
        } else if (key.equals(res.getString(R.string.ramplus_slider_slight_mode_percentage_key))
                || key.equals(res.getString(R.string.ramplus_slider_moderate_mode_percentage_key))
                || key.equals(res.getString(R.string.ramplus_slider_extreme_mode_percentage_key))) {
            // Reapply the current mode to pick up the new mode percentage
            mRamPlusManager.setMode(mRamPlusManager.getCurrentMode());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateModePreference();
        mRamPlusManager.requestAddTileService();

        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}
