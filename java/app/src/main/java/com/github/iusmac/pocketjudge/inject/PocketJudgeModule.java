/*
 * Copyright (C) 2023 iusmac <iusico.maxim@libero.it>
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
package com.github.iusmac.pocketjudge.inject;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/** Application level module. */
@InstallIn(SingletonComponent.class)
@Module
public final class PocketJudgeModule {
    @Provides
    static SharedPreferences provideSharedPreferences(final @ApplicationContext Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    static SensorManager provideSensorManager(final @ApplicationContext Context context) {
        return ContextCompat.getSystemService(context, SensorManager.class);
    }

    @Provides
    static KeyguardManager provideKeyguardManager(final @ApplicationContext Context context) {
        return ContextCompat.getSystemService(context, KeyguardManager.class);
    }

    @Provides
    static NotificationManager provideNotificationManager(
            final @ApplicationContext Context context) {

        return ContextCompat.getSystemService(context, NotificationManager.class);
    }
}
