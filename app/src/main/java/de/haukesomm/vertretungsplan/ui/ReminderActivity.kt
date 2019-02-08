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

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.background.FetchRule
import de.haukesomm.vertretungsplan.background.FetchServiceManager


private const val REQUEST_CREATE_REMINDER = 1000


class ReminderActivity : AppCompatActivity(), FetchRuleAdapter.Client {

    private lateinit var prefs: SharedPreferences


    // Uses AlarmManager system-service which cannot be used before onCreate() was called
    private val fetchServiceManager by lazy { FetchServiceManager(this) }

    private lateinit var ruleAdapter: FetchRuleAdapter


    private val ruleListView by lazy { findViewById<ListView>(R.id.activity_reminder_rules_list) }

    private val fab by lazy { findViewById<FloatingActionButton>(R.id.fab) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        initToolbar()
        initRuleList()
        initFab()

        showNewReminderBehaviorDialogIfRequested()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> this.finish()
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CREATE_REMINDER
                && resultCode == CreateReminderActivity.RESULT_SCHEDULED) {
            refreshRuleList()
        }
    }


    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.activity_reminder)
    }

    private fun initRuleList() {
        ruleAdapter = FetchRuleAdapter(this, fetchServiceManager.listScheduled().toMutableList(), this)
        ruleListView.adapter = ruleAdapter
        ruleListView.emptyView = findViewById(R.id.activity_reminder_rules_list_empty)
    }

    private fun initFab() {
        fab.setOnClickListener {
            val createIntent = Intent(this, CreateReminderActivity::class.java)
            startActivityForResult(createIntent, REQUEST_CREATE_REMINDER)
        }
    }


    private fun refreshRuleList() {
        ruleAdapter.removeAll()
        fetchServiceManager.listScheduled().toMutableList().forEach { ruleAdapter.add(it) }
        ruleAdapter.notifyDataSetChanged()
    }


    override fun onDeleteRule(rule: FetchRule) {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.activity_reminder_delete_confirm_title))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.activity_reminder_rules_delete_confirm_action))
                { _, _ ->
                    fetchServiceManager.cancel(rule.id)
                    ruleAdapter.remove(rule)
                    ruleAdapter.notifyDataSetChanged()
                }
                .setNegativeButton(getString(R.string.activity_reminder_rules_delete_keep))
                { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }


    private fun showNewReminderBehaviorDialogIfRequested() {
        val shouldDisplay = prefs.getBoolean(
                getString(R.string.pref_upgrade_showNewReminderBehaviorDialog), false)
        if (shouldDisplay) {
            val dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.activity_reminder_newReminderBehaviorDialog_title)
                    .setMessage(R.string.activity_reminder_newReminderBehaviorDialog_text)
                    .setPositiveButton(R.string.activity_reminder_newReminderBehaviorDialog_dismiss)
                    { dialog: DialogInterface, _ ->
                        prefs.edit()
                                .putBoolean(
                                        getString(R.string.pref_upgrade_showNewReminderBehaviorDialog),
                                        false)
                                .apply()
                        dialog.dismiss()
                    }
                    .setCancelable(false)
            dialog.show()
        }
    }
}