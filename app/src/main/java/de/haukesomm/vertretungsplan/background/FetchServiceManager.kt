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

package de.haukesomm.vertretungsplan.background

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.haukesomm.vertretungsplan.helper.TimeHelper

class FetchServiceManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val ruleStorage = FetchRuleStorage(context)

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    fun listScheduled() = ruleStorage.use { it.listRules() }

    fun schedule(rule: FetchRule) {
        val pendingServiceIntent = createServiceReceiverPendingIntent(rule.id, rule.grade.name)

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                TimeHelper.futureTimeToMillis(rule.hour, rule.minute),
                AlarmManager.INTERVAL_DAY,
                pendingServiceIntent)

        ruleStorage.use { it.addRule(rule) }
    }

    fun cancel(id: Int) {
        ruleStorage.use {
            val gradeName = it.get(id).grade.name
            val pendingServiceIntent = createServiceReceiverPendingIntent(id, gradeName)
            alarmManager.cancel(pendingServiceIntent)
            ruleStorage.removeRule(id)
        }
    }

    fun restoreCancelledReminders() =
            ruleStorage.use { it.listRules().forEach { rule -> cancel(rule.id); schedule(rule) } }


    fun dismissAllNotifications() = listScheduled().forEach { notificationManager.cancel(it.id) }


    private fun createServiceReceiverPendingIntent(id: Int, gradeName: String): PendingIntent {
        val intent = Intent(context, FetchServiceReceiver::class.java)
        intent.action = FetchServiceReceiver.ACTION_START
        // Important: All INTENT_EXTRA_* attributes are specified in the FetchService class!
        intent.putExtra(FetchService.INTENT_EXTRA_ID, id)
        intent.putExtra(FetchService.INTENT_EXTRA_GRADE_NAME, gradeName)

        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}