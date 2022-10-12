/*
 * Copyright (C) 2022 iusmac <iusico.maxim@libero.it>
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

package org.lineageos.settings.pocketjudge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.PartsUtils;

public class PocketJudge {
    private static final String TAG = "PocketJudge";
    private static final boolean DEBUG = true;

    public static final String KEY_POCKET_JUDGE_SWITCH = "key_pocket_judge";
    private static final String POCKET_BRIDGE_INPOCKET_FILE = "/sys/kernel/pocket_judge/inpocket";

    private Context mContext;

    public PocketJudge(final Context context) {
        mContext = context;
    }

    public void onBootCompleted() {
        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean enabled = sharedPrefs.getBoolean(KEY_POCKET_JUDGE_SWITCH, false);
        if (enabled) {
            startService(mContext);
        }
        if (DEBUG) Log.d(TAG, "Started. Service is enabled: " + Boolean.valueOf(enabled).toString());
    }

    public static void startService(final Context context) {
        PartsUtils.startService(context, PocketJudgeService.class);
    }

    public static void stopService(final Context context) {
        PartsUtils.stopService(context, PocketJudgeService.class);
    }

    public static boolean isSafeDoorTriggered() {
        String raw = PartsUtils.readLine(POCKET_BRIDGE_INPOCKET_FILE);
        return "2".equals(raw);
    }

    public static void setInPocket(boolean active) {
        PartsUtils.writeValue(POCKET_BRIDGE_INPOCKET_FILE, active ? "1" : "0");
    }
}
