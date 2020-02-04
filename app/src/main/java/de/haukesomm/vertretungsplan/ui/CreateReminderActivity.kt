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

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Spinner
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.background.FetchRule
import de.haukesomm.vertretungsplan.background.FetchServiceManager
import de.haukesomm.vertretungsplan.helper.TimeHelper
import de.haukesomm.vertretungsplan.plan.Grade

class CreateReminderActivity : AppCompatActivity() {

    companion object {

        const val RESULT_CANCELLED = 1000

        const val RESULT_SCHEDULED = 1010
    }


    private val timePicker by lazy { findViewById<TimePicker>(R.id.activity_reminder_create_timepicker) }

    private val gradeSpinner by lazy { findViewById<Spinner>(R.id.activity_reminder_create_grades) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reminder_create)

        initToolbar()
        initTimePicker()
        initGrades()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_reminder_create, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> exitNegativeResult()
            R.id.menu_activity_reminder_create_schedule -> exitPositiveResult()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = getString(R.string.activity_reminder_create_title)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initGrades() {
        val grades = Grade.defaults

        gradeSpinner.adapter = GradeAdapter(this, grades)

        // Use persisted (lastly selected) Grade

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val rememberGrade = prefs.getBoolean(getString(R.string.pref_isGradePersistenceEnabled),
                false)
        if (rememberGrade) {
            val name = prefs.getString(getString(R.string.pref_persistedGradeName), grades[0].name)!!
            val grade = Grade(name)

            gradeSpinner.setSelection(grades.indexOf(grade))
        }
    }

    private fun initTimePicker() {
        timePicker.setIs24HourView(true)
    }


    private fun scheduleReminder() {
        val hour = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> timePicker.hour
            else -> @Suppress("DEPRECATION") timePicker.currentHour
        }
        val minute = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> timePicker.minute
            else -> @Suppress("DEPRECATION") timePicker.currentMinute
        }

        val rule = FetchRule(TimeHelper.generateTimeBasedID(), hour, minute,
                gradeSpinner.selectedItem as Grade)

        val manager = FetchServiceManager(applicationContext)
        manager.schedule(rule)
    }


    private fun exitNegativeResult() {
        setResult(RESULT_CANCELLED)
        finish()
    }

    private fun exitPositiveResult() {
        scheduleReminder()
        setResult(RESULT_SCHEDULED)
        finish()
    }
}