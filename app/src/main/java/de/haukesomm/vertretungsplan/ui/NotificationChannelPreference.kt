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

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import de.haukesomm.vertretungsplan.R

@RequiresApi(Build.VERSION_CODES.O)
class NotificationChannelPreference(context: Context, attrs: AttributeSet)
    : Preference(context, attrs) {

    private val mChannelId: String

    init {
        val attrSet = intArrayOf(R.attr.channelId)
        val ta = context.obtainStyledAttributes(attrs, attrSet)
        mChannelId = ta.getString(0)!!
        ta.recycle()
    }


    override fun onClick() {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, mChannelId)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

        context.startActivity(intent)
    }
}