/*
 * Copyright (C) 2022 The LineageOS Project
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

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.OnMainSwitchChangeListener;

import org.lineageos.settings.R;
import org.lineageos.settings.preferences.SeekBarPreference;
import org.lineageos.settings.PartsUtils;

public class SmartChargingFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnMainSwitchChangeListener {
    private final String TAG = getClass().getName();
    private final boolean DEBUG = false;

    private Context mContext;

    private SharedPreferences mSharedPrefs;

    private MainSwitchPreference mSmartChargingSwitch;
    private SeekBarPreference mSeekBarChargingLimitPreference;
    private SeekBarPreference mSeekBarChargingResumePreference;
    private SeekBarPreference mSeekBarChargingTempPreference;
    private TwoStatePreference mResetStatsPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mContext = getContext();
        mSharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(mContext);

        addPreferencesFromResource(R.xml.smartcharging_settings);

        mSmartChargingSwitch = (MainSwitchPreference) findPreference(SmartCharging.KEY_CHARGING_SWITCH);
        mSmartChargingSwitch.setChecked(mSharedPrefs.getBoolean(SmartCharging.KEY_CHARGING_SWITCH, false));
        mSmartChargingSwitch.addOnSwitchChangeListener(this);

        mSeekBarChargingLimitPreference = findPreference(SmartCharging.KEY_CHARGING_LIMIT);
        mSeekBarChargingLimitPreference.setEnabled(mSmartChargingSwitch.isChecked());
        mSeekBarChargingLimitPreference.setMax(SmartCharging.CHARGING_LIMIT_MAX_DEFAULT);
        mSeekBarChargingLimitPreference.setMin(SmartCharging.CHARGING_LIMIT_MIN_DEFAULT);
        mSeekBarChargingLimitPreference.setDefaultValue(SmartCharging.CHARGING_LIMIT_DEFAULT, false /* update */);
        mSeekBarChargingLimitPreference.setValue(SmartCharging.getChargingLimit(mSharedPrefs), false /* update */);
        mSeekBarChargingLimitPreference.setOnPreferenceChangeListener(this);

        mSeekBarChargingResumePreference = findPreference(SmartCharging.KEY_CHARGING_RESUME);
        mSeekBarChargingResumePreference.setEnabled(mSmartChargingSwitch.isChecked());
        mSeekBarChargingResumePreference.setMax(SmartCharging.CHARGING_RESUME_MAX_DEFAULT);
        mSeekBarChargingResumePreference.setMin(SmartCharging.CHARGING_RESUME_MIN_DEFAULT);
        mSeekBarChargingResumePreference.setDefaultValue(SmartCharging.CHARGING_RESUME_DEFAULT, false /* update */);
        mSeekBarChargingResumePreference.setValue(SmartCharging.getChargingResume(mSharedPrefs), false /* update */);
        mSeekBarChargingResumePreference.setOnPreferenceChangeListener(this);

        mSeekBarChargingTempPreference = findPreference(SmartCharging.KEY_CHARGING_TEMP);
        mSeekBarChargingTempPreference.setEnabled(mSmartChargingSwitch.isChecked());
        mSeekBarChargingTempPreference.setMax(SmartCharging.CHARGING_TEMP_MAX_DEFAULT);
        mSeekBarChargingTempPreference.setMin(SmartCharging.CHARGING_TEMP_MIN_DEFAULT);
        mSeekBarChargingTempPreference.setDefaultValue(SmartCharging.CHARGING_TEMP_DEFAULT, false);
        mSeekBarChargingTempPreference.setValue(SmartCharging.getTempLimit(mSharedPrefs), false /* update */);
        mSeekBarChargingTempPreference.setOnPreferenceChangeListener(this);

        mResetStatsPreference = findPreference(SmartCharging.KEY_RESET_STATS);
        mResetStatsPreference.setChecked(SmartCharging.isResetStatsNeeded(mSharedPrefs));
        mResetStatsPreference.setEnabled(mSmartChargingSwitch.isChecked());
        mResetStatsPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (isChecked) {
            SmartCharging.startService(mContext);
        } else {
            SmartCharging.stopService(mContext);
            SmartCharging.enableCharging();
        }

        mSeekBarChargingLimitPreference.setEnabled(isChecked);
        mSeekBarChargingResumePreference.setEnabled(isChecked);
        mSeekBarChargingTempPreference.setEnabled(isChecked);
        mResetStatsPreference.setEnabled(isChecked);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        switch (key) {
            case SmartCharging.KEY_CHARGING_LIMIT:
            case SmartCharging.KEY_CHARGING_RESUME:
                mSharedPrefs.edit().putInt(key, (int) newValue).apply();
                if (SmartCharging.getChargingLimit(mSharedPrefs) <=
                        SmartCharging.getChargingResume(mSharedPrefs)) {
                    String message = mContext.getString(R.string.smart_charging_warning);
                    PartsUtils.createToast(mContext, message);
                }
                break;
            case SmartCharging.KEY_CHARGING_TEMP:
                mSharedPrefs.edit().putInt(key, (int) newValue).apply();
                break;
            case SmartCharging.KEY_RESET_STATS:
                mSharedPrefs.edit().putBoolean(key, (Boolean) newValue).apply();
                break;
            default:
                throw new RuntimeException("Cannot store undefined preference: " + key);
        }

        if (SmartCharging.isPlugged()) {
            SmartChargingService.update(mSharedPrefs);
        }

        return true;
    }
}
