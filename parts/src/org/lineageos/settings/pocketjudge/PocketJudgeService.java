/*
 * MIT License
 *
 * Copyright (c) 2021 Trần Mạnh Cường <maytinhdibo>
 *               2022 iusmac <iusico.maxim@libero.it>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.lineageos.settings.pocketjudge;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lineageos.providers.LineageSettings;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.PhoneStateReceiver;
import org.lineageos.settings.R;

public class PocketJudgeService extends Service {
    private static final String TAG = "PocketJudgeService";
    private static final boolean DEBUG = false;

    private static final int EVENT_UNLOCK = 2;
    private static final int EVENT_TURN_ON_SCREEN = 1;
    private static final int EVENT_TURN_OFF_SCREEN = 0;

    private static final int NOTIFICATION_ID = 2;
    public static final String NOTIFICATION_CHANNEL = "pocketjudge_notification_channel";
    private NotificationManager mNotificationManager = null;

    private boolean mVolBtnMusicControlsEnabled = false;
    private boolean mVolumeWakeScreenEnabled = false;

    private Thread mSafeDoorThread = null;
    private boolean mIsSafeDoorThreadExit = false;

    private float mProximityMaxRange;
    private int mLastAction = -1;
    private boolean mIsSensorRunning = false;

    private ExecutorService mExecutorService;
    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    private Context mContext;
    private KeyguardManager mKeyguardManager;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mExecutorService = Executors.newSingleThreadExecutor();
        mProximityMaxRange = mProximitySensor.getMaximumRange();

        mContext = getApplicationContext();

        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mScreenStateReceiver, screenStateFilter);

        mKeyguardManager = (KeyguardManager)
            mContext.getSystemService(Context.KEYGUARD_SERVICE);

        mNotificationManager = (NotificationManager)
            mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel notificationChannel = new
            NotificationChannel(NOTIFICATION_CHANNEL,
                    mContext.getString(R.string.pocket_judge_title),
                    NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setSound(null, null);
        mNotificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        this.unregisterReceiver(mScreenStateReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Future<?> submit(Runnable runnable) {
        return mExecutorService.submit(runnable);
    }

    private void showNotification() {
        Notification.Builder notificationBuilder;
        notificationBuilder = new Notification.Builder(mContext,
                NOTIFICATION_CHANNEL);

        notificationBuilder.setSmallIcon(R.drawable.ic_pocket)
            .setShowWhen(false)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentTitle(mContext
                    .getString(R.string.pocket_judge_notif_title))
            .setContentText(mContext
                    .getString(R.string.pocket_judge_notif_msg));

        Notification n = notificationBuilder.build();
        mNotificationManager.notify(NOTIFICATION_ID, n);
    }

    private void removeNotification() {
        mNotificationManager.cancelAll();
    }

    private void setInPocket(boolean active) {
        PocketJudge.setInPocket(active);
        if (active) {
            showNotification();
        } else {
            removeNotification();
        }
    }

    private void disableSensor() {
        if (!mIsSensorRunning) return;
        if (DEBUG) Log.d(TAG, "Disabling proximity sensor");
        submit(() -> {
            mSensorManager.unregisterListener(mProximitySensorEventListener,
                    mProximitySensor);
            setInPocket(false);
            mIsSensorRunning = false;
            stopSafeDoorThreadPoll();
        });
    }

    private void enableSensor() {
        if (mIsSensorRunning) return;
        if (DEBUG) Log.d(TAG, "Enabling proximity sensor");
        submit(() -> {
            mSensorManager.registerListener(mProximitySensorEventListener,
                    mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            mIsSensorRunning = true;
        });
    }

    private void startSafeDoorThreadPoll() {
        mIsSafeDoorThreadExit = false;
        if (null != mSafeDoorThread) {
            return;
        }
        mSafeDoorThread = new Thread() {
            public void run() {
                while (true) {
                    if (mIsSafeDoorThreadExit) {
                        break;
                    }

                    if (PocketJudge.isSafeDoorTriggered()) {
                        if (DEBUG)
                            Log.d(TAG, "Thread(): SAFE DOOR TRIGGERED, " +
                                "force unblocking touchscreen/buttons and disable sensor");
                        disableSensor();
                        break;
                    }

                    try {
                        final int hundredMillisecond = 200;
                        Thread.sleep(hundredMillisecond);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mSafeDoorThread.start();
    }

    private void stopSafeDoorThreadPoll() {
        if (null != mSafeDoorThread) {
            mIsSafeDoorThreadExit = true;
            mSafeDoorThread = null;
        }
    }

    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (DEBUG) Log.d(TAG, "Receiving screen intent: ACTION_SCREEN_ON");
                final int oldAction = mLastAction;
                mLastAction = EVENT_TURN_ON_SCREEN;

                if (!mKeyguardManager.isKeyguardLocked()
                        || oldAction == EVENT_UNLOCK) {
                    if (DEBUG) Log.d(TAG,
                            "ACTION_SCREEN_ON: Screen is on but no keyguard. " +
                            "Skipping sensor enable");
                } else {
                    enableSensor();
                }

                submit(() -> {
                    // Disable force wake screen with volume keys if the user
                    // doesn't want it
                    if (!mVolBtnMusicControlsEnabled && !mVolumeWakeScreenEnabled) {
                        LineageSettings.System.putIntForUser(mContext.getContentResolver(),
                                LineageSettings.System.VOLUME_WAKE_SCREEN, 0,
                                UserHandle.USER_CURRENT);
                    }
                });
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if (DEBUG) Log.d(TAG, "Receiving screen intent: ACTION_SCREEN_OFF");
                mLastAction = EVENT_TURN_OFF_SCREEN;
                disableSensor();
                submit(() -> {
                    mVolBtnMusicControlsEnabled = LineageSettings.System.getIntForUser(
                            mContext.getContentResolver(),
                            LineageSettings.System.VOLBTN_MUSIC_CONTROLS, 0,
                            UserHandle.USER_CURRENT) != 0;
                    if (mVolBtnMusicControlsEnabled) {
                        return;
                    }

                    mVolumeWakeScreenEnabled = LineageSettings.System.getIntForUser(
                            mContext.getContentResolver(),
                            LineageSettings.System.VOLUME_WAKE_SCREEN, 0,
                            UserHandle.USER_CURRENT) != 0;
                    if (mVolumeWakeScreenEnabled) {
                        return;
                    }

                    // Force wake screen with volume keys if volume buttons should
                    // not seek media tracks
                    LineageSettings.System.putIntForUser(mContext.getContentResolver(),
                            LineageSettings.System.VOLUME_WAKE_SCREEN, 1,
                            UserHandle.USER_CURRENT);
                });
            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                if (DEBUG) Log.d(TAG, "Receiving screen intent: ACTION_USER_PRESENT");
                mLastAction = EVENT_UNLOCK;
                disableSensor();
            }
        }
    };

    public SensorEventListener mProximitySensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }

        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
                if (event == null) {
                    if (DEBUG) Log.d(TAG, "Event is null!");
                } else if (event.values == null || event.values.length == 0) {
                    if (DEBUG)
                        Log.d(TAG, "Event has no values! event.values null ? " +
                                (event.values == null));
                } else {
                    final float value = event.values[0];
                    final boolean isPositive = event.values[0] < mProximityMaxRange;
                    if (DEBUG) {
                        final long time = SystemClock.uptimeMillis();
                        Log.d(TAG, "Event: time=" + time + ", value=" + value
                                + ", maxRange=" + mProximityMaxRange + ", isPositive=" + isPositive);
                    }
                    if (isPositive) {
                        if (PhoneStateReceiver.CUR_STATE ==
                                PhoneStateReceiver.IDLE) {
                            if (DEBUG) Log.d(TAG, "onSensorChanged(): COVERED, " +
                                    "blocking touchscreen and buttons");
                            setInPocket(true);

                            // We're in pocket, start listen for safe-door
                            // state
                            if (null == mSafeDoorThread) {
                                startSafeDoorThreadPoll();
                            }
                        }
                    } else {
                        if (DEBUG) Log.d(TAG, "onSensorChanged(): UNCOVERED, " +
                                "unblocking touchscreen and buttons");
                        setInPocket(false);
                    }
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "Event: something went wrong, exception caught, e = " + e);
                setInPocket(false);
            }
        }
    };
}
