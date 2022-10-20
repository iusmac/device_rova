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
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.R;

public class SmartChargingService extends Service {
    private static final String TAG = "SmartChargingService";
    private static final boolean DEBUG = false;

    private String mTargetClassName = "org.lineageos.settings.smartcharging.SmartChargingActivity";
    private static final int NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_CHANNEL = "smartcharging_notification_channel";
    public static final String NOTIFICATION_DISMISS = "smartcharging.service.dismiss";
    private NotificationManager mNotificationManager = null;
    private NotificationChannel mNotificationChannel = null;

    private WakeLock mWakeLock = null;

    private boolean mBatteryMonitorRegistered = false;
    private static final String BATTERY_MONITOR_UPDATE_INTENT =
        "org.lineageos.settings.smartcharging.BatteryMonitorReceiver";
    public BatteryMonitorReceiver mBatteryMonitorReceiver = null;
    private SharedPreferences mSharedPrefs;

    private enum stopChargingReason { OVERHEATED, OVERCHARGED, NOTIF_DISMISS, UNKNOWN }
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
        private Long mNextUpdateTime = 0L;

        public BatteryMonitorReceiver(SmartChargingService service, Context
                context, SharedPreferences sharedPrefs) {
            mService = service;
            mContext = context;
            mSharedPrefs = sharedPrefs;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mIsActive = false;
            if (!SmartCharging.isPlugged()) {
                if (DEBUG) Log.d(TAG, "Charger/USB Unplugged");
                mService.stopBatteryMonitoring(stopChargingReason.UNKNOWN);
            } else {
                start();
                mService.update(mSharedPrefs);
                mService.showNotification();
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
                    intervalMin = 2;
            }

            assert mAlarmManager != null;
            mNextUpdateTime = (System.currentTimeMillis() / 1000L + intervalMin
                    * 60) * 1000L;
            mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    mNextUpdateTime, mPendingIntent);
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

