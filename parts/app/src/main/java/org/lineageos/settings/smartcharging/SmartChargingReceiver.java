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
 */

package org.lineageos.settings.smartcharging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.lineageos.settings.BuildConfig.DEBUG;

@AndroidEntryPoint(BroadcastReceiver.class)
public final class SmartChargingReceiver extends Hilt_SmartChargingReceiver {
    private static final String TAG = "SmartChargingReceiver";

    @Inject
    Provider<SmartChargingManager> mSmartChargingManagerProvider;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);

        if (DEBUG) Log.d(TAG, "Intent = " + intent);

        switch (intent.getAction()) {
            case Intent.ACTION_POWER_CONNECTED:
                mSmartChargingManagerProvider.get().onPowerConnected();
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                mSmartChargingManagerProvider.get().onPowerDisconnected();
                break;
            case SmartCharging.BATTERY_MONITOR_UPDATE_INTENT:
                mSmartChargingManagerProvider.get().onBatteryUpdate();
                break;
            case SmartChargingNotificationManager.NOTIFICATION_DISMISS_INTENT:
                mSmartChargingManagerProvider.get().onNotificationDismiss();
                break;
            case SmartChargingNotificationManager.NOTIFICATION_NOT_NOW_INTENT:
                mSmartChargingManagerProvider.get().onNotificationNotNowAction();
        }
    }
}
