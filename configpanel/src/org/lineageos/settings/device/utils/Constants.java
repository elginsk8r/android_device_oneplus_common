/*
 * Copyright (C) 2015 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.AmbientDisplayConfiguration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.evervolv.internal.util.PackageManagerUtils;

import org.lineageos.settings.device.DozeService;

import static android.provider.Settings.Secure.DOZE_ALWAYS_ON;
import static android.provider.Settings.Secure.DOZE_ENABLED;

public class Constants {

    // Preference keys
    public static final String OCLICK_CONNECT_KEY = "oclick_connect";
    public static final String OCLICK_DEVICE_ADDRESS_KEY = "oclick_device_address";
    public static final String OCLICK_SNAPSHOT_KEY = "oclick_take_snapshot";
    public static final String OCLICK_FIND_PHONE_KEY = "oclick_find_my_phone";
    public static final String OCLICK_FENCE_KEY = "oclick_fence";
    public static final String OCLICK_DISCONNECT_ALERT_KEY = "oclick_disconnect_alert";
    public static final String BUTTON_SWAP_KEY = "button_swap";
    public static final String NOTIF_SLIDER_TOP_KEY = "keycode_top_position";
    public static final String NOTIF_SLIDER_MIDDLE_KEY = "keycode_middle_position";
    public static final String NOTIF_SLIDER_BOTTOM_KEY = "keycode_bottom_position";

    // Button nodes
    public static final String BUTTON_SWAP_NODE = "/proc/s1302/key_rep";
    public static final String NOTIF_SLIDER_TOP_NODE = "/proc/tri-state-key/keyCode_top";
    public static final String NOTIF_SLIDER_MIDDLE_NODE = "/proc/tri-state-key/keyCode_middle";
    public static final String NOTIF_SLIDER_BOTTOM_NODE = "/proc/tri-state-key/keyCode_bottom";

    // Pocket mode
    public static final String POCKETMODE_KEY = "pocketmode_service";
    public static final String ACTION_POCKETMODE_UPDATE = "org.lineageos.pocketmode.UPDATE";

    // Doze preference keys
    public static final String ALWAYS_ON_DISPLAY = "always_on_display";
    public static final String CATEG_PICKUP_SENSOR = "pickup_sensor";
    public static final String CATEG_PROX_SENSOR = "proximity_sensor";
    public static final String GESTURE_PICK_UP_KEY = "gesture_pick_up";
    public static final String GESTURE_HAND_WAVE_KEY = "gesture_hand_wave";
    public static final String GESTURE_POCKET_KEY = "gesture_pocket";

    // Holds <preference_key> -> <proc_node> mapping
    public static final Map<String, String> sBooleanNodePreferenceMap = new HashMap<>();
    public static final Map<String, String> sStringNodePreferenceMap = new HashMap<>();

    // Holds <preference_key> -> <default_values> mapping
    public static final Map<String, Object> sNodeDefaultMap = new HashMap<>();

    public static final String[] sButtonPrefKeys = {
        BUTTON_SWAP_KEY,
        NOTIF_SLIDER_TOP_KEY,
        NOTIF_SLIDER_MIDDLE_KEY,
        NOTIF_SLIDER_BOTTOM_KEY
    };

    static {
        sBooleanNodePreferenceMap.put(BUTTON_SWAP_KEY, BUTTON_SWAP_NODE);
        sStringNodePreferenceMap.put(NOTIF_SLIDER_TOP_KEY, NOTIF_SLIDER_TOP_NODE);
        sStringNodePreferenceMap.put(NOTIF_SLIDER_MIDDLE_KEY, NOTIF_SLIDER_MIDDLE_NODE);
        sStringNodePreferenceMap.put(NOTIF_SLIDER_BOTTOM_KEY, NOTIF_SLIDER_BOTTOM_NODE);

        sNodeDefaultMap.put(BUTTON_SWAP_KEY, false);
        sNodeDefaultMap.put(NOTIF_SLIDER_TOP_KEY, "601");
        sNodeDefaultMap.put(NOTIF_SLIDER_MIDDLE_KEY, "602");
        sNodeDefaultMap.put(NOTIF_SLIDER_BOTTOM_KEY, "603");

        sNodeDefaultMap.put(OCLICK_FENCE_KEY, true);
        sNodeDefaultMap.put(OCLICK_DISCONNECT_ALERT_KEY, true);
    }

    public static boolean isPreferenceEnabled(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, (Boolean) sNodeDefaultMap.get(key));
    }

    public static String getPreferenceString(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, (String) sNodeDefaultMap.get(key));
    }

    public static boolean hasPocketMode(Context context) {
        return PackageManagerUtils.isAppInstalled(context, "org.lineageos.pocketmode");
    }

    public static void updatePocketMode(Context context, boolean value) {
        final Intent intent = new Intent(ACTION_POCKETMODE_UPDATE);
        intent.putExtra("enable", value);
        context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    public static void checkDozeService(Context context) {
        final boolean dozeEnabled = isDozeEnabled(context);
        final boolean sensorsEnabled = isPickUpEnabled(context) || isHandwaveEnabled(context)
                || isPocketEnabled(context);
        final boolean alwaysOnEnabled = isAlwaysOnEnabled(context);

        if (dozeEnabled && sensorsEnabled && !alwaysOnEnabled) {
            context.startServiceAsUser(new Intent(context, DozeService.class),
                    UserHandle.CURRENT);
        } else {
            context.stopServiceAsUser(new Intent(context, DozeService.class),
                    UserHandle.CURRENT);
        }
    }

    public static boolean isDozeEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                DOZE_ENABLED, 1) != 0;
    }

    public static boolean enableDoze(Context context, boolean enable) {
        return Settings.Secure.putInt(context.getContentResolver(),
                DOZE_ENABLED, enable ? 1 : 0);
    }

    public static void launchDozePulse(Context context) {
        context.sendBroadcastAsUser(new Intent("com.android.systemui.doze.pulse"),
                new UserHandle(UserHandle.USER_CURRENT));
    }

    public static boolean enableAlwaysOn(Context context, boolean enable) {
        return Settings.Secure.putIntForUser(context.getContentResolver(),
                DOZE_ALWAYS_ON, enable ? 1 : 0, UserHandle.USER_CURRENT);
    }

    public static boolean isAlwaysOnEnabled(Context context) {
        final boolean enabledByDefault = context.getResources()
                .getBoolean(com.android.internal.R.bool.config_dozeAlwaysOnEnabled);
        final boolean alwaysOnAvailable = new AmbientDisplayConfiguration(context)
                .alwaysOnAvailable();

        return Settings.Secure.getIntForUser(context.getContentResolver(),
                DOZE_ALWAYS_ON, alwaysOnAvailable && enabledByDefault ? 1 : 0,
                UserHandle.USER_CURRENT) != 0;
    }

    public static boolean isPickUpEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(GESTURE_PICK_UP_KEY, false);
    }

    public static boolean isHandwaveEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(GESTURE_HAND_WAVE_KEY, false);
    }

    public static boolean isPocketEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(GESTURE_POCKET_KEY, false);
    }

    public static Sensor getSensor(SensorManager sm, String type) {
        if (TextUtils.isEmpty(type)) {
            return null;
        }
        for (Sensor s : sm.getSensorList(Sensor.TYPE_ALL)) {
            if (type.equals(s.getStringType())) {
                return s;
            }
        }
        return null;
    }
}
