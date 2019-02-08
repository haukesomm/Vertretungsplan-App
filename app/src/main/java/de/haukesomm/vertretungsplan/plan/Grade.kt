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

data class Grade(val name: String, val type: Type) {

    enum class Type {
        UNKNOWN,
        DEFAULT,
        SENIOR
    }


    /*
     * This constructor returns a Grade with a given name and tries to determine it's Type based
     * on the default set of Grades in the companion object. Type.UNKNOWN will be used if no
     * fitting Type could be found.
     * */
    constructor(name: String) : this(name, defaults.find { it.name == name }?.type ?: Type.UNKNOWN)


    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        val defaults = listOf(
                // Mittelstufe:
                Grade("5/1", Grade.Type.DEFAULT),   Grade("5/2", Grade.Type.DEFAULT),
                Grade("5/3", Grade.Type.DEFAULT),   Grade("5/4", Grade.Type.DEFAULT),
                Grade("5/5", Grade.Type.DEFAULT),   Grade("6/1", Grade.Type.DEFAULT),
                Grade("6/2", Grade.Type.DEFAULT),   Grade("6/3", Grade.Type.DEFAULT),
                Grade("6/4", Grade.Type.DEFAULT),   Grade("6/5", Grade.Type.DEFAULT),
                Grade("7/1", Grade.Type.DEFAULT),   Grade("7/2", Grade.Type.DEFAULT),
                Grade("7/3", Grade.Type.DEFAULT),   Grade("7/4", Grade.Type.DEFAULT),
                Grade("7/5", Grade.Type.DEFAULT),   Grade("8/1", Grade.Type.DEFAULT),
                Grade("8/2", Grade.Type.DEFAULT),   Grade("8/3", Grade.Type.DEFAULT),
                Grade("8/4", Grade.Type.DEFAULT),   Grade("8/5", Grade.Type.DEFAULT),
                Grade("9/1", Grade.Type.DEFAULT),   Grade("9/2", Grade.Type.DEFAULT),
                Grade("9/3", Grade.Type.DEFAULT),   Grade("9/4", Grade.Type.DEFAULT),
                Grade("9/5", Grade.Type.DEFAULT),   Grade("10/1", Grade.Type.DEFAULT),
                Grade("10/2", Grade.Type.DEFAULT),  Grade("10/3", Grade.Type.DEFAULT),
                Grade("10/4", Grade.Type.DEFAULT),  Grade("10/5", Grade.Type.DEFAULT),
                Grade("11/1", Grade.Type.DEFAULT),  Grade("11/2", Grade.Type.DEFAULT),
                Grade("11/3", Grade.Type.DEFAULT),  Grade("11/4", Grade.Type.DEFAULT),
                Grade("11/5", Grade.Type.DEFAULT),
                // Oberstufe:
                Grade("12", Grade.Type.SENIOR),     Grade("13", Grade.Type.SENIOR)
        )
    }
}