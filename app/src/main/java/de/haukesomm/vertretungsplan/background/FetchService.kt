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

package de.haukesomm.vertretungsplan.background

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import de.haukesomm.vertretungsplan.plan.PlanDownloader
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.helper.NotificationHelper
import de.haukesomm.vertretungsplan.helper.TimeHelper
import de.haukesomm.vertretungsplan.plan.Grade
import de.haukesomm.vertretungsplan.ui.SplashActivity


private const val NAME = "VertretungsplanFetchService"


class FetchService : IntentService(NAME) {

    companion object {

        const val INTENT_EXTRA_ID = "extra_id"

        const val INTENT_EXTRA_GRADE_NAME = "extra_grade"
    }


    private val preferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext) }

    private val notificationManager by lazy {
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }


    override fun onHandleIntent(intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showForegroundNotification()
        }


        val downloader = PlanDownloader()
        val plans = downloader.downloadAll()

        val gradeName = intent!!.getStringExtra(INTENT_EXTRA_GRADE_NAME)!!
        val grade = Grade(gradeName)

        var changesAvailable = false
        for (plan in plans) {
            val entries = plan.entries[grade]
            if (entries != null && entries.isNotEmpty()) {
                changesAvailable = true
                break
            }
        }


        val isSilentModeOn = preferences.getBoolean(
                applicationContext.getString(R.string.pref_isSilentModeEnabled), false)
        val respectWeekend = preferences.getBoolean(
                applicationContext.getString(R.string.pref_isSilenceOnWeekendEnabled), false)
                && TimeHelper.isWeekend

        if (changesAvailable && !respectWeekend && !isSilentModeOn) {
            val id = intent.getIntExtra(INTENT_EXTRA_ID, 1000)
            sendNotification(id, gradeName)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showForegroundNotification() {
        val builder = Notification.Builder(applicationContext, NotificationHelper.CHANNEL_SYNC)
        builder.setSmallIcon(R.drawable.ic_cloud_download)
        builder.setContentTitle(getString(R.string.notifications_fetch_loading))
        builder.setColor(getColor(R.color.colorAccent))
        builder.setProgress(0, 0, true)

        startForeground(TimeHelper.generateTimeBasedID(), builder.build())
    }


    private fun sendNotification(id: Int, gradeName: String) {
        val builder = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                Notification.Builder(applicationContext, NotificationHelper.CHANNEL_PLAN)
            else -> @Suppress("DEPRECATION") Notification.Builder(applicationContext)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setColor(getColor(R.color.colorAccent))
        }

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (preferences.getBoolean(getString(R.string.pref_isNotificationSoundEnabled), false)) {
                builder.setSound(NotificationHelper.DEFAULT_RINGTONE)
            }
            if (preferences.getBoolean(getString(R.string.pref_isNotificationVibrationEnabled), true)) {
                builder.setVibrate(NotificationHelper.DEFAULT_VIBRATION_PATTERN)
            }
            if (preferences.getBoolean(getString(R.string.pref_isNotificationLightEnabled), true)) {
                builder.setLights(NotificationHelper.DEFAULT_COLOR, 3000, 3000)
            }
        }

        builder.setSmallIcon(R.drawable.ic_school)
        builder.setContentTitle(String.format(getString(R.string.notifications_fetch_changesAvailable_format), gradeName))
        builder.setContentText(getString(R.string.notifications_fetch_openAppForMore))

        val contentIntent = Intent(applicationContext, SplashActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(applicationContext, id, contentIntent, 0)
        builder.setContentIntent(contentPendingIntent)

        notificationManager.notify(id, builder.build())
    }
}