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

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

public final class RamPlusService extends TileService {
    private static final String TAG = "RamPlus";
    private static final boolean DEBUG = true;

    private static final String KEY_MODE = "key_ram_plus_mode";
    private static final String SWAP_FREE_LOW_PERCENTAGE_PROP =
        "persist.device_config.lmkd_native.swap_free_low_percentage";

    private enum Mode { MODERATE, SLIGHT, EXTREME }

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
        final String mode = getSharedPrefs().getString(KEY_MODE, null);
        return mode != null ? Mode.valueOf(mode) : Mode.MODERATE;
    }

    private void setMode(final Mode mode) {
        final int oldPercentage = PartsUtils.getintProp(SWAP_FREE_LOW_PERCENTAGE_PROP, -1);
        final int newPercentage = getResources().getIntArray(
                R.array.ramplus_swap_free_low_percentages)[mode.ordinal()];

        // Set value only if it differs from the current one, otherwise we will
        // pointlessly trigger LMK reinit
        if (oldPercentage != newPercentage) {
            PartsUtils.setintProp(SWAP_FREE_LOW_PERCENTAGE_PROP, newPercentage);
        }
        getSharedPrefs().edit().putString(KEY_MODE, mode.name()).apply();
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

    private SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
