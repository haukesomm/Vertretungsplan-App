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
import android.content.Intent
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class ActivityHelper(private val mContext: Context) {

    fun launch(clazz: Class<*>) {
        launch(clazz, null)
    }

    fun launch(clazz: Class<*>, requestCode: Int?) {
        launch(Intent(mContext, clazz), requestCode)
    }


    fun launch(i: Intent) {
        launch(i, null)
    }

    fun launch(i: Intent, requestCode: Int?) {
        val h = Handler()
        h.postDelayed({
            when (requestCode) {
                null -> mContext.startActivity(i)
                else -> (mContext as AppCompatActivity).startActivityForResult(i, requestCode)
            }
        }, 300L)
    }
}
