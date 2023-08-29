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

import android.content.SharedPreferences;
import android.os.SystemProperties;

import javax.inject.Inject;

import org.lineageos.settings.PartsUtils;

public class SmartCharging {
    private static final String TAG = "SmartCharging";

    static final String PROP_LAST_STOP_CHARGING_REASON =
        "service.smartcharging.last_stop_charging_reason";

    static final String PROP_NEXT_BATTERY_MONITOR_TRIGGER_TIME =
        "service.smartcharging.next_battery_monitor_trigger_time";

    static final String BATTERY_MONITOR_UPDATE_INTENT = "smartcharging.BatteryMonitorUpdateIntent";

    // Prefs
    static final String KEY_CHARGING_SWITCH = "smart_charging";
    static final String KEY_CHARGING_LIMIT = "seek_bar_limit";
    static final String KEY_CHARGING_RESUME = "seek_bar_resume";
    static final String KEY_CHARGING_TEMP = "seek_bar_temp";
    static final String KEY_CHARGING_CURRENT_MAX = "current_max_pref";
    static final String KEY_RESET_STATS = "reset_stats";

    // Sysfs nodes
    static final String CHARGING_ENABLED_PATH = "/sys/class/power_supply/battery/charging_enabled";
    static final String CHARGING_CURRENT_MAX_PATH = "/sys/class/power_supply/usb/current_max";
    static final String CHARGER_PRESENT_PATH = "/sys/class/power_supply/usb/present";
    static final String BATTERY_CAPACITY_PATH = "/sys/class/power_supply/battery/capacity";
    static final String BATTERY_TEMPERATURE_PATH = "/sys/class/power_supply/battery/temp";

    // Charging defaults
    static final int CHARGING_LIMIT_DEFAULT = 80;
    static final int CHARGING_LIMIT_MAX_DEFAULT = 100;
    static final int CHARGING_LIMIT_MIN_DEFAULT = 65;
    static final int CHARGING_RESUME_DEFAULT = 60;
    static final int CHARGING_RESUME_MAX_DEFAULT = 99;
    static final int CHARGING_RESUME_MIN_DEFAULT = 15;
    static final int CHARGING_TEMP_DEFAULT = 35;
    static final int CHARGING_TEMP_MAX_DEFAULT = 50;
    static final int CHARGING_TEMP_MIN_DEFAULT = 10;
    // NOTE: negative current is handled at kernel driver level
    static final String CHARGING_CURRENT_MAX_DEFAULT = "-1";

    private final SharedPreferences mSharedPrefs;

    @Inject
    public SmartCharging(final SharedPreferences sharedPrefs) {
        mSharedPrefs = sharedPrefs;
    }

    boolean isEnabled() {
        return mSharedPrefs.getBoolean(KEY_CHARGING_SWITCH, false);
    }

    @SmartChargingStopReason int getLastStopChargingReason() {
        return SystemProperties.getInt(PROP_LAST_STOP_CHARGING_REASON,
                SmartChargingStopReason.UNKNOWN);
    }

    boolean isBatteryResetStatsNeeded() {
        return mSharedPrefs.getBoolean(KEY_RESET_STATS, false);
    }

    long getBatteryMonitorNextUpdateTime() {
        return SystemProperties.getLong(PROP_NEXT_BATTERY_MONITOR_TRIGGER_TIME, 0);
    }

    float getBatteryTemp() {
        final String raw = PartsUtils.readLine(BATTERY_TEMPERATURE_PATH);
        return ((float) Integer.parseInt(raw == null ? "0" : raw)) / 10;
    }

    int getBatteryCapacity() {
        final String raw = PartsUtils.readLine(BATTERY_CAPACITY_PATH);
        return Integer.parseInt(raw == null ? "0" : raw);
    }

    boolean isPlugged() {
        final String raw = PartsUtils.readLine(CHARGER_PRESENT_PATH);
        return raw.equals("1");
    }

    int getChargingLimit() {
        return mSharedPrefs.getInt(KEY_CHARGING_LIMIT, CHARGING_LIMIT_DEFAULT);
    }

    int getChargingResume() {
        return mSharedPrefs.getInt(KEY_CHARGING_RESUME, CHARGING_RESUME_DEFAULT);
    }

    int getTempLimit() {
        return mSharedPrefs.getInt(KEY_CHARGING_TEMP, CHARGING_TEMP_DEFAULT);
    }

    boolean isChargingEnabled() {
        final String raw = PartsUtils.readLine(CHARGING_ENABLED_PATH);
        return raw.equals("1");
    }

    String getCurrentMax() {
        return mSharedPrefs.getString(KEY_CHARGING_CURRENT_MAX, CHARGING_CURRENT_MAX_DEFAULT);
    }
}
