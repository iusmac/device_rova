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

package org.lineageos.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.provider.DeviceConfig;
import android.provider.DeviceConfig.Properties;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.android.settingslib.development.DevelopmentSettingsEnabler;

import lineageos.providers.LineageSettings;

public class DefaultSystemSettings {
    private static final String TAG = "DefaultSystemSettings";
    private static final boolean DEBUG = false;

    private Context mContext;
    private SharedPreferences mSharedPrefs;

    public DefaultSystemSettings(final Context context) {
        mContext = context;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private boolean isFirstRun(String key) {
        boolean isFirstRun = mSharedPrefs.getBoolean(key, true);
        if (isFirstRun) {
            mSharedPrefs.edit().putBoolean(key, false).apply();
        }
        return isFirstRun;
    }

    private void saveFirstRun(String key) {
        mSharedPrefs.edit().putBoolean(key, true).apply();
    }

    public void onBootCompleted() {
        if (isFirstRun("disable-nav-keys")) {
            writeDisableNavkeysOption(true);
        }

        if (isFirstRun("enable-battery-light")) {
            writeBatteryLightOption(true);
        }

        if (isFirstRun("enable-dt2w")) {
            writeDt2wOption(true);
        }

        if (isFirstRun("enable-auto-brightness")) {
            writeAutoBrightnessOption(true);
        }

        if (isFirstRun("set-kg-custom-clock-top-margin-to-130")) {
            Settings.Secure.putIntForUser(mContext.getContentResolver(),
                    "kg_custom_clock_top_margin", 130, UserHandle.USER_CURRENT);
        }

        if (isFirstRun("set-kg-small-clock-text-size-to-68")) {
            Settings.Secure.putIntForUser(mContext.getContentResolver(),
                    "kg_small_clock_text_size", 68, UserHandle.USER_CURRENT);
        }

        if (isFirstRun("set-kg-large-clock-text-size-to-120")) {
            Settings.Secure.putIntForUser(mContext.getContentResolver(),
                    "kg_large_clock_text_size", 120, UserHandle.USER_CURRENT);
        }

        tweakActivityManagerSettings();
        writeAnimationSettings();
    }

    private void writeDisableNavkeysOption(final boolean enabled) {
        final boolean virtualKeysEnabled = LineageSettings.System.getIntForUser(
                mContext.getContentResolver(), LineageSettings.System.FORCE_SHOW_NAVBAR, 0,
                UserHandle.USER_CURRENT) != 0;
        if (enabled != virtualKeysEnabled) {
            LineageSettings.System.putIntForUser(mContext.getContentResolver(),
                    LineageSettings.System.FORCE_SHOW_NAVBAR, enabled ? 1 : 0,
                    UserHandle.USER_CURRENT);
        }
    }

    private void writeBatteryLightOption(final boolean enabled) {
        final boolean isBatteryLightEnabled = LineageSettings.System.getIntForUser(
                mContext.getContentResolver(), LineageSettings.System.BATTERY_LIGHT_ENABLED, 0,
                UserHandle.USER_CURRENT) != 0;
        if (enabled != isBatteryLightEnabled) {
            LineageSettings.System.putIntForUser(mContext.getContentResolver(),
                    LineageSettings.System.BATTERY_LIGHT_ENABLED, enabled ? 1 : 0,
                    UserHandle.USER_CURRENT);
        }
    }

    private void writeDt2wOption(final boolean enabled) {
        final boolean isDt2wEnabled = Settings.Secure.getIntForUser(
                mContext.getContentResolver(), Settings.Secure.DOUBLE_TAP_TO_WAKE, 0,
                UserHandle.USER_CURRENT) != 0;
        if (enabled != isDt2wEnabled) {
            Settings.Secure.putIntForUser(mContext.getContentResolver(),
                    Settings.Secure.DOUBLE_TAP_TO_WAKE, enabled ? 1 : 0,
                    UserHandle.USER_CURRENT);
        }
    }

    private void writeAutoBrightnessOption(final boolean enabled) {
        final int manualValue = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        final boolean isAutoBrightnessEnabled = Settings.System.getIntForUser(
                mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, manualValue,
                UserHandle.USER_CURRENT) != manualValue;
        if (enabled != isAutoBrightnessEnabled) {
            final int autoValue = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    enabled ? autoValue : manualValue,
                    UserHandle.USER_CURRENT);
        }
    }

    private void runCmd(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd);
            Thread.sleep(1000);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void tweakActivityManagerSettings() {
        final Properties properties = new Properties.Builder("activity_manager")
                .setInt("max_cached_processes", 96)
                .setInt("max_phantom_processes", 2147483647)
                .setBoolean("use_compaction", true)
                .setInt("compact_action_1", 2)
                .setInt("compact_action_2", 2)
                .setBoolean("use_oom_re_ranking", true)
                .setString("imperceptible_kill_exempt_proc_states", "0,1,2,4,12,14")
                .build();

        try {
            DeviceConfig.setProperties(properties);
        } catch( Exception e) {
            e.printStackTrace();
        }
    }

    private void writeAnimationSettings() {
        final String[] toggleAnimationTargets = {
            Settings.Global.WINDOW_ANIMATION_SCALE,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            Settings.Global.ANIMATOR_DURATION_SCALE
        };

        final String animationOffValue = "0";

        boolean allAnimationsDisabled = true;
        for (String animationSetting : toggleAnimationTargets) {
            final String currentAnimationValue = Settings.Global.getString(
                    mContext.getContentResolver(), animationSetting);
            if (!animationOffValue.equals(currentAnimationValue)) {
                allAnimationsDisabled = false;
                break;
            }
        }

        boolean canSetAnimationValues = true;

        // Respect "Accessibility -> Remove animations" option
        canSetAnimationValues &= !allAnimationsDisabled;
        // Respect "Developer options" preference
        canSetAnimationValues &= !DevelopmentSettingsEnabler
            .isDevelopmentSettingsEnabled(mContext);

        if (canSetAnimationValues) {
            for (String animationSetting : toggleAnimationTargets) {
                Settings.Global.putString(mContext.getContentResolver(),
                        animationSetting, "0.7");
            }
        }
    }
}
