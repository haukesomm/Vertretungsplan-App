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

package de.haukesomm.vertretungsplan.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import de.haukesomm.vertretungsplan.plan.Plan
import de.haukesomm.vertretungsplan.R

class PlanMessageAdapter(private val context: Context,
                         private var plans: List<Plan>) : BaseAdapter() {

    override fun getCount() = plans.size

    override fun getItem(position: Int) = plans[position]

    override fun getItemId(position: Int) = 0L


    fun setItems(items: List<Plan>) { plans = items }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = when (convertView) {
            null -> LayoutInflater.from(context).inflate(R.layout.adapter_message_plan, parent, false)
            else -> convertView
        }

        val title = view.findViewById<TextView>(R.id.adapter_planmessage_title)
        title.text = getItem(position).title

        val message = view.findViewById<TextView>(R.id.adapter_planmessage_message)
        message.text = getItem(position).message

        return view
    }
}