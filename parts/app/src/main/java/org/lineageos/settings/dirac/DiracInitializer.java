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

package org.lineageos.settings.dirac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static org.lineageos.settings.BuildConfig.DEBUG;

public final class DiracInitializer extends BroadcastReceiver {
    private static final String TAG = "DiracInitializer";

    private DiracUtils mDiracUtils;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (DEBUG) Log.d(TAG, "onReceive().");

        mDiracUtils = new DiracUtils(context);

        mDiracUtils.setEnabled(mDiracUtils.isDiracEnabled());
        mDiracUtils.setHeadsetType(mDiracUtils.getHeadsetType());
        mDiracUtils.setLevel(mDiracUtils.getLevel());
    }
}
