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

package de.haukesomm.vertretungsplan.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import de.haukesomm.vertretungsplan.R

class NotificationHelper(private val context: Context) {

    // Needed since other apps need to access the channel IDs
    companion object {

        val DEFAULT_RINGTONE = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)!!

        val DEFAULT_VIBRATION_PATTERN = longArrayOf(0L, 300L, 300L, 300L)

        const val DEFAULT_COLOR = Color.WHITE


        // Channel IDs:

        const val CHANNEL_SYNC = "channel_vertretungsplan_sync"

        const val CHANNEL_PLAN = "channel_vertretungsplan_plan"
    }


    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager


    @RequiresApi(Build.VERSION_CODES.O)
    fun initNotificationChannels() {
        if (manager.getNotificationChannel(CHANNEL_SYNC) == null) {
            initSyncChannel()
        }
        if (manager.getNotificationChannel(CHANNEL_PLAN) == null) {
            initPlanChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initSyncChannel() {
        val channel = NotificationChannel(
                CHANNEL_SYNC,
                context.getString(R.string.notifications_channel_sync_title),
                NotificationManager.IMPORTANCE_LOW)

        channel.description = context.getString(R.string.notifications_channel_sync_description)
        channel.setShowBadge(false)

        manager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initPlanChannel() {
        val channel = NotificationChannel(
                CHANNEL_PLAN,
                context.getString(R.string.notifications_channel_plan_title),
                NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }
}