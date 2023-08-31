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

package org.lineageos.settings.soundcontrol;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.Log;

import dagger.hilt.android.qualifiers.ApplicationContext;

import javax.inject.Inject;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

import static org.lineageos.settings.BuildConfig.DEBUG;

public final class SoundControl {
    private final String TAG = getClass().getSimpleName();

    final ContentResolver mContentResolver;
    final Resources mResources;

    @Inject
    public SoundControl(final @ApplicationContext Context context) {
        mContentResolver = context.getContentResolver();
        mResources = context.getResources();
    }

    public void onBootCompleted() {
        if (DEBUG) Log.d(TAG, "onBootCompleted().");
        setVolumeGain(getVolumeGain());
        setMicrophoneGain(getMichrophoneGain());
    }

    void onPreferenceUpdate(final String key, final int value) {
        if (DEBUG) Log.d(TAG, String.format("onPreferenceUpdate(key=%s,value=%d).", key, value));
        if (key.equals(mResources.getString(R.string.sound_control_key_volume_gain))) {
            setVolumeGain(value);
        } else if (key.equals(mResources.getString(R.string.sound_control_key_microphone_gain))) {
            setMicrophoneGain(value);
        }
    }

    private int getVolumeGain() {
        return Settings.Secure.getInt(mContentResolver, mResources.getString(
                    R.string.sound_control_key_volume_gain),
                mResources.getInteger(R.integer.sound_control_volume_gain_default_value));
    }

    private void setVolumeGain(final int value) {
        PartsUtils.setValue(mResources.getString(R.string.sound_control_sysfs_volume_gain_path),
                value + " " + value);
    }

    private int getMichrophoneGain() {
        return Settings.Secure.getInt(mContentResolver, mResources.getString(
                    R.string.sound_control_key_microphone_gain),
                mResources.getInteger(R.integer.sound_control_microphone_gain_default_value));
    }

    private void setMicrophoneGain(final int value) {
        PartsUtils.setValue(mResources.getString(
                    R.string.sound_control_sysfs_microphone_gain_path), value);
    }
}
