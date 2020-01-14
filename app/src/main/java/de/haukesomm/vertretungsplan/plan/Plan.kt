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

package de.haukesomm.vertretungsplan.plan

import java.net.URL

data class Plan(val title: String,
                val message: String,
                val entries: Map<Grade, List<PlanEntry>>) {

    companion object {
        const val homepage = "https://www.gym-nw.de"
        val urls = listOf(
                URL("https://www.gym-nw.org/media/hornischer/schueler.htm"),
                URL("https://www.gym-nw.org/media/hornischer/schueler2.htm"))
    }
}