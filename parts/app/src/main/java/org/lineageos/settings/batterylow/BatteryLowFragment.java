package org.lineageos.settings.batterylow;

import android.os.Bundle;

import androidx.preference.Preference;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import dagger.hilt.android.AndroidEntryPoint;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;
import org.lineageos.settings.preferences.SliderPreference;

import javax.inject.Inject;

@AndroidEntryPoint(SettingsBasePreferenceFragment.class)
public class BatteryLowFragment extends Hilt_BatteryLowFragment {
    @Inject
    BatteryLow mBatteryLow;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.batterylow_settings);

        final SliderPreference preference = (SliderPreference)
            findPreference(mBatteryLow.getSeekBarPreferenceKey());
        preference.setTickVisible(true);
        preference.setEndText(PartsUtils.formatPercentage(preference.getMax()));
        preference.setHapticFeedbackMode(SliderPreference.HAPTIC_FEEDBACK_MODE_ON_ENDS);
        preference.setLabelFormater((level) -> PartsUtils.formatPercentage((int) level));
        preference.setOnPreferenceChangeListener((pref, newValue) -> {
            final int level = (Integer) newValue;
            switch (level) {
                case 0 -> mBatteryLow.stopService();
                default -> mBatteryLow.startService();
            }
            return true;
        });
    }
}
