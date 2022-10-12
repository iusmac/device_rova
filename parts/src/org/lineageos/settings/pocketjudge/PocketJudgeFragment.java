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
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import org.lineageos.settings.R;

public class PocketJudgeFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private final String TAG = getClass().getName();
    private final boolean DEBUG = false;

    private final String KEY_POCKET_JUDGE_FOOTER = "footer_preference";

    private Context mContext;

    private SharedPreferences mSharedPrefs;

    private TwoStatePreference mPocketJudgePreference;
    private Preference mFooterPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mContext = getContext();
        mSharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(mContext);

        addPreferencesFromResource(R.xml.pocketjudge_settings);

        mPocketJudgePreference = findPreference(PocketJudge.KEY_POCKET_JUDGE_SWITCH);
        mPocketJudgePreference.setChecked(mSharedPrefs.getBoolean(
                    PocketJudge.KEY_POCKET_JUDGE_SWITCH, false));
        mPocketJudgePreference.setOnPreferenceChangeListener(this);

        mFooterPref = findPreference(KEY_POCKET_JUDGE_FOOTER);
        final String title =
                getString(R.string.pocket_judge_demo_title_cancel) + "\n" +
                getString(R.string.pocket_judge_demo_tag_number1) +
                getString(R.string.pocket_judge_demo_tag_cancel) + "\n" +
                getString(R.string.pocket_judge_demo_tag_number2) +
                getString(R.string.pocket_judge_demo_tag_cancel2);
        mFooterPref.setTitle(title);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked = (Boolean) newValue;
        if (isChecked) {
            PocketJudge.startService(mContext);
        } else {
            PocketJudge.stopService(mContext);
        }
        mSharedPrefs.edit().putBoolean(
                PocketJudge.KEY_POCKET_JUDGE_SWITCH,
                isChecked).apply();
        return true;
    }
}
