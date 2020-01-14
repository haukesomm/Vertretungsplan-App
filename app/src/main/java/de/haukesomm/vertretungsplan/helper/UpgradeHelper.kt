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

package de.haukesomm.vertretungsplan.helper

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager

import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.background.FetchServiceManager


// Version code of the final 2.0 release
private const val VERSION_CODE_V2 = 53


object UpgradeHelper {

    fun checkVersion(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)


        val last = prefs.getInt(context.getString(R.string.pref_previousAppVersion), -1)

        val previous: Int = try {
            // Cast to Int in order to maintain backward-compatibility
            PackageInfoCompat.getLongVersionCode(
                    context.packageManager.getPackageInfo(context.packageName, 0)).toInt()
        } catch (n: PackageManager.NameNotFoundException) {
            Log.w("UpgradeHelper", "Unable to retrieve app version! Setting to -1.")
            -1
        }


        if (last < previous) {
            val manager = FetchServiceManager(context)
            manager.restoreCancelledReminders()

            prefs.edit().putInt(context.getString(R.string.pref_previousAppVersion), previous).apply()

            onUpgrade(context, prefs, last, previous)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onUpgrade(context: Context, prefs: SharedPreferences, previous: Int, current: Int) {
        /*
         * The code to be executed if the application version increases goes here.
         * This method is called if, and only if the new version number is truly greater then
         * the previous version.
         * */
    }
}
