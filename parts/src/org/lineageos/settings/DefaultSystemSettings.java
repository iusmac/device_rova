/*
 * Copyright (C) 2022 The LineageOS Project
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

package org.lineageos.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class DefaultSystemSettings {
    private static final String TAG = "DefaultSystemSettings";
    private static final boolean DEBUG = false;

    private Context mContext;
    private SharedPreferences mSharedPrefs;

    public DefaultSystemSettings(final Context context) {
        mContext = context;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private boolean isFirstRun(String key) {
        boolean isFirstRun = mSharedPrefs.getBoolean(key, true);
        if (isFirstRun) {
            mSharedPrefs.edit().putBoolean(key, false).apply();
        }
        return isFirstRun;
    }

    private void saveFirstRun(String key) {
        mSharedPrefs.edit().putBoolean(key, true).apply();
    }

    public void onBootCompleted() {
    }
}
