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

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.background.FetchServiceManager
import de.haukesomm.vertretungsplan.helper.ActivityHelper
import de.haukesomm.vertretungsplan.plan.*


private const val REQUEST_SETTINGS_ACTIVITY = 1000


class PlanActivity : AppCompatActivity(), PlanDownloaderClient {

    private val activityHelper by lazy { ActivityHelper(this) }

    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }


    private val drawerLayout by lazy { findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.activity_plan_drawerLayout) }

    private val navigationView by lazy { findViewById<NavigationView>(R.id.activity_plan_navigationView) }


    private lateinit var activeFragment: Fragment

    private val planEntriesFragment by lazy { PlanEntriesFragment() }

    private val planMessagesFragment by lazy { PlanMessagesFragment() }


    private val bottomNavigation by lazy { findViewById<BottomNavigationView>(R.id.activity_plan_bottomNavigation) }

    private lateinit var messagesBadge: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan)

        // Force-stop onCreate() and launch SplashActivity instead if PlanCache is isEmpty
        // (e.g. object has been destroyed due to application pause)
        if (PlanCache.isEmpty) {
            Log.d("PlanActivity", "PlanCache is isEmpty, reloading via SplashActivity")
            activityHelper.launch(SplashActivity::class.java)
            finish()
            return
        }

        initToolbar()
        initDrawer()
        initFragments()
        initMessagesBadge()
    }


    override fun onResume() {
        super.onResume()

        val fetchServiceManager = FetchServiceManager(this)
        fetchServiceManager.dismissAllNotifications()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_plan_top, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
            R.id.menu_activity_plan_reload -> reloadPlans()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SETTINGS_ACTIVITY && activeFragment is PlanEntriesFragment) {
            (activeFragment as PlanEntriesFragment).triggerCourseFilterUpdate()
        }
    }


    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
    }


    private val drawerSelectionListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.drawer_activity_plan_silentmode -> { /* Do nothing. See initSilentMode() */ }
            R.id.drawer_activity_plan_reminder -> activityHelper.launch(ReminderActivity::class.java)
            R.id.drawer_activity_plan_preferences ->
                activityHelper.launch(PreferenceActivity::class.java, REQUEST_SETTINGS_ACTIVITY)
            R.id.drawer_activity_plan_about -> activityHelper.launch(InfoActivity::class.java)
            R.id.drawer_activity_homepage -> {
                activityHelper.launch(
                        Intent(Intent.ACTION_VIEW, Uri.parse(Plan.homepage)))
            }
            R.id.drawer_activity_plan_rate -> {
                activityHelper.launch(
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            }
        }

        drawerLayout.closeDrawers()
        true
    }

    private fun initDrawer() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_toolbar)

        navigationView.setNavigationItemSelectedListener(drawerSelectionListener)

        initSilentMode()
    }

    private fun initSilentMode() {
        val silentmode = navigationView.menu.findItem(R.id.drawer_activity_plan_silentmode)
        val silentmodeSwitch = silentmode.actionView
                .findViewById<Switch>(R.id.drawer_activity_plan_silentmode_switch)

        silentmodeSwitch.isChecked =
                preferences.getBoolean(getString(R.string.pref_isSilentModeEnabled), false)
        silentmodeSwitch.setOnClickListener { toggleSilentMode() }
    }

    private fun toggleSilentMode() {
        val switch = navigationView.findViewById<Switch>(R.id.drawer_activity_plan_silentmode_switch)

        preferences.edit()
                .putBoolean(getString(R.string.pref_isSilentModeEnabled), switch.isChecked)
                .apply()

        if (switch.isChecked) {
            Toast.makeText(this, R.string.notifications_silentMode_info, Toast.LENGTH_LONG).show()
        }
    }


    private fun handleFragmentTransition(id: Int, animate: Boolean) {
        val fragment: Fragment = when (id) {
            R.id.menu_activity_plan_entries -> planEntriesFragment
            R.id.menu_activity_plan_messages -> planMessagesFragment
            else -> return
        }

        val transaction = supportFragmentManager
                .beginTransaction().replace(R.id.activity_plan_fragmentHolder, fragment)
        if (animate) {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }
        transaction.commit()

        activeFragment = fragment
    }

    private fun initFragments() {
        // Fragment to show on startup:
        handleFragmentTransition(R.id.menu_activity_plan_entries, false)

        bottomNavigation.setOnNavigationItemSelectedListener {
            handleFragmentTransition(it.itemId, true)
            hideMessagesBadge()
            true
        }

        // Ignore reselection of already active Fragments
        bottomNavigation.setOnNavigationItemReselectedListener {}
    }


    private fun initMessagesBadge() {
        // Workaround until badges are officially part of the support library

        val menuView = bottomNavigation.getChildAt(0) as BottomNavigationMenuView
        val messagesItemView = menuView.getChildAt(1) as BottomNavigationItemView

        messagesBadge = LayoutInflater.from(this).inflate(R.layout.activity_plan_badge,
                menuView, false)
        messagesBadge.visibility = View.INVISIBLE

        val badgePadding =
                24 * (resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        messagesBadge.setPadding(badgePadding, 0, 0, 0)

        messagesItemView.addView(messagesBadge)


        showMessageBadgeIfAvailable()
    }

    private fun showMessageBadgeIfAvailable() {
        if (PlanCache.getAll().any { it.message.isNotBlank() }
                && activeFragment !is PlanMessagesFragment) {
            showMessagesBadge()
        }
    }

    private fun showMessagesBadge() {
        messagesBadge.visibility = View.VISIBLE
    }

    private fun hideMessagesBadge() {
        messagesBadge.visibility = View.INVISIBLE
    }


    private fun reloadPlans() = PlanDownloaderTask(this).execute()

    override fun onPlanDownloadSucceeded(result: List<Plan>) {
        PlanCache.reset(result)
        showMessageBadgeIfAvailable()
        Toast.makeText(applicationContext, R.string.activity_main_reload_successful,
                Toast.LENGTH_LONG).show()
    }

    override fun onPlanDownloadFailed() {
        val dialog = PlanDownloaderHelper.getErrorDialogTemplate(this)
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.dialog_downloader_plan_error_reload)) { d, _ ->
            d.dismiss()
            reloadPlans()
        }
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.dialog_downloader_plan_error_cancel)) { d, _ ->
            d.dismiss()
        }
        dialog.show()
    }
}