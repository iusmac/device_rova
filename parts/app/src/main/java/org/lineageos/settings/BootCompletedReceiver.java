/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2018 The LineageOS Project
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

package org.lineageos.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

import org.lineageos.settings.soundcontrol.SoundControlSettings;
import org.lineageos.settings.dirac.DiracUtils;
import org.lineageos.settings.smartcharging.SmartCharging;
import org.lineageos.settings.ramplus.RamPlusService;

import static org.lineageos.settings.BuildConfig.DEBUG;

@AndroidEntryPoint(BroadcastReceiver.class)
public class BootCompletedReceiver extends Hilt_BootCompletedReceiver {
    private static final String TAG = "XiaomiParts";

    @Inject
    DiracUtils mDiracUtils;

    @Inject
    DefaultSystemSettings mDefaultSystemSettings;

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);

        int gain = Settings.Secure.getInt(
            context.getContentResolver(),
            SoundControlSettings.PREF_VOLUME_GAIN, -10
        );
        PartsUtils.setValue(
            SoundControlSettings.VOLUME_GAIN_PATH,
            gain + " " + gain
        );

        PartsUtils.setValue(
            SoundControlSettings.MICROPHONE_GAIN_PATH,
            Settings.Secure.getInt(
                context.getContentResolver(),
                SoundControlSettings.PREF_MICROPHONE_GAIN,
                0
            )
        );
        mDiracUtils.onBootCompleted();
        new SmartCharging(context).onBootCompleted();
        mDefaultSystemSettings.onBootCompleted();
        RamPlusService.sync(context);
    }
}
