/*
 * Copyright (c) 2015 The CyanogenMod Project
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

package org.lineageos.settings.device.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.device.utils.Constants;

import com.evervolv.internal.util.FileUtils;

public class PocketSensorListener implements SensorEventListener {

    private static final boolean DEBUG = false;
    private static final String TAG = "PocketSensorListener";

    private static final String FPC_FILE = Constants.FP_PROXIMITY_STATE_NODE;

    // Maximum time for the hand to cover the sensor: 1s
    private static final int HANDWAVE_MAX_DELTA_NS = 1000 * 1000 * 1000;
    // Minimum time until the device is considered to have been in the pocket: 2s
    private static final int POCKET_MIN_DELTA_NS = 2000 * 1000 * 1000;

    private Context mContext;
    private boolean mSawNear = false;
    private long mInPocketTime = 0;

    public PocketSensorListener(Context context) {
        mContext = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mSawNear && event.values[0] != 1) {
            long delta = event.timestamp - mInPocketTime;
            if (Constants.isHandwaveEnabled(mContext) && Constants.isPocketEnabled(mContext)) {
                Constants.launchDozePulse(mContext);
            } else if (Constants.isHandwaveEnabled(mContext)) {
                if (delta < HANDWAVE_MAX_DELTA_NS) {
                    Constants.launchDozePulse(mContext);
                }
            } else if (Constants.isPocketEnabled(mContext)) {
                if (delta  >= POCKET_MIN_DELTA_NS) {
                    Constants.launchDozePulse(mContext);
                }
            }
        }
        mInPocketTime = event.timestamp;
        mSawNear = event.values[0] == 1;

        boolean updateEnabled = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean(Constants.POCKETMODE_KEY, false);
        if (!updateEnabled)
            return;

        if (FileUtils.isFileWritable(FPC_FILE)) {
            FileUtils.writeLine(FPC_FILE, mSawNear ? "1" : "0");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* Empty */
    }
}
