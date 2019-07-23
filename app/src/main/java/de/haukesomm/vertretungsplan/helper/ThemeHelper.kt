package de.haukesomm.vertretungsplan.helper

import androidx.appcompat.app.AppCompatDelegate

class ThemeHelper {

    fun setDarkModeBehavior(identifier: String) {
        // Identifiers are specified in the preferences.xml file!
        AppCompatDelegate.setDefaultNightMode(when(identifier) {
            "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            "always" -> AppCompatDelegate.MODE_NIGHT_YES
            "never" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        })
    }
}