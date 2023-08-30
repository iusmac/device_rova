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
import android.content.res.Resources;
import android.os.SystemProperties;

import dagger.hilt.android.qualifiers.ApplicationContext;

import javax.inject.Inject;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

public class SmartCharging {
    private static final String TAG = "SmartCharging";

    private final SharedPreferences mSharedPrefs;

    private final Resources mResources;

    @Inject
    public SmartCharging(final @ApplicationContext Context context,
            final SharedPreferences sharedPrefs) {

        mSharedPrefs = sharedPrefs;

        mResources = context.getResources();
    }

    boolean isEnabled() {
        return mSharedPrefs.getBoolean(mResources.getString(
                    R.string.smart_charging_key_main_switch), false);
    }

    @SmartChargingStopReason int getLastStopChargingReason() {
        return SystemProperties.getInt(mResources.getString(
                    R.string.smart_charging_prop_last_stop_charging_reason),
                SmartChargingStopReason.UNKNOWN);
    }

    boolean isBatteryResetStatsNeeded() {
        return mSharedPrefs.getBoolean(mResources.getString(
                    R.string.smart_charging_key_reset_stats), false);
    }

    long getBatteryMonitorNextUpdateTime() {
        return SystemProperties.getLong(mResources.getString(
                    R.string.smart_charging_prop_next_battery_monitor_trigger_time), 0);
    }

    float getBatteryTemp() {
        final String raw = PartsUtils.readLine(mResources.getString(
                    R.string.smart_charging_sysfs_battery_temperature_path));
        return ((float) Integer.parseInt(raw == null ? "0" : raw)) / 10;
    }

    int getBatteryCapacity() {
        final String raw = PartsUtils.readLine(mResources.getString(
                    R.string.smart_charging_sysfs_battery_capacity_path));
        return Integer.parseInt(raw == null ? "0" : raw);
    }

    boolean isPlugged() {
        final String raw = PartsUtils.readLine(mResources.getString(
                    R.string.smart_charging_sysfs_charger_present_path));
        return raw.equals("1");
    }

    int getChargingLimit() {
        return mSharedPrefs.getInt(mResources.getString(
                    R.string.smart_charging_key_charging_limit),
                mResources.getInteger(R.integer.smart_charging_charging_limit_default_value));
    }

    int getChargingResume() {
        return mSharedPrefs.getInt(mResources.getString(
                    R.string.smart_charging_key_charging_resume),
                mResources.getInteger(R.integer.smart_charging_charging_resume_default_value));
    }

    int getTempLimit() {
        return mSharedPrefs.getInt(mResources.getString(R.string.smart_charging_key_charging_temp),
                mResources.getInteger(R.integer.smart_charging_charging_temp_default_value));
    }

    boolean isChargingEnabled() {
        final String raw = PartsUtils.readLine(mResources.getString(
                    R.string.smart_charging_sysfs_charging_enabled_path));
        return raw.equals("1");
    }

    String getCurrentMax() {
        return mSharedPrefs.getString(mResources.getString(
                    R.string.smart_charging_key_charging_current_max), mResources.getString(
                        R.string.smart_charging_current_default_value));
    }
}
