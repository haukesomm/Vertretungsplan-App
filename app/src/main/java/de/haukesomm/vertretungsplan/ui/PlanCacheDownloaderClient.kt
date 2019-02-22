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
import android.content.DialogInterface
import android.text.Html
import androidx.appcompat.app.AlertDialog
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.plan.Plan
import de.haukesomm.vertretungsplan.plan.PlanCache
import de.haukesomm.vertretungsplan.plan.PlanDownloaderTask

abstract class PlanCacheDownloaderClient(private val context: Context) : PlanDownloaderTask.Client {

    override fun onDownloadFinished(result: List<Plan>) {
        PlanCache.reset()
        result.forEach { PlanCache.add(it) }
    }

    override fun onDownloadFailed() {
        val alertDialog = AlertDialog.Builder(context).create()

        alertDialog.setTitle(context.getString(R.string.dialog_downloader_plan_error_title))
        @Suppress("DEPRECATION")
        alertDialog.setMessage(
                Html.fromHtml(context.getString(R.string.dialog_downloader_plan_error_text)))

        alertDialog.setCancelable(false)

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.dialog_downloader_plan_error_reload))
                { dialog, _ ->
                    onReload()
                    dialog.dismiss()
                }

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getString(R.string.dialog_downloader_plan_error_cancel))
                { dialog, _ ->
                    onCancel()
                    dialog.dismiss()
                }

        alertDialog.show()
    }

    abstract fun onReload()

    abstract fun onCancel()
}