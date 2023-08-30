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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.Log;

import dagger.hilt.android.qualifiers.ApplicationContext;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

import static org.lineageos.settings.BuildConfig.DEBUG;

public final class SmartChargingManager {
    private static final String TAG = "SmartChargingManager";

    private final Context mContext;
    private final SharedPreferences mSharedPrefs;
    private final Provider<AlarmManager> mAlarmManagerProvider;
    private final SmartChargingNotificationManager mSmartChargingNotificationManager;
    private final SmartCharging mSmartCharging;

    private final Resources mResources;

    @Inject
    public SmartChargingManager(final @ApplicationContext Context context,
            final SharedPreferences sharedPrefs,
            final Provider<AlarmManager> alarmManagerProvider,
            final SmartChargingNotificationManager smartChargingNotificationManager,
            final SmartCharging smartCharging) {

        mContext = context;
        mSharedPrefs = sharedPrefs;
        mAlarmManagerProvider = alarmManagerProvider;
        mSmartChargingNotificationManager = smartChargingNotificationManager;
        mSmartCharging = smartCharging;

        mResources = context.getResources();
    }

    public void onBootCompleted() {
        final boolean enabled = mSmartCharging.isEnabled();
        if (DEBUG) Log.d(TAG, "onBootCompleted() : enabled: " + enabled);
        if (enabled) {
            enable();
        }
    }

    void onPowerConnected() {
        if (DEBUG) Log.d(TAG, "Charger/USB Connected");

        if (mSmartCharging.getLastStopChargingReason() == SmartChargingStopReason.NOTIFICATION) {
            // User suspended monitoring for this session, but charging resumed. We need to reset
            // last stop charging reason here, so that the next charger re-plug can start the
            // monitoring as expected
            setLastStopChargingReason(SmartChargingStopReason.UNKNOWN);
        } else {
            startBatteryMonitoring();
        }
    }

    void onPowerDisconnected() {
        if (DEBUG) Log.d(TAG, "Charger/USB Disconnected");

        final @SmartChargingStopReason int lastStopChargingReason =
            mSmartCharging.getLastStopChargingReason();

        final boolean isPreviouslyOverheated =
            lastStopChargingReason == SmartChargingStopReason.OVERHEATED;
        final boolean isPreviouslyOvercharged =
            lastStopChargingReason == SmartChargingStopReason.OVERCHARGED;

        // Stop now if there's no reason to monitor the battery
        if (!isPreviouslyOverheated && !isPreviouslyOvercharged) {
            stopBatteryMonitoring(SmartChargingStopReason.UNKNOWN);
            return;
        }

        // Ignore dismissed state and show notification again to ensure the user doesn't get
        // confused when he discover the device isn't charging even after re-plugging the power
        // cable
        if (mSmartChargingNotificationManager.isNotificationDismissed()) {
            mSmartChargingNotificationManager.setNotificationDismissed(false);
            mSmartChargingNotificationManager.showNotification();
        }

        final boolean isCharged = mSmartCharging.getChargingLimit() <=
            mSmartCharging.getBatteryCapacity();
        if (isCharged && isPreviouslyOvercharged && mSmartCharging.isBatteryResetStatsNeeded()) {
            resetBatteryStats();
        }
    }

    void onBatteryUpdate() {
        if (DEBUG) Log.d(TAG, "onBatteryUpdate().");

        if (!mSmartCharging.isPlugged()) {
            if (DEBUG) Log.d(TAG, "Charger/USB Unplugged");
            stopBatteryMonitoring(SmartChargingStopReason.UNKNOWN);
        } else {
            startBatteryMonitoring();
        }
    }

    void onNotificationDismiss() {
        if (DEBUG) Log.d(TAG, "onNotificationDismiss().");

        mSmartChargingNotificationManager.setNotificationDismissed(true);
    }

    void onNotificationNotNowAction() {
        if (DEBUG) Log.d(TAG, "onNotificationNotNowAction().");

        stopBatteryMonitoring(SmartChargingStopReason.NOTIFICATION);
    }

    void onPreferenceUpdate(final String which) {
        if (DEBUG) Log.d(TAG, String.format("onPreferenceUpdate(which=%s).", which));

        if (which.equals(mResources.getString(R.string.smart_charging_key_charging_temp))) {
            // If the battery was previously overheated, but the user wants a different max
            // battery temperature threshold, then we must reset the last stop charging reason
            // to ensure that the new value is picked up when reaching the battery cooling
            // control logic
            if (mSmartCharging.getLastStopChargingReason() ==
                    SmartChargingStopReason.OVERHEATED) {
                setLastStopChargingReason(SmartChargingStopReason.UNKNOWN);
            }
        }

        if (mSmartCharging.isPlugged()) {
            startBatteryMonitoring();
        }
    }

    void enable() {
        if (DEBUG) Log.d(TAG, "enable().");

        PartsUtils.startService(mContext, SmartChargingService.class);

        if (mSmartCharging.isPlugged()) {
            startBatteryMonitoring();
        }
    }

    void disable() {
        if (DEBUG) Log.d(TAG, "disable().");

        stopBatteryMonitoring(SmartChargingStopReason.UNKNOWN);
        PartsUtils.stopService(mContext, SmartChargingService.class);
    }

