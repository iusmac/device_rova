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

package org.lineageos.settings.ramplus;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

import static org.lineageos.settings.BuildConfig.DEBUG;

@AndroidEntryPoint(TileService.class)
public final class RamPlusService extends Hilt_RamPlusService {
    private static final String TAG = "RamPlus";

    private enum Mode { MODERATE, SLIGHT, EXTREME }

    @Inject
    SharedPreferences mSharedPrefs;

    @Override
    public void onTileAdded() {
        if (DEBUG) Log.d(TAG, "Tile added.");
        sync();
    }

    @Override
    public void onStartListening() {
        if (DEBUG) Log.d(TAG, "Tile is listening.");
        sync();
    }

    @Override
    public void onClick() {
        final Mode nextMode;
        switch (getMode()) {
            case MODERATE:
                nextMode = Mode.SLIGHT;
                break;

            case SLIGHT:
                nextMode = Mode.EXTREME;
                break;

            default:
                nextMode = Mode.MODERATE;
        }

        if (DEBUG) Log.d(TAG, "Tile clicked : nextMode=" + nextMode);

        updateTile(nextMode);
        setMode(nextMode);
    }

    private Mode getMode() {
        final String mode = mSharedPrefs.getString(getString(R.string.ramplus_key_mode), null);
        return mode != null ? Mode.valueOf(mode) : Mode.MODERATE;
    }

    private void setMode(final Mode mode) {
        final String prop = getString(R.string.ramplus_prop_swap_free_low_percentage);
        final int oldPercentage = PartsUtils.getintProp(prop, -1);
        final int newPercentage = getResources().getIntArray(
                R.array.ramplus_swap_free_low_percentages)[mode.ordinal()];

        // Set value only if it differs from the current one, otherwise we will
        // pointlessly trigger LMK reinit
        if (oldPercentage != newPercentage) {
            PartsUtils.setintProp(prop, newPercentage);
        }
        mSharedPrefs.edit().putString(getString(R.string.ramplus_key_mode), mode.name()).apply();
    }

    private void updateTile(final Mode mode) {
        final Resources res = getResources();
        final TypedArray icons = res.obtainTypedArray(R.array.ramplus_mode_icons);
        final int iconResId = icons.getResourceId(mode.ordinal(), -1);
        icons.recycle();

        final Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.setIcon(Icon.createWithResource(this, iconResId));
        tile.setSubtitle(res.getStringArray(R.array.ramplus_mode_names)[mode.ordinal()]);
        tile.updateTile();
    }

    private void sync() {
        final Mode mode = getMode();
        updateTile(mode);
        setMode(mode);
    }

    public static void sync(final Context context) {
        TileService.requestListeningState(context, new ComponentName(context,
                    RamPlusService.class));
    }
}
