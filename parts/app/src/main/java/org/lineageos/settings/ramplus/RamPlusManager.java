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

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.service.quicksettings.TileService;
import android.util.Log;

import dagger.hilt.android.qualifiers.ApplicationContext;

import javax.inject.Inject;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

import static org.lineageos.settings.BuildConfig.DEBUG;

public final class RamPlusManager {
    private static final String TAG = "RamPlusManager";

    private final Context mContext;
    private final SharedPreferences mSharedPrefs;
    private final StatusBarManager mStatusBarManager;
    private final Resources mResources;
    private final ComponentName mTileComponent;

    @Inject
    public RamPlusManager(final @ApplicationContext Context context,
            final SharedPreferences sharedPrefs, final StatusBarManager statusBarManager) {

        mContext = context;
        mSharedPrefs = sharedPrefs;
        mStatusBarManager = statusBarManager;
        mResources = context.getResources();
        mTileComponent = new ComponentName(context, RamPlusService.class);
    }

    public void onBootCompleted() {
        if (DEBUG) Log.d(TAG, "onBootCompleted().");

        setMode(getCurrentMode());
        refreshTileService();
    }

    RamPlusMode getNextMode(final RamPlusMode currentMode) {
        if (DEBUG) Log.d(TAG, String.format("getNextMode(currentMode=%s).", currentMode));

        switch (currentMode) {
            case MODERATE:
                return RamPlusMode.SLIGHT;
            case SLIGHT:
                return RamPlusMode.EXTREME;
        }
        return RamPlusMode.MODERATE;
    }

    RamPlusMode getCurrentMode() {
        final String mode = mSharedPrefs.getString(mResources.getString(
                    R.string.ramplus_key_mode), null);

        if (DEBUG) Log.d(TAG, "getCurrentMode() : mode=" + mode);

        return mode != null ? RamPlusMode.valueOf(mode) : RamPlusMode.MODERATE;
    }

    void setMode(final RamPlusMode mode) {
        if (DEBUG) Log.d(TAG, String.format("setMode(mode=%s).", mode));

        final int modeIdx = mode.ordinal();

        final var defaultLowPercent = mResources.getIntArray(
                R.array.ramplus_swap_free_low_percentages)[modeIdx];
        setSwapFreeLowPercentage(getModeSwapFreeLowPercentageSetting(mode, defaultLowPercent));
        setLowMemMinOomScore(mResources.getIntArray(
                    R.array.ramplus_lowmem_min_oom_score_values)[modeIdx]);

        mSharedPrefs.edit().putString(mResources.getString(R.string.ramplus_key_mode),
                mode.name()).apply();
    }

    void refreshTileService() {
        TileService.requestListeningState(mContext, mTileComponent);
    }

    void requestAddTileService() {
        final var label = mResources.getString(R.string.ramplus_qs_title);
        final var icon = Icon.createWithResource(mContext, R.drawable.ic_ramplus);
        mStatusBarManager.requestAddTileService(mTileComponent, label, icon,
                mContext.getMainExecutor(), (result) -> {});
    }

    private void setSwapFreeLowPercentage(final int value) {
        if (DEBUG) Log.d(TAG, String.format("setSwapFreeLowPercentage(value=%d).", value));

        final String prop = mResources.getString(R.string.ramplus_prop_swap_free_low_percentage);
        final int oldPercentage = PartsUtils.getintProp(prop, -1);

        // Set value only if it differs from the current one, otherwise we will pointlessly trigger
        // LMK reinit
        if (oldPercentage != value) {
            PartsUtils.setintProp(prop, value);
        }
    }

    private void setLowMemMinOomScore(final int value) {
        if (DEBUG) Log.d(TAG, String.format("setLowMemMinOomScore(value=%d).", value));

        final String prop = mResources.getString(R.string.ramplus_prop_lowmem_min_oom_score);
        final int oldValue = PartsUtils.getintProp(prop, -1);
        if (oldValue != value) {
            PartsUtils.setintProp(prop, value);
        }
    }

    private int getModeSwapFreeLowPercentageSetting(final RamPlusMode mode, final int defValue) {
        final var key = switch (mode) {
            case MODERATE -> R.string.ramplus_slider_moderate_mode_percentage_key;
            case SLIGHT -> R.string.ramplus_slider_slight_mode_percentage_key;
            case EXTREME -> R.string.ramplus_slider_extreme_mode_percentage_key;
            default -> throw new IllegalArgumentException("Unhandled mode: " + mode);
        };
        return mSharedPrefs.getInt(mResources.getString(key), defValue);
    }
}
