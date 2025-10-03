package org.lineageos.settings.batterylow;

import android.content.Context;
import android.content.SharedPreferences;

import dagger.hilt.android.qualifiers.ApplicationContext;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.lineageos.settings.PartsUtils;
import org.lineageos.settings.R;

@Singleton
public class BatteryLow {
    private final Context mContext;
    private final Provider<SharedPreferences> mSharedPrefsProvider;
    private final int mBatteryLevelDefaultThreshold;
    private String mSeekBarPreferenceKey;

    @Inject
    BatteryLow(final @ApplicationContext Context context,
            final Provider<SharedPreferences> sharedPrefsProvider) {

        mContext = context;
        mSharedPrefsProvider = sharedPrefsProvider;

        mBatteryLevelDefaultThreshold = context.getResources()
            .getInteger(R.integer.batterylow_level_default_threshold);
        mSeekBarPreferenceKey = context.getString(R.string.batterylow_level_service_key_seekbar);
    }

    public void onBootCompleted() {
        if (isEnabled()) {
            startService();
        }
    }

    String getSeekBarPreferenceKey() {
        return mSeekBarPreferenceKey;
    }

    void startService() {
        PartsUtils.startService(mContext, BatteryLowService.class);
    }

    void stopService() {
        PartsUtils.stopService(mContext, BatteryLowService.class);
    }

    int getBatteryLevelLowThreshold() {
        return mSharedPrefsProvider.get().getInt(mSeekBarPreferenceKey,
                mBatteryLevelDefaultThreshold);
    }

    private boolean isEnabled() {
        return getBatteryLevelLowThreshold() != 0;
    }
}
