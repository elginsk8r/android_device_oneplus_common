/*
 * Copyright (C) 2021 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.doze

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import androidx.preference.*

class DozeSettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {
    private lateinit var alwaysOnDisplayPreference: SwitchPreference
    private lateinit var switchBar: View
    private lateinit var textView: TextView

    private var pickUpPreference: ListPreference? = null
    private var pocketPreference: SwitchPreference? = null

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.doze_settings)
        activity.actionBar?.setDisplayHomeAsUpEnabled(true)

        pickUpPreference = findPreference(Utils.GESTURE_PICK_UP_KEY)
        pickUpPreference?.isEnabled = dozeEnabled
        pickUpPreference?.onPreferenceChangeListener = this
        if (Utils.alwaysOnDisplayAvailable(context)) {
            pickUpPreference?.dependency = Utils.ALWAYS_ON_DISPLAY
        }

        pocketPreference = findPreference(Utils.GESTURE_POCKET_KEY)
        pocketPreference?.isEnabled = dozeEnabled
        pocketPreference?.onPreferenceChangeListener = this
        if (Utils.alwaysOnDisplayAvailable(context)) {
            pocketPreference?.dependency = Utils.ALWAYS_ON_DISPLAY
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        handler.post { Utils.checkDozeService(context) }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                activity.onBackPressed()
                true
            }
            else -> false
        }
    }
}
