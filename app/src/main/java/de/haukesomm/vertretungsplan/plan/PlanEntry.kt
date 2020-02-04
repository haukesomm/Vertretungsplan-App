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

import de.haukesomm.vertretungsplan.helper.modifyOnce

data class PlanEntry(val lessons: String,
                     val subject: String,
                     val room: String,
                     val comment: String) {

    // Can be set if an entry contains a cancelled lesson
    var cancellation by modifyOnce(false)

    // Can be set to provide a course name (senior specific)
    var course by modifyOnce("")
}