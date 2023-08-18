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
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import org.lineageos.settings.preferences.CustomSeekBarPreference;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

public class SoundControlSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    // NOTE: the kernel techpack audio driver implements speaker & headphone
    // gain to 'headphone_gain' file
    public static final String PREF_VOLUME_GAIN = "volume_gain";
    public static final String VOLUME_GAIN_PATH = "/sys/kernel/sound_control/headphone_gain";

    public static final String PREF_MICROPHONE_GAIN = "microphone_gain";
    public static final String MICROPHONE_GAIN_PATH = "/sys/kernel/sound_control/mic_gain";

    private CustomSeekBarPreference mVolumeGain;
    private CustomSeekBarPreference mMicrophoneGain;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.soundcontrol_settings, rootKey);

        mVolumeGain = (CustomSeekBarPreference) findPreference(PREF_VOLUME_GAIN);
        mVolumeGain.setOnPreferenceChangeListener(this);

        mMicrophoneGain = (CustomSeekBarPreference) findPreference(PREF_MICROPHONE_GAIN);
        mMicrophoneGain.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        switch (key) {
            case PREF_VOLUME_GAIN:
                PartsUtils.setValue(VOLUME_GAIN_PATH, value + " " + value);
                break;

            case PREF_MICROPHONE_GAIN:
                PartsUtils.setValue(MICROPHONE_GAIN_PATH, (int) value);
                break;

            default:
                break;
        }
        return true;
    }
}
