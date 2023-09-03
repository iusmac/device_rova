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

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

import org.lineageos.settings.soundcontrol.SoundControl;
import org.lineageos.settings.dirac.DiracUtils;
import org.lineageos.settings.smartcharging.SmartChargingManager;
import org.lineageos.settings.ramplus.RamPlusManager;

import static org.lineageos.settings.BuildConfig.DEBUG;

@AndroidEntryPoint(BroadcastReceiver.class)
public class BootCompletedReceiver extends Hilt_BootCompletedReceiver {
    private static final String TAG = "XiaomiParts";

    @Inject
    SoundControl mSoundControl;

    @Inject
    DiracUtils mDiracUtils;

    @Inject
    SmartChargingManager mSmartChargingManager;

    @Inject
    DefaultSystemSettings mDefaultSystemSettings;

    @Inject
    RamPlusManager mRamPlusManager;

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);

        mSoundControl.onBootCompleted();
        mDiracUtils.onBootCompleted();
        mSmartChargingManager.onBootCompleted();
        mDefaultSystemSettings.onBootCompleted();
        mRamPlusManager.onBootCompleted();
    }
}
