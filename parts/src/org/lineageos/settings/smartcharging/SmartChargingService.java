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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class SmartChargingService extends Service {
    private static final String TAG = "SmartChargingService";
    private static final boolean DEBUG = false;
    private boolean mBatteryMonitorRegistered = false;
    private SharedPreferences mSharedPrefs;

    private enum stopChargingReason { OVERHEATED, OVERCHARGED, UNKNOWN }
    private static stopChargingReason sLastStopChargingReason =
        stopChargingReason.UNKNOWN;

    public BroadcastReceiver mBatteryMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!SmartCharging.isPlugged()) {
                if (DEBUG) Log.d(TAG, "Charger/USB Unplugged");
                stopBatteryMonitoring();
            } else {
                update(mSharedPrefs);
            }
        }
    };

    public BroadcastReceiver mPowerMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
                if (DEBUG) Log.d(TAG, "Charger/USB Connected");
                startBatteryMonitoring();
            } else if (intent.getAction() == Intent.ACTION_POWER_DISCONNECTED) {
                if (DEBUG) Log.d(TAG, "Charger/USB Disconnected");
                int battCap = SmartCharging.getBatteryCapacity();
                final int chargingLimit = SmartCharging.getChargingLimit(mSharedPrefs);
                final boolean isCharged = chargingLimit == battCap;
                if (isCharged && SmartCharging.isResetStatsNeeded(mSharedPrefs)) {
                    resetStats();
                }
            }
        }
    };

    public void resetStats() {
        if (DEBUG) Log.d(TAG, "Resetting battery stats");
        try {
            Runtime.getRuntime().exec("dumpsys batterystats --reset");
            Thread.sleep(1000);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void update(final SharedPreferences sharedPrefs) {
        final int battCap = SmartCharging.getBatteryCapacity();
        final float battTemp = SmartCharging.getBatteryTemp();
        final boolean chargingEnabled = SmartCharging.isChargingEnabled();

        final int chargingLimit = SmartCharging.getChargingLimit(sharedPrefs);
        final int chargingResume = SmartCharging.getChargingResume(sharedPrefs);
        int tempLimit = SmartCharging.getTempLimit(sharedPrefs);

        if (DEBUG) {
            String msg1 = String.format("Kernel Charging Enabled: %s",
                    Boolean.valueOf(chargingEnabled).toString());
            String msg2 = String.format("Battery Capacity: %d%% (Limit/Resume: %d%%/%d%%)",
                    battCap, chargingLimit, chargingResume);
            String msg3 = String.format("Battery Temperature: %.2f°C (Limit: %d°C)",
                    battTemp, tempLimit);
            String msg4 = String.format("Last charging stop reason: %s",
                    sLastStopChargingReason);
            Log.d(TAG, msg1 + ", " + msg2 + ", " + msg3+ ", " + msg4);
        }

        final boolean isPreviouslyOverheated =
            sLastStopChargingReason == stopChargingReason.OVERHEATED;

        // Let the battery cool down by at least 3 °C since it has overheated
        // previously. This is just to avoid charging triggering repeatedly.
        if (isPreviouslyOverheated) {
            tempLimit -= 3;
        }

        final boolean isOvercharged = chargingLimit <= battCap;
        final boolean isOverheated = tempLimit <= battTemp;
        final boolean isResumeable = chargingResume >= battCap;

        if (chargingEnabled) {
            if (isOvercharged) {
                sLastStopChargingReason = stopChargingReason.OVERCHARGED;
            } else if (isOverheated) {
                sLastStopChargingReason = stopChargingReason.OVERHEATED;
            } else {
                return;
            }
            if (DEBUG) Log.d(TAG, "Stopping charging");
            SmartCharging.disableCharging();
        } else if ((isResumeable || isPreviouslyOverheated) &&
                chargingLimit != chargingResume && !isOverheated) {
            if (DEBUG) Log.d(TAG, "Enabling charging");
            SmartCharging.enableCharging();
            sLastStopChargingReason = stopChargingReason.UNKNOWN;
        }
    }

    private void startBatteryMonitoring() {
        if (!mBatteryMonitorRegistered) {
            if (DEBUG) Log.d(TAG, "Creating battery monitor service");
            IntentFilter batteryMonitor = new
                IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            getApplicationContext().registerReceiver(mBatteryMonitor, batteryMonitor);
            mBatteryMonitorRegistered = true;
        }
    }

    private void stopBatteryMonitoring() {
        if (mBatteryMonitorRegistered) {
            if (DEBUG) Log.d(TAG, "Destroying battery monitor service");
            getApplicationContext().unregisterReceiver(mBatteryMonitor);
            mBatteryMonitorRegistered = false;
        }
        SmartCharging.enableCharging();
        sLastStopChargingReason = stopChargingReason.UNKNOWN;
    }

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        super.onCreate();
        mSharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        IntentFilter powerMonitor = new IntentFilter();
        powerMonitor.addAction(Intent.ACTION_POWER_CONNECTED);
        powerMonitor.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(mPowerMonitor, powerMonitor);

        if (SmartCharging.isPlugged()) {
            startBatteryMonitoring();
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        super.onDestroy();
        unregisterReceiver(mPowerMonitor);
        stopBatteryMonitoring();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
