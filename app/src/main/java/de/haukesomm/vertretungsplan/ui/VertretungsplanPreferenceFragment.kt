/*
 * Copyright 2016-2020 Hauke Sommerfeld
 *
 * This file is part of the 'Vertretungsplan-App' project:
 * https://github.com/haukesomm/Vertretungsplan-App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.haukesomm.vertretungsplan.ui

import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import de.haukesomm.vertretungsplan.R

class VertretungsplanPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(R.xml.preferences)

        // Notification settings preferences are added depending on the device's API level
        val notifications = preferenceScreen.findPreference<PreferenceGroup>(
                getString(R.string.pref_category_notifications)) as PreferenceGroup

        addPreferencesFromResource(R.xml.preferences_notifications_customize, notifications)
    }

    private fun addPreferencesFromResource(@XmlRes res: Int, group: PreferenceGroup) {
        val screen = preferenceScreen

        addPreferencesFromResource(res)

        val lastPreference = screen.preferenceCount
        while (screen.preferenceCount > lastPreference) {
            val preference = screen.getPreference(lastPreference)
            screen.removePreference(preference)

            group.addPreference(preference)
        }
    }
}
