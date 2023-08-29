/*
 * Copyright (C) 2023 The LineageOS Project
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

package org.lineageos.settings.smartcharging;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.OnMainSwitchChangeListener;

import dagger.Lazy;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

import org.lineageos.settings.R;
import org.lineageos.settings.preferences.SeekBarPreference;
import org.lineageos.settings.PartsUtils;

import static org.lineageos.settings.BuildConfig.DEBUG;

@AndroidEntryPoint(PreferenceFragmentCompat.class)
public class SmartChargingFragment extends Hilt_SmartChargingFragment implements
        Preference.OnPreferenceChangeListener, OnMainSwitchChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = getClass().getName();

    @Inject
    Lazy<SmartCharging> mSmartChargingLazy;

    @Inject
    Lazy<SmartChargingManager> mSmartChargingManagerLazy;

    private MainSwitchPreference mSmartChargingSwitch;
    private SeekBarPreference mSeekBarChargingLimitPreference;
    private SeekBarPreference mSeekBarChargingResumePreference;
    private SeekBarPreference mSeekBarChargingTempPreference;
    private ListPreference mChargingCurrentMaxListPref;
    private TwoStatePreference mResetStatsPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.smartcharging_settings);

        mSmartChargingSwitch = (MainSwitchPreference) findPreference(SmartCharging.KEY_CHARGING_SWITCH);
        mSmartChargingSwitch.addOnSwitchChangeListener(this);

        mSeekBarChargingLimitPreference = findPreference(SmartCharging.KEY_CHARGING_LIMIT);
        mSeekBarChargingLimitPreference.setEnabled(mSmartChargingSwitch.isChecked());
        mSeekBarChargingLimitPreference.setMax(SmartCharging.CHARGING_LIMIT_MAX_DEFAULT);
        mSeekBarChargingLimitPreference.setMin(SmartCharging.CHARGING_LIMIT_MIN_DEFAULT);
        mSeekBarChargingLimitPreference.setDefaultValue(SmartCharging.CHARGING_LIMIT_DEFAULT, false /* update */);
        mSeekBarChargingLimitPreference.setOnPreferenceChangeListener(this);

        mSeekBarChargingResumePreference = findPreference(SmartCharging.KEY_CHARGING_RESUME);
        mSeekBarChargingResumePreference.setEnabled(mSmartChargingSwitch.isChecked());
        mSeekBarChargingResumePreference.setMax(SmartCharging.CHARGING_RESUME_MAX_DEFAULT);
        mSeekBarChargingResumePreference.setMin(SmartCharging.CHARGING_RESUME_MIN_DEFAULT);
        mSeekBarChargingResumePreference.setDefaultValue(SmartCharging.CHARGING_RESUME_DEFAULT, false /* update */);
        mSeekBarChargingResumePreference.setOnPreferenceChangeListener(this);

        mSeekBarChargingTempPreference = findPreference(SmartCharging.KEY_CHARGING_TEMP);
        mSeekBarChargingTempPreference.setEnabled(mSmartChargingSwitch.isChecked());
        mSeekBarChargingTempPreference.setMax(SmartCharging.CHARGING_TEMP_MAX_DEFAULT);
        mSeekBarChargingTempPreference.setMin(SmartCharging.CHARGING_TEMP_MIN_DEFAULT);
        mSeekBarChargingTempPreference.setDefaultValue(SmartCharging.CHARGING_TEMP_DEFAULT, false /* update */);
        mSeekBarChargingTempPreference.setOnPreferenceChangeListener(this);

        mChargingCurrentMaxListPref = findPreference(SmartCharging.KEY_CHARGING_CURRENT_MAX);
        mChargingCurrentMaxListPref.setEnabled(mSmartChargingSwitch.isChecked());
        mChargingCurrentMaxListPref.setDefaultValue(SmartCharging.CHARGING_CURRENT_MAX_DEFAULT);
        mChargingCurrentMaxListPref.setOnPreferenceChangeListener(this);

        mResetStatsPreference = findPreference(SmartCharging.KEY_RESET_STATS);
        mResetStatsPreference.setEnabled(mSmartChargingSwitch.isChecked());
        mResetStatsPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (isChecked) {
            mSmartChargingManagerLazy.get().enable();
        } else {
            mSmartChargingManagerLazy.get().disable();
        }

        mSeekBarChargingLimitPreference.setEnabled(isChecked);
        mSeekBarChargingResumePreference.setEnabled(isChecked);
        mSeekBarChargingTempPreference.setEnabled(isChecked);
        mChargingCurrentMaxListPref.setEnabled(isChecked);
        mResetStatsPreference.setEnabled(isChecked);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        switch (key) {
            case SmartCharging.KEY_CHARGING_LIMIT:
            case SmartCharging.KEY_CHARGING_RESUME:
                int chargingLimit = mSmartChargingLazy.get().getChargingLimit();
                int chargingResume = mSmartChargingLazy.get().getChargingResume();

                if (key.equals(SmartCharging.KEY_CHARGING_LIMIT)) {
                    chargingLimit = (int) newValue;
                } else {
                    chargingResume = (int) newValue;
                }

                if (chargingLimit <= chargingResume) {
                    PartsUtils.createToast(getActivity(),
                            getString(R.string.smart_charging_warning));
                    return false;
                }
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPrefs, final String key) {
        switch (key) {
            case SmartCharging.KEY_CHARGING_LIMIT:
            case SmartCharging.KEY_CHARGING_RESUME:
            case SmartCharging.KEY_CHARGING_TEMP:
            case SmartCharging.KEY_CHARGING_CURRENT_MAX:
            case SmartCharging.KEY_RESET_STATS:
                mSmartChargingManagerLazy.get().onPreferenceUpdate(key);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
    }
}