        public Long getNextUpdateTime() {
            return mNextUpdateTime;
        }
    }

    public BroadcastReceiver mPowerMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
                if (DEBUG) Log.d(TAG, "Charger/USB Connected");
                if (sLastStopChargingReason == stopChargingReason.NOTIF_DISMISS) {
                    // User dismissed monitoring, but charging resumed. We need
                    // to reset last stop reason here, so that the next charger
                    // re-plug can start the monitoring as expected.
                    sLastStopChargingReason = stopChargingReason.UNKNOWN;
                } else {
                    startBatteryMonitoring();
                }
            } else if (intent.getAction() == Intent.ACTION_POWER_DISCONNECTED) {
                if (DEBUG) Log.d(TAG, "Charger/USB Disconnected");
                final boolean isPreviouslyOverheated =
                    sLastStopChargingReason == stopChargingReason.OVERHEATED;
                final boolean isPreviouslyOvercharged =
                    sLastStopChargingReason == stopChargingReason.OVERCHARGED;

                // Stop now if there's no reason to monitor
                if (!isPreviouslyOverheated && !isPreviouslyOvercharged) {
                    stopBatteryMonitoring(stopChargingReason.UNKNOWN);
                    return;
                }

                int battCap = SmartCharging.getBatteryCapacity();
                final int chargingLimit = SmartCharging.getChargingLimit(mSharedPrefs);
                final boolean isCharged = chargingLimit <= battCap;
                if (isCharged && isPreviouslyOvercharged &&
                        SmartCharging.isResetStatsNeeded(mSharedPrefs)) {
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
            sLastStopChargingReason = stopChargingReason.UNKNOWN;
            SmartCharging.enableCharging();
        }
    }

    private void startBatteryMonitoring() {
        if (!mBatteryMonitorRegistered) {
            if (DEBUG) Log.d(TAG, "Creating battery monitor service");
            IntentFilter batteryMonitor = new
                IntentFilter(BATTERY_MONITOR_UPDATE_INTENT);
            getApplicationContext().registerReceiver(mBatteryMonitorReceiver,
                    batteryMonitor);
            mBatteryMonitorRegistered = true;
        }

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        if (!mBatteryMonitorReceiver.isActive()) {
            mBatteryMonitorReceiver.start();
        }
        update(mSharedPrefs);
        showNotification();
    }

    private void stopBatteryMonitoring(stopChargingReason reason) {
        if (mBatteryMonitorRegistered) {
            if (DEBUG) Log.d(TAG, "Destroying battery monitor service");
            getApplicationContext().unregisterReceiver(mBatteryMonitorReceiver);
            mBatteryMonitorReceiver.stop();
            mBatteryMonitorRegistered = false;
        }

        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        removeNotification();

        sLastStopChargingReason = reason;
        SmartCharging.enableCharging();
    }

    private void showNotification() {
        Context ctx = getApplicationContext();
        Intent aIntent = new Intent(Intent.ACTION_MAIN);
        aIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        aIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        aIntent.setClassName(getPackageName(), mTargetClassName);
        PendingIntent pAIntent = PendingIntent.getActivity(ctx, 0, aIntent,
                PendingIntent.FLAG_IMMUTABLE |
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (mNotificationChannel == null) {
            mNotificationChannel =
                new NotificationChannel(NOTIFICATION_CHANNEL,
                        ctx.getString(R.string.smart_charging_title),
                        NotificationManager.IMPORTANCE_LOW);

            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }

        Notification.Builder notificationBuilder;
        notificationBuilder = new Notification.Builder(ctx,
                NOTIFICATION_CHANNEL);
        notificationBuilder
            .setSmallIcon(R.drawable.ic_power)
            .setShowWhen(false)
            .setAutoCancel(true);

        Intent intent = new Intent(NOTIFICATION_DISMISS);
        intent.setClass(ctx, SmartChargingService.class);
        PendingIntent pIntent = PendingIntent.getService(ctx, 0, intent,
                PendingIntent.FLAG_IMMUTABLE |
                PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(0,
                ctx.getString(R.string.not_now),
                pIntent);
        notificationBuilder.setContentIntent(pAIntent);

        notificationBuilder.setContentTitle(ctx.getString(R.string.smart_charging_title));

        final int chargingLimit = SmartCharging.getChargingLimit(mSharedPrefs);
        final int chargingResume = SmartCharging.getChargingResume(mSharedPrefs);
        final int tempLimit = SmartCharging.getTempLimit(mSharedPrefs);
        final float battTemp = SmartCharging.getBatteryTemp();

        Notification.InboxStyle style = new Notification.InboxStyle();
        style
            .addLine(String.format("%s: %d%%",
                    ctx.getString(R.string.smart_charging_level_title),
                    chargingLimit))
            .addLine(String.format("%s: %d%%",
                    ctx.getString(R.string.smart_charging_resume_level_title),
                    chargingResume))
            .addLine(String.format("%s: %.1f°C (max. %d°C)",
                    ctx.getString(R.string.smart_charging_battery_temperature),
                    battTemp, tempLimit));

        Long nextUpdateTime = mBatteryMonitorReceiver.getNextUpdateTime();
        if (nextUpdateTime > 0) {
            String timeString = DateUtils.formatDateTime(ctx, nextUpdateTime,
                    DateUtils.FORMAT_SHOW_TIME);
            style
                .addLine(" ")
                .addLine(ctx.getString(R.string.next_update_time, timeString));
        }
        notificationBuilder.setStyle(style);

        Notification n = notificationBuilder.build();
        n.flags &= ~Notification.FLAG_NO_CLEAR;
        startForeground(NOTIFICATION_ID, n);
    }

    private void removeNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE);
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

        PowerManager powerManager = (PowerManager) getSystemService(ctx.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.setReferenceCounted(false);

        if (SmartCharging.isPlugged()) {
            startBatteryMonitoring();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : "";
        if (action == null) {
            action = "";
        }

        if (DEBUG) Log.d(TAG, "Receiving onStartCommand(); action = " + action);
        super.onStartCommand(intent, flags, startId);

        if (!"".equals(action)) {
            if (NOTIFICATION_DISMISS.equals(action)) {
                stopBatteryMonitoring(stopChargingReason.NOTIF_DISMISS);
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        super.onDestroy();
        unregisterReceiver(mPowerMonitor);
        stopBatteryMonitoring(stopChargingReason.UNKNOWN);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
