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

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

import org.lineageos.settings.R;

import static org.lineageos.settings.BuildConfig.DEBUG;

@AndroidEntryPoint(TileService.class)
public final class RamPlusService extends Hilt_RamPlusService {
    private static final String TAG = "RamPlusService";

    @Inject
    RamPlusManager mRamPlusManager;

    @Override
    public void onTileAdded() {
        if (DEBUG) Log.d(TAG, "Tile added.");
        syncTile();
    }

    @Override
    public void onStartListening() {
        if (DEBUG) Log.d(TAG, "Tile is listening.");
        syncTile();
    }

    @Override
    public void onClick() {
        final RamPlusMode nextMode = mRamPlusManager.getNextMode(mRamPlusManager.getCurrentMode());

        if (DEBUG) Log.d(TAG, "Tile clicked : nextMode=" + nextMode);

        updateTile(nextMode);
        mRamPlusManager.setMode(nextMode);
    }

    private void updateTile(final RamPlusMode mode) {
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

    private void syncTile() {
        final RamPlusMode mode = mRamPlusManager.getCurrentMode();
        updateTile(mode);
        mRamPlusManager.setMode(mode);
    }
}
