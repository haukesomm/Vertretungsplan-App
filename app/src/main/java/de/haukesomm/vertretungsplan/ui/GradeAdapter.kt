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
import de.haukesomm.vertretungsplan.plan.Grade
import de.haukesomm.vertretungsplan.R

class GradeAdapter(private val context: Context,
                   private val grades: List<Grade>) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)


    override fun getCount() = grades.size

    override fun getItem(position: Int) = grades[position]

    override fun getItemId(position: Int) = 0L


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = when (convertView) {
            null -> inflater.inflate(R.layout.adapter_grade, parent, false)
            else -> convertView
        }

        val grade = grades[position]


        val name = view.findViewById<TextView>(R.id.adapter_grade_name)
        name.text = grade.name

        val type = view.findViewById<TextView>(R.id.adapter_grade_type)
        type.text = context.getString(when (grade.type) {
            Grade.Type.UNKNOWN -> R.string.unknown
            Grade.Type.DEFAULT -> R.string.adapter_grade_default
            Grade.Type.SENIOR -> R.string.adapter_grade_senior
        })


        return view
    }
}