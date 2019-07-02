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

import java.lang.IllegalStateException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class ModifyOnceProperty<T>(private val default: T) : ReadWriteProperty<Any, T> {

    private object EMPTY

    private var value: Any = EMPTY


    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>): T = when(value) {
        EMPTY -> default
        else -> value as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (value != EMPTY) {
            throw IllegalStateException("Value has already been modified")
        } else {
            this.value = value
        }
    }
}

fun <T> modifyOnce(defaultValue: T) = ModifyOnceProperty(defaultValue)