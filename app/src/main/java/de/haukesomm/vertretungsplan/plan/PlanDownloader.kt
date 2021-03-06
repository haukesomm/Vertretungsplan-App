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

package de.haukesomm.vertretungsplan.plan

import android.util.Log
import org.jsoup.Jsoup
import java.io.IOException

class PlanDownloader {

    fun downloadAll(): List<Plan> {
        val parser = PlanParser()
        val list = ArrayList<Plan>()

        for (url in Plan.urls) {
            try {
                val document = Jsoup.connect(url.toString()).get()
                val plan = parser.parse(document)
                list.add(plan)
            } catch (io: IOException) {
                Log.w("PlanDownloader", "Cannot download plan: ${io.message}")
            } catch (p: PlanParserException) {
                Log.w("PlanDownloader", "Error while parsing the downloaded plan: ${p.message}")
            }
        }

        return list
    }
}