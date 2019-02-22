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
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Html
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import de.haukesomm.vertretungsplan.plan.Plan
import de.haukesomm.vertretungsplan.helper.UpgradeHelper
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.helper.NotificationHelper
import de.haukesomm.vertretungsplan.plan.PlanDownloaderTask

class SplashActivity : AppCompatActivity() {

    private lateinit var downloader: PlanDownloaderTask

    private lateinit var preferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        UpgradeHelper.checkVersion(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHelper(this).initNotificationChannels()
        }

        beginDownload()
    }

    override fun onPause() {
        super.onPause()
        Log.i("SplashActivity", "Activity paused, cancelling Downloader...")
        cancelDownload()
    }


    private fun beginDownload() {
        downloader = PlanDownloaderTask(downloaderClient)
        downloader.execute()
    }

    private fun cancelDownload() {
        downloader.cancel(true)
    }

    private val downloaderClient = object : PlanCacheDownloaderClient(this) {

        override fun onDownloadFinished(result: List<Plan>) {
            super.onDownloadFinished(result)
            when {
                !preferences.getBoolean(
                        getString(R.string.pref_isEulaAccepted), false) -> displayEula()
                else -> launchMainActivity()
            }
        }

        override fun onReload() = beginDownload()

        override fun onCancel() = finishAffinity()
    }


    @Suppress("DEPRECATION")
    private fun displayEula() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setMessage(Html.fromHtml(getString(R.string.eula)))
        alertDialog.setCancelable(false)

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.activity_splash_eula_accept))
        { dialog, _ ->
            val editor = preferences.edit()
            editor.putBoolean(getString(R.string.pref_isEulaAccepted), true)
            editor.apply()

            dialog.dismiss()

            launchMainActivity()
        }

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.activity_splash_eula_decline))
        { _, _ ->
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