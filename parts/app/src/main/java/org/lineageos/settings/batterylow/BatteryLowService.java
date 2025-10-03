package org.lineageos.settings.batterylow;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import javax.inject.Provider;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

import static android.os.OsProtoEnums.BATTERY_PLUGGED_NONE; // = 0
import static org.lineageos.settings.BuildConfig.DEBUG;

@AndroidEntryPoint(Service.class)
public class BatteryLowService extends Hilt_BatteryLowService {
    private static final String TAG = "BatteryLowService";
    private static final int NOTIFICATION_ID = 2;
    private static final String NOTIFICATION_CHANNEL = "batterylow.notificationChannel";

    private int mLastBatteryLevel;
    private boolean mNotificationShown;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_BATTERY_LEVEL_CHANGED -> {
                    final Bundle lastEvent =
                        intent.getParcelableArrayListExtra(BatteryManager.EXTRA_EVENTS,
                                Bundle.class).getLast();
                    mLastBatteryLevel = lastEvent.getInt(BatteryManager.EXTRA_LEVEL);
                    final int batteryStatus = lastEvent.getInt(BatteryManager.EXTRA_STATUS);
                    final int plugType = lastEvent.getInt(BatteryManager.EXTRA_PLUGGED);
                    final boolean thresholdBreached =
                        mLastBatteryLevel <= mBatteryLow.getBatteryLevelLowThreshold();
                    final boolean plugged = plugType != BATTERY_PLUGGED_NONE;
                    final boolean isBatteryVeryLow = !plugged && thresholdBreached
                        && batteryStatus != BatteryManager.BATTERY_STATUS_UNKNOWN;
                    if (DEBUG) Log.d(TAG, "isBatteryVeryLow=" + isBatteryVeryLow +
                            ",level=" + mLastBatteryLevel +
                            ",batteryStatus=" + batteryStatus +
                            ",plugType=" + plugType +
                            ",thresholdBreached=" + thresholdBreached);

                    showNotification(isBatteryVeryLow);
                }
                case Intent.ACTION_LOCALE_CHANGED -> {
                    // Resend the notification to update the locale-sensitive part (title, content)
                    if (mNotificationShown) {
                        BatteryLowService.this.notify(/*muted=*/ true);
                    }
                    createNotificationChannel();
                }
            }
        }
    };

    @Inject
    Provider<NotificationManagerCompat> mNotificationManagerProvider;

    @Inject
    BatteryLow mBatteryLow;

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG) Log.d(TAG, "Creating service");

        createNotificationChannel();

        final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_LEVEL_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @SuppressLint("MissingPermission")
    private void notify(final boolean muted) {
        mNotificationManagerProvider.get().notify(NOTIFICATION_ID, createNotification(muted));
    }

    private void showNotification(final boolean shouldShow) {
        if (DEBUG) Log.d(TAG, "showNotification(" + shouldShow + ").");

        if (shouldShow) {
            notify(/*muted=*/ false);
        } else if (mNotificationShown != shouldShow) {
            mNotificationManagerProvider.get().cancel(NOTIFICATION_ID);
        }
        mNotificationShown = shouldShow;
    }

    private void createNotificationChannel() {
        final NotificationChannelCompat.Builder builder = new NotificationChannelCompat.Builder(
                NOTIFICATION_CHANNEL,
                // Notification importance should be IMPORTANCE_MAX to have the highest priority, so
                // it can be shown in all times
                NotificationManager.IMPORTANCE_MAX)
            .setName(getString(R.string.batterylow_title))
            .setDescription(getString(R.string.batterylow_about))
            .setLightsEnabled(true)
            .setVibrationEnabled(true);

        // Use the system audio file to play the low battery sound
        final String soundPath = Settings.Global.getString(getContentResolver(),
                Settings.Global.LOW_BATTERY_SOUND);
        builder.setSound(Uri.parse("file://" + soundPath), new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build());

        mNotificationManagerProvider.get().createNotificationChannel(builder.build());
    }

    private Notification createNotification(final boolean muted) {
        final String percentage = PartsUtils.formatPercentage(mLastBatteryLevel);
        final String contentText = getString(R.string.batterylow_heads_up_message, percentage);
        return new Notification.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_power_low)
            .setAutoCancel(true)
            .setContentIntent(createPendingContentIntent())
            .setContentTitle(getString(R.string.batterylow_title))
            .setContentText(contentText)
            .setOnlyAlertOnce(muted)
            .setStyle(new Notification.BigTextStyle().bigText(contentText))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setColor(PartsUtils.getColorAttrDefaultColor(this, android.R.attr.colorError)) // (red)
            .build();
    }

    private PendingIntent createPendingContentIntent() {
        final Intent i = new Intent();
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setClass(this, BatteryLowActivity.class);
        return PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE |
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");

        super.onDestroy();
        unregisterReceiver(mReceiver);
        showNotification(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
