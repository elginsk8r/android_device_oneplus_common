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

package org.lineageos.settings.device.utils;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.AmbientDisplayConfiguration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.device.DozeService;

import java.util.List;

public final class DozeUtils {

    private static final String TAG = "DozeUtils";
    private static final boolean DEBUG = false;

    private static final String DOZE_INTENT = "com.android.systemui.doze.pulse";

    public static final String ALWAYS_ON_DISPLAY = "always_on_display";
    public static final String GESTURE_PICK_UP_KEY = "gesture_pick_up_type";
    public static final String GESTURE_HAND_WAVE_KEY = "gesture_hand_wave";
    public static final String GESTURE_POCKET_KEY = "gesture_pocket";
    public static final String GESTURE_FP_POCKET_KEY = "gesture_fp_pocket";

    public static final String FP_PROXIMITY_STATE = "/sys/devices/soc/soc:fpc_fpc1020/proximity_state";

    public static void checkService(Context context) {
        if (isDozeEnabled(context) && sensorsEnabled(context) && !isAlwaysOnEnabled(context)) {
            context.startServiceAsUser(new Intent(context, DozeService.class),
                    UserHandle.CURRENT);
        } else {
            context.stopServiceAsUser(new Intent(context, DozeService.class),
                    UserHandle.CURRENT);
        }
    }

    public static boolean isDozeEnabled(Context context) {
        return new AmbientDisplayConfiguration(context).pulseOnNotificationEnabled(UserHandle.USER_CURRENT);
    }

    public static void launchDozePulse(Context context) {
        if (DEBUG) Log.d(TAG, "Launch doze pulse");
        context.sendBroadcastAsUser(new Intent(DOZE_INTENT),
                new UserHandle(UserHandle.USER_CURRENT));
    }

    public static boolean isAlwaysOnEnabled(Context context) {
        return new AmbientDisplayConfiguration(context).alwaysOnEnabled(UserHandle.USER_CURRENT);
    }

    public static boolean alwaysOnDisplayAvailable(Context context) {
        return new AmbientDisplayConfiguration(context).alwaysOnAvailable();
    }

    public static boolean isGestureEnabled(Context context, String gesture) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(gesture, false);
    }

    public static boolean isPickUpEnabled(Context context) {
        return !PreferenceManager.getDefaultSharedPreferences(context)
                .getString(GESTURE_PICK_UP_KEY, "0").equals("0");
    }

    public static boolean isPickUpSetToWake(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(GESTURE_PICK_UP_KEY, "0").equals("2");
    }

    public static boolean isHandwaveGestureEnabled(Context context) {
        return isGestureEnabled(context, GESTURE_HAND_WAVE_KEY);
    }

    public static boolean isPocketGestureEnabled(Context context) {
        return isGestureEnabled(context, GESTURE_POCKET_KEY);
    }

    public static boolean isFingerprintPocketGestureEnabled(Context context) {
        return isGestureEnabled(context, GESTURE_FP_POCKET_KEY);
    }

    public static boolean sensorsEnabled(Context context) {
        return isPickUpEnabled(context) || isHandwaveGestureEnabled(context)
                || isPocketGestureEnabled(context);
    }

    public static Sensor findSensorWithType(SensorManager sensorManager, String type) {
        if (TextUtils.isEmpty(type)) {
            return null;
        }
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensorList) {
            if (type.equals(s.getStringType())) {
                return s;
            }
        }
        return null;
    }
}
