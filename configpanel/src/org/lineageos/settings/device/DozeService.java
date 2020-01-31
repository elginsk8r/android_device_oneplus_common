/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2018 The LineageOS Project
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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import org.lineageos.settings.device.sensors.PickupSensorListener;
import org.lineageos.settings.device.sensors.PocketSensorListener;
import org.lineageos.settings.device.utils.Constants;

public class DozeService extends Service {
    private static final String TAG = "DozeService";
    private static final boolean DEBUG = false;

    private SensorManager mSensorManager;
    private Sensor mPickupSensor;
    private PickupSensorListener mPickupSensorListener;
    private Sensor mPocketSensor;
    private PocketSensorListener mPocketSensorListener;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");

        mSensorManager = getSystemService(SensorManager.class);

        mPickupSensor = Constants.getSensor(mSensorManager, "oneplus.sensor.pickup");
        mPickupSensorListener = new PickupSensorListener(this);

        mPocketSensor = Constants.getSensor(mSensorManager, "oneplus.sensor.pocket");
        mPocketSensorListener = new PocketSensorListener(this);

        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        super.onDestroy();
        unregisterReceiver(mScreenStateReceiver);
        mSensorManager.unregisterListener(mPickupSensorListener, mPickupSensor);
        mSensorManager.unregisterListener(mPocketSensorListener, mPocketSensor);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onDisplayOn() {
        if (Constants.isPickUpEnabled(this)) {
            mSensorManager.unregisterListener(mPickupSensorListener, mPickupSensor);
        }
        if (Constants.isHandwaveEnabled(this) || Constants.isPocketEnabled(this)) {
            mSensorManager.unregisterListener(mPocketSensorListener, mPocketSensor);
        }
    }

    private void onDisplayOff() {
        if (Constants.isPickUpEnabled(this)) {
            mSensorManager.registerListener(mPickupSensorListener, mPickupSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (Constants.isHandwaveEnabled(this) || Constants.isPocketEnabled(this)) {
            mSensorManager.registerListener(mPocketSensorListener, mPocketSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                onDisplayOn();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                onDisplayOff();
            }
        }
    };
}
