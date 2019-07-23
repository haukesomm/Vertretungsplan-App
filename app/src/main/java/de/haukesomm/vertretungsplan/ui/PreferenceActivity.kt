/*
 * Copyright 2016-2019 Hauke Sommerfeld
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

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.helper.ThemeHelper

class PreferenceActivity : AppCompatActivity() {

    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private val themeHelper = ThemeHelper()


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        initToolbar()
        registerPreferenceListener()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> this.finish()
        }

        return super.onOptionsItemSelected(item)
    }


    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = getString(R.string.activity_preferences)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun registerPreferenceListener() {
        // A reference to the listener needs to be saved because the listener would be garbage-
        // collected otherwise. This is because the internal implementation uses a WeakHashMap.
        val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs: SharedPreferences, key: String ->
            val darkModeKey = getString(R.string.pref_darkModeBehavior)
            if (key == darkModeKey) {
                val darkModeBehavior = prefs.getString(darkModeKey, "")!!
                themeHelper.setDarkModeBehavior(darkModeBehavior)
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }
}
