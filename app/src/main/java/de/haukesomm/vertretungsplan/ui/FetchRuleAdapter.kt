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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.background.FetchRule
import de.haukesomm.vertretungsplan.plan.Grade

class FetchRuleAdapter(private val context: Context,
                       private val items: MutableList<FetchRule>,
                       private val client: Client) : BaseAdapter() {

    interface Client {
        fun onDeleteRule(rule: FetchRule)
    }


    private val inflater = LayoutInflater.from(context)


    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getCount() = items.size


    fun add(rule: FetchRule) = items.add(rule)

    fun remove(rule: FetchRule) = items.remove(rule)

    fun removeAll() = items.clear()


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = when (convertView) {
            null -> inflater.inflate(R.layout.adapter_fetchrule, parent, false)
            else -> convertView
        }


        val rule = getItem(position)


        val grade = rule.grade
        val gradeText = view.findViewById<TextView>(R.id.adapter_fetchrule_grade)
        gradeText.text = String.format(context.getString(when (grade.type) {
            Grade.Type.SENIOR -> R.string.adapter_fetchrule_gradeFormat_senior
            else -> R.string.adapter_fetchrule_gradeFormat_default
        }), grade.name)

        val time = view.findViewById<TextView>(R.id.adapter_fetchrule_time)
        time.text = String.format(
                context.getString(R.string.adapter_fetchrule_timeFormat), rule.hour, rule.minute)


        view.setOnClickListener { client.onDeleteRule(rule) }


        return view
    }
}