package org.lineageos.settings.batterylow;

import android.os.Bundle;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint(CollapsingToolbarBaseActivity.class)
public class BatteryLowActivity extends Hilt_BatteryLowActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(
                    com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                    new BatteryLowFragment()).commit();
        }
    }
}
