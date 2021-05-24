/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.device;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.evervolv.internal.util.FileUtils;
import org.lineageos.settings.device.utils.DozeUtils;

public class DozeSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {

    private ListPreference mPickUpPreference;
    private SwitchPreference mHandwavePreference;
    private SwitchPreference mPocketPreference;
    private SwitchPreference mFingerprintPreference;

    private Handler mHandler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.doze_settings);
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        boolean dozeEnabled = DozeUtils.isDozeEnabled(getActivity());
        int availableSensors = DozeUtils.getAvailableSensors(getActivity());

        mPickUpPreference = (ListPreference) findPreference(DozeUtils.GESTURE_PICK_UP_KEY);
        mPickUpPreference.setEnabled(dozeEnabled);
        mPickUpPreference.setOnPreferenceChangeListener(this);
        if (availableSensors != 1 && availableSensors != 3) {
            getPreferenceScreen().removePreference(mPickUpPreference);
        }

        mHandwavePreference = (SwitchPreference) findPreference(DozeUtils.GESTURE_HAND_WAVE_KEY);
        mHandwavePreference.setEnabled(dozeEnabled);
        mHandwavePreference.setOnPreferenceChangeListener(this);
        if (availableSensors != 2 && availableSensors != 3) {
            getPreferenceScreen().removePreference(mHandwavePreference);
        }

        mPocketPreference = (SwitchPreference) findPreference(DozeUtils.GESTURE_POCKET_KEY);
        mPocketPreference.setEnabled(dozeEnabled);
        mPocketPreference.setOnPreferenceChangeListener(this);
        if (availableSensors != 2 && availableSensors != 3) {
            getPreferenceScreen().removePreference(mPocketPreference);
        }

        mFingerprintPreference = (SwitchPreference) findPreference(DozeUtils.GESTURE_FP_POCKET_KEY);
        mFingerprintPreference.setEnabled(dozeEnabled);
        mFingerprintPreference.setOnPreferenceChangeListener(this);
        if (!FileUtils.fileExists(DozeUtils.FP_PROXIMITY_STATE)) {
            getPreferenceScreen().removePreference(mFingerprintPreference);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        mHandler.post(() -> DozeUtils.checkService(getActivity()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }
}
