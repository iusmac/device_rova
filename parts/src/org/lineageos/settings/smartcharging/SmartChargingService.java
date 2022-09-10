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

import android.app.AlarmManager;
import android.app.PendingIntent;
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
    private static final String BATTERY_MONITOR_UPDATE_INTENT =
        "org.lineageos.settings.smartcharging.BatteryMonitorReceiver";
    public BatteryMonitorReceiver mBatteryMonitorReceiver = null;
    private SharedPreferences mSharedPrefs;

    private enum stopChargingReason { OVERHEATED, OVERCHARGED, UNKNOWN }
    private static stopChargingReason sLastStopChargingReason =
        stopChargingReason.UNKNOWN;

    /*
     * This receiver may look "hairy", but it simply does what the
     * "ACTION_BATTERY_CHANGED" intent did before. Now we use RTC Alarm Manager
     * to endlessly repeat the logic. This workarounds the case when the device
     * enters in an idle state + now we check less frequently instead of every
     * 10secs as "ACTION_BATTERY_CHANGED" intent does.
     */
    private class BatteryMonitorReceiver extends BroadcastReceiver {
        private boolean mIsActive = false;
        private AlarmManager mAlarmManager = null;
        private PendingIntent mPendingIntent = null;
        private Context mContext = null;
        private SharedPreferences mSharedPrefs;
        private SmartChargingService mService = null;

        public BatteryMonitorReceiver(SmartChargingService service, Context
                context, SharedPreferences sharedPrefs) {
            mService = service;
            mContext = context;
            mSharedPrefs = sharedPrefs;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!SmartCharging.isPlugged()) {
                if (DEBUG) Log.d(TAG, "Charger/USB Unplugged");
                mService.stopBatteryMonitoring();
            } else {
                mService.update(mSharedPrefs);
                start();
            }
        }

        public void start() {
            if (mAlarmManager == null) {
                mAlarmManager = (AlarmManager)
                    mContext.getSystemService(Context.ALARM_SERVICE);
            }
            if (mPendingIntent == null) {
                Intent intent = new Intent(BATTERY_MONITOR_UPDATE_INTENT);
                mPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
                        PendingIntent.FLAG_IMMUTABLE |
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            int intervalMin;
            switch (sLastStopChargingReason) {
                case OVERHEATED:
                    intervalMin = 5;
                    break;
                case OVERCHARGED:
                    intervalMin = 15;
                    break;
                default:
                    intervalMin = 3;
            }

            assert mAlarmManager != null;
            mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    (System.currentTimeMillis() / 1000L + intervalMin * 60) * 1000L,
                    mPendingIntent);
            mIsActive = true;
        }

        public void stop() {
            if (mAlarmManager != null && mPendingIntent != null) {
                mAlarmManager.cancel(mPendingIntent);
            }
            mIsActive = false;
        }

        public boolean isActive() {
            return mIsActive;
        }
    }

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
        if (!mBatteryMonitorReceiver.isActive()) {
            if (DEBUG) Log.d(TAG, "Creating battery monitor service");
            IntentFilter batteryMonitor = new
                IntentFilter(BATTERY_MONITOR_UPDATE_INTENT);
            getApplicationContext().registerReceiver(mBatteryMonitorReceiver,
                    batteryMonitor);
            mBatteryMonitorReceiver.start();
            update(mSharedPrefs);
        }
    }

    private void stopBatteryMonitoring() {
        if (mBatteryMonitorReceiver.isActive()) {
            if (DEBUG) Log.d(TAG, "Destroying battery monitor service");
            getApplicationContext().unregisterReceiver(mBatteryMonitorReceiver);
            mBatteryMonitorReceiver.stop();
        }
        SmartCharging.enableCharging();
        sLastStopChargingReason = stopChargingReason.UNKNOWN;
    }

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        super.onCreate();
        Context ctx = getApplicationContext();
        mSharedPrefs = PreferenceManager
            .getDefaultSharedPreferences(ctx);

        IntentFilter powerMonitor = new IntentFilter();
        powerMonitor.addAction(Intent.ACTION_POWER_CONNECTED);
        powerMonitor.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(mPowerMonitor, powerMonitor);

        mBatteryMonitorReceiver = new BatteryMonitorReceiver(this, ctx,
                mSharedPrefs);

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