    private void startBatteryMonitoring() {
        if (DEBUG) Log.d(TAG, "startBatteryMonitoring().");

        final long nextTriggerTime = getNextBatteryMonitoringTriggerTime();
        mAlarmManagerProvider.get().setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                nextTriggerTime, getAlarmPendingIntent());
        SystemProperties.set(mResources.getString(
                    R.string.smart_charging_prop_next_battery_monitor_trigger_time),
                String.valueOf(nextTriggerTime));

        reevaluate();

        if (!mSmartChargingNotificationManager.isNotificationDismissed()) {
            mSmartChargingNotificationManager.showNotification();
        }
    }

    private void stopBatteryMonitoring(final @SmartChargingStopReason int reason) {
        if (DEBUG) Log.d(TAG, String.format("stopBatteryMonitoring(reason=%d).", reason));

        mAlarmManagerProvider.get().cancel(getAlarmPendingIntent());
        mSmartChargingNotificationManager.removeNotification();
        setLastStopChargingReason(reason);
        setChargingEnabled(true);
        setMaxChargingCurrent(mResources.getString(R.string.smart_charging_current_default_value));
    }

    private void reevaluate() {
        if (DEBUG) Log.d(TAG, "reevaluate().");

        final @SmartChargingStopReason int lastStopChargingReason =
            mSmartCharging.getLastStopChargingReason();
        final int battCap = mSmartCharging.getBatteryCapacity();
        final float battTemp = mSmartCharging.getBatteryTemp();
        final boolean chargingEnabled = mSmartCharging.isChargingEnabled();

        final int chargingLimit = mSmartCharging.getChargingLimit();
        final int chargingResume = mSmartCharging.getChargingResume();
        int tempLimit = mSmartCharging.getTempLimit();
        final String currentMax = mSmartCharging.getCurrentMax();

        if (DEBUG) {
            Log.d(TAG, "Kernel Charging Enabled: " + chargingEnabled
                    + ", " + String.format("Battery Capacity: %d%% (Limit/Resume: %d%%/%d%%)",
                        battCap, chargingLimit, chargingResume)
                    + ", " + String.format("Battery Temperature: %.2f°C (Limit: %d°C)", battTemp,
                        tempLimit)
                    + ", " + "Current intensity (max.): " + currentMax
                    + ", " + "Last charging stop reason: " + lastStopChargingReason);
        }

        final boolean isPreviouslyOverheated = lastStopChargingReason ==
            SmartChargingStopReason.OVERHEATED;

        // Let the battery cool down by at least 3 °C since it has overheated previously. This is
        // just to avoid charging triggering repeatedly
        if (isPreviouslyOverheated) {
            tempLimit -= 3;
        }

        final boolean isOvercharged = chargingLimit <= battCap;
        final boolean isOverheated = tempLimit <= battTemp;
        final boolean isResumeable = chargingResume >= battCap;

        setMaxChargingCurrent(currentMax);

        if (chargingEnabled) {
            if (isOvercharged) {
                setLastStopChargingReason(SmartChargingStopReason.OVERCHARGED);
            } else if (isOverheated) {
                setLastStopChargingReason(SmartChargingStopReason.OVERHEATED);
            } else {
                return;
            }
            setChargingEnabled(false);
        } else if ((isResumeable || isPreviouslyOverheated) && chargingLimit != chargingResume
                && !isOverheated) {
            setLastStopChargingReason(SmartChargingStopReason.UNKNOWN);
            setChargingEnabled(true);
        }
    }

    private void resetBatteryStats() {
        if (DEBUG) Log.d(TAG, "resetBatteryStats().");
        try {
            Runtime.getRuntime().exec(new String[] { "dumpsys", "batterystats", "--reset" });
            Thread.sleep(1000);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void setLastStopChargingReason(final @SmartChargingStopReason int reason) {
        if (DEBUG) Log.d(TAG, String.format("setLastStopChargingReason(reason=%s).", reason));
        SystemProperties.set(mResources.getString(
                    R.string.smart_charging_prop_last_stop_charging_reason),
                String.valueOf(reason));
    }

    private void setChargingEnabled(final boolean enabled) {
        if (DEBUG) Log.d(TAG, String.format("setChargingEnabled(enabled=%s).", enabled));
        PartsUtils.writeValue(mResources.getString(
                    R.string.smart_charging_sysfs_charging_enabled_path), enabled ? "1" : "0");
    }

    private void setMaxChargingCurrent(final String mA) {
        if (DEBUG) Log.d(TAG, String.format("setMaxChargingCurrent(mA=%s).", mA));
        PartsUtils.writeValue(mResources.getString(
                    R.string.smart_charging_sysfs_charging_current_max_path), mA);
    }

    private long getNextBatteryMonitoringTriggerTime() {
        final int intervalMinutes;
        switch (mSmartCharging.getLastStopChargingReason()) {
            case SmartChargingStopReason.OVERHEATED:
                intervalMinutes = 5;
                break;
            case SmartChargingStopReason.OVERCHARGED:
                intervalMinutes = 15;
                break;
            default:
                intervalMinutes = 2;
        }

        return (System.currentTimeMillis() / 1000L + intervalMinutes * 60) * 1000L;
    }

    private PendingIntent getAlarmPendingIntent() {
        final Intent intent = new Intent(mContext, SmartChargingReceiver.class);
        intent.setAction(mResources.getString(
                    R.string.smart_charging_intent_battery_monitor_update));
        return PendingIntent.getBroadcast(mContext, /*requestCode=*/ 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
