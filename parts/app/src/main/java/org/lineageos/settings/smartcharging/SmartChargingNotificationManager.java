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

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.SystemProperties;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;

import dagger.hilt.android.qualifiers.ApplicationContext;

import javax.inject.Inject;

import org.lineageos.settings.R;

import static org.lineageos.settings.BuildConfig.DEBUG;

public final class SmartChargingNotificationManager {
    private static final String TAG = "SmartChargingNotification";

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL = "smartcharging.notificationChannel";

    private final Context mContext;
    private final NotificationManagerCompat mNotificationManager;
    private final SmartCharging mSmartCharging;

    private final Resources mResources;

    @Inject
    public SmartChargingNotificationManager(final @ApplicationContext Context context,
            final NotificationManagerCompat notificationManager,
            final SmartCharging smartCharging) {

        mContext = context;
        mNotificationManager = notificationManager;
        mSmartCharging = smartCharging;

        mResources = context.getResources();
    }

    @SuppressLint("MissingPermission")
    void showNotification() {
        if (DEBUG) Log.d(TAG, "showNotification().");

        mNotificationManager.notify(NOTIFICATION_ID, createNotification());
    }

    void removeNotification() {
        if (DEBUG) Log.d(TAG, "removeNotification().");

        mNotificationManager.cancel(NOTIFICATION_ID);
        setNotificationDismissed(false);
    }

    boolean isNotificationDismissed() {
        return SystemProperties.getBoolean(mResources.getString(
                    R.string.smart_charging_prop_notification_dismissed), false);
    }

    void setNotificationDismissed(final boolean dismised) {
        if (DEBUG) Log.d(TAG, String.format("setNotificationDismissed(dismissed=%s).", dismised));
        SystemProperties.set(mResources.getString(
                    R.string.smart_charging_prop_notification_dismissed),
                String.valueOf(dismised));
    }

    private PendingIntent createPendingIntent(final Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_MAIN:
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(mContext, SmartChargingActivity.class);
                return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE |
                        PendingIntent.FLAG_UPDATE_CURRENT);
            default:
                intent.setClass(mContext, SmartChargingReceiver.class);
                return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE |
                        PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private void createNotificationChannel() {
        final NotificationChannelCompat.Builder builder = new NotificationChannelCompat.Builder(
                NOTIFICATION_CHANNEL, NotificationManagerCompat.IMPORTANCE_LOW);
        builder.setName(mContext.getString(R.string.smart_charging_title));

        mNotificationManager.createNotificationChannel(builder.build());
    }

    private Notification createNotification() {
        createNotificationChannel();
        final Notification.Builder notificationBuilder = new Notification.Builder(mContext,
                NOTIFICATION_CHANNEL);
        notificationBuilder.setSmallIcon(R.drawable.ic_power)
            .setShowWhen(false);

        notificationBuilder.addAction(new Notification.Action.Builder(
                    Icon.createWithResource("", 0),
                    mContext.getString(R.string.not_now),
                    createPendingIntent(new Intent(mResources.getString(
                                R.string.smart_charging_intent_notification_not_now)))).build());
        notificationBuilder.setContentIntent(createPendingIntent(new Intent(Intent.ACTION_MAIN)));
        notificationBuilder.setDeleteIntent(createPendingIntent(new Intent(mResources.getString(
                            R.string.smart_charging_intent_notification_dismiss))));
        notificationBuilder.setContentTitle(mContext.getString(R.string.smart_charging_title));

        final Notification.InboxStyle style = new Notification.InboxStyle();
        style.addLine(String.format("%s: %d%%",
                    mContext.getString(R.string.smart_charging_level_title),
                    mSmartCharging.getChargingLimit()))
            .addLine(String.format("%s: %d%%",
                    mContext.getString(R.string.smart_charging_resume_level_title),
                    mSmartCharging.getChargingResume()))
            .addLine(String.format("%s: %.1f°C (max. %d°C)",
                    mContext.getString(R.string.smart_charging_battery_temperature),
                    mSmartCharging.getBatteryTemp(), mSmartCharging.getTempLimit()));

        final long nextUpdateTime = mSmartCharging.getBatteryMonitorNextUpdateTime();
        if (nextUpdateTime > 0) {
            final String prettyTime = DateUtils.formatDateTime(mContext, nextUpdateTime,
                    DateUtils.FORMAT_SHOW_TIME);
            style.addLine(" ").addLine(mContext.getString(R.string.next_update_time, prettyTime));
        }
        notificationBuilder.setStyle(style);

        final Notification n = notificationBuilder.build();
        n.flags &= ~Notification.FLAG_NO_CLEAR;
        return n;
    }
}
