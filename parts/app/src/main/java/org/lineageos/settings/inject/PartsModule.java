/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.inject;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.session.MediaSessionManager;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

import org.lineageos.settings.dirac.DiracSound;

/** Application level module */
@InstallIn(SingletonComponent.class)
@Module
public final class PartsModule {
    @Provides
    static SharedPreferences provideSharedPreferences(final @ApplicationContext Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    static MediaSessionManager provideMediaSessionManager(
            final @ApplicationContext Context context) {

        return ContextCompat.getSystemService(context, MediaSessionManager.class);
    }

    @Provides
    @Singleton
    static DiracSound provideDiracSound() {
        return new DiracSound(/*priority=*/ 0, /*audioSession=*/ 0);
    }

    @Provides
    static AlarmManager provideAlarmManger(final @ApplicationContext Context context) {
        return ContextCompat.getSystemService(context, AlarmManager.class);
    }
}
