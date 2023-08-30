/*
 * Copyright (C) 2018 The Asus-SDM660 Project
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
 * limitations under the License
 */

package org.lineageos.settings.soundcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import org.lineageos.settings.preferences.CustomSeekBarPreference;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

public class SoundControlSettings extends PreferenceFragmentCompat implements
        Preference.OnPreferenceChangeListener {

    private CustomSeekBarPreference mVolumeGain;
    private CustomSeekBarPreference mMicrophoneGain;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.soundcontrol_settings, rootKey);

        mVolumeGain = (CustomSeekBarPreference)
            findPreference(getString(R.string.sound_control_key_volume_gain));
        mVolumeGain.setOnPreferenceChangeListener(this);

        mMicrophoneGain = (CustomSeekBarPreference)
            findPreference(getString(R.string.sound_control_key_microphone_gain));
        mMicrophoneGain.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        if (key.equals(getString(R.string.sound_control_key_volume_gain))) {
            PartsUtils.setValue(getString(R.string.sound_control_sysfs_volume_gain_path), value + " " + value);
        } else if (key.equals(getString(R.string.sound_control_key_microphone_gain))) {
            PartsUtils.setValue(getString(R.string.sound_control_sysfs_microphone_gain_path), (int) value);
        }
        return true;
    }
}
