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
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.helper.*
import de.haukesomm.vertretungsplan.plan.*

class SplashActivity : AppCompatActivity(), PlanDownloaderClient {

    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private val themeHelper = ThemeHelper()

    private lateinit var downloader: PlanDownloaderTask


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setTheme()

        UpgradeHelper.checkVersion(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHelper(this).initNotificationChannels()
        }

        if (ConfigCatHelper().isEndOfLifeReached()) {
            displayEndOfLifeNotice()
        } else {
            beginDownload()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i("SplashActivity", "Activity paused, cancelling Downloader...")
        cancelDownloadIfNecessary()
    }


    private fun displayEndOfLifeNotice() {
        val launchIntent = Intent(this, EndOfLifeActivity::class.java)
        startActivity(launchIntent)
    }


    private fun beginDownload() {
        downloader = PlanDownloaderTask(this)
        downloader.execute()
    }

    private fun cancelDownloadIfNecessary() {
        if (this::downloader.isInitialized) {
            downloader.cancel(true)
        }
    }

    override fun onPlanDownloadSucceeded(result: List<Plan>) {
        PlanCache.reset(result)

        val isEulaAccepted = preferences.getBoolean(getString(R.string.pref_isEulaAccepted), false)
        when {
            isEulaAccepted -> launchMainActivity()
            else -> displayEula()
        }
    }

    override fun onPlanDownloadFailed() {
        val dialog = PlanDownloaderHelper.getErrorDialogTemplate(this)
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.dialog_downloader_plan_error_reload)) { d, _ ->
            d.dismiss()
            beginDownload()
        }
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.dialog_downloader_plan_error_cancel)) { d, _ ->
            d.dismiss()
            finishAffinity()
        }
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.dialog_downloader_plan_error_website)) { d, _ ->
            d.dismiss()
            ActivityHelper(this).launch(Intent(Intent.ACTION_VIEW, Uri.parse(Plan.homepage)))
        }
        dialog.show()
    }


    private fun setTheme() {
        val darkModeBehavior = preferences.getString(getString(R.string.pref_darkModeBehavior), "")!!
        themeHelper.setDarkModeBehavior(darkModeBehavior)
    }


    @Suppress("DEPRECATION")
    private fun displayEula() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setMessage(Html.fromHtml(getString(R.string.eula)))
        alertDialog.setCancelable(false)

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.activity_splash_eula_accept)) { dialog, _ ->
            val editor = preferences.edit()
            editor.putBoolean(getString(R.string.pref_isEulaAccepted), true)
            editor.apply()

            dialog.dismiss()

            launchMainActivity()
        }

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.activity_splash_eula_decline)) { _, _ ->
            finishAffinity()
        }

        alertDialog.show()
    }


    private fun launchMainActivity() {
        val launchIntent = Intent(this, PlanActivity::class.java)
        startActivity(launchIntent)
        overridePendingTransition(android.R.anim.fade_in, R.anim.none)
    }
}