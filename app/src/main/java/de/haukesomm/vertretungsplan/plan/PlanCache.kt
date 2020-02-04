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

object PlanCache {

    private val plans = mutableMapOf<Int, Plan>()

    private var availableID = 0


    val isEmpty
        get() = plans.isEmpty()


    fun add(plan: Plan, notify: Boolean = true) {
        plans[availableID++] = plan
        if (notify) notifyObservers()
    }

    fun addAll(plans: List<Plan>, notify: Boolean = true) {
        plans.forEach { add(it, false) }
        if (notify) notifyObservers()
    }

    fun getAll(): List<Plan> {
        val list = mutableListOf<Plan>()
        plans.keys.forEach { key -> list += plans[key]!! }
        return list
    }

    fun reset(notify: Boolean = true) {
        plans.clear()
        if (notify) notifyObservers()
    }

    fun reset(plans: List<Plan>) {
        reset(false)
        addAll(plans, false)
        notifyObservers()
    }


    private val observers = mutableListOf<Observer>()

    fun notifyObservers() = observers.forEach { it.onPlanCacheUpdate() }

    fun subscribe(observer: Observer) = observers.add(observer)

    // Leaks may occur if not called!
    fun unsubscribe(observer: Observer) = observers.remove(observer)

    interface Observer {

        fun onPlanCacheUpdate()
    }
}