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
import android.util.Log;

import androidx.preference.PreferenceManager;
import java.lang.IllegalArgumentException;

import org.lineageos.settings.PartsUtils;

public class SmartCharging {
    private static final String TAG = "SmartCharging";
    private static final boolean DEBUG = false;

    public static final String KEY_CHARGING_SWITCH = "smart_charging";
    public static final String KEY_CHARGING_LIMIT = "seek_bar_limit";
    public static final String KEY_CHARGING_RESUME = "seek_bar_resume";
    public static final String KEY_CHARGING_TEMP = "seek_bar_temp";
    public static final String KEY_RESET_STATS = "reset_stats";

    public static final String CHARGING_ENABLED_PATH = "/sys/class/power_supply/battery/charging_enabled";
    public static final String CHARGER_PRESENT_PATH = "/sys/class/power_supply/usb/present";
    public static final String BATTERY_CAPACITY_PATH = "/sys/class/power_supply/battery/capacity";
    public static final String BATTERY_TEMPERATURE_PATH = "/sys/class/power_supply/battery/temp";

    public static final int CHARGING_LIMIT_DEFAULT = 80;
    public static final int CHARGING_LIMIT_MAX_DEFAULT = 100;
    public static final int CHARGING_LIMIT_MIN_DEFAULT = 65;
    public static final int CHARGING_RESUME_DEFAULT = 60;
    public static final int CHARGING_RESUME_MAX_DEFAULT = 99;
    public static final int CHARGING_RESUME_MIN_DEFAULT = 15;
    public static final int CHARGING_TEMP_DEFAULT = 35;
    public static final int CHARGING_TEMP_MAX_DEFAULT = 50;
    public static final int CHARGING_TEMP_MIN_DEFAULT = 10;

    private Context mContext;

    public SmartCharging(final Context context) {
        mContext = context;
    }

    public void onBootCompleted() {
        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean enabled = sharedPrefs.getBoolean(KEY_CHARGING_SWITCH, false);
        if (enabled) {
            startService(mContext);
        }
        if (DEBUG) Log.d(TAG, "Started. Service is enabled: " + Boolean.valueOf(enabled).toString());
    }

    public static void startService(final Context context) {
        PartsUtils.startService(context, SmartChargingService.class);
    }

    public static void stopService(final Context context) {
        PartsUtils.stopService(context, SmartChargingService.class);
    }

    public static void enableCharging() {
        PartsUtils.writeValue(CHARGING_ENABLED_PATH, "1");
    }

    public static void disableCharging() {
        PartsUtils.writeValue(CHARGING_ENABLED_PATH, "0");
    }

    public static float getBatteryTemp() {
        String raw = PartsUtils.readLine(BATTERY_TEMPERATURE_PATH);
        return ((float) Integer.parseInt(raw == null ? "0" : raw)) / 10;
    }

    public static int getBatteryCapacity() {
        String raw = PartsUtils.readLine(BATTERY_CAPACITY_PATH);
        return Integer.parseInt(raw == null ? "0" : raw);
    }

    public static boolean isChargingEnabled() {
        String raw = PartsUtils.readLine(CHARGING_ENABLED_PATH);
        return raw.equals("1");
    }

    public static boolean isPlugged() {
        String raw = PartsUtils.readLine(CHARGER_PRESENT_PATH);
        return raw.equals("1");
    }

    public static int getChargingLimit(final SharedPreferences sharedPrefs) {
        return sharedPrefs.getInt(KEY_CHARGING_LIMIT, CHARGING_LIMIT_DEFAULT);
    }

    public static int getChargingResume(final SharedPreferences sharedPrefs) {
        return sharedPrefs.getInt(KEY_CHARGING_RESUME, CHARGING_RESUME_DEFAULT);
    }

    public static int getTempLimit(final SharedPreferences sharedPrefs) {
        return sharedPrefs.getInt(KEY_CHARGING_TEMP, CHARGING_TEMP_DEFAULT);
    }

    public static boolean isResetStatsNeeded(final SharedPreferences sharedPrefs) {
        return sharedPrefs.getBoolean(KEY_RESET_STATS, false);
    }
}
