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

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import de.haukesomm.vertretungsplan.plan.PlanEntry
import de.haukesomm.vertretungsplan.R


private const val DESCRIPTION_DELIMITER = ", "


class PlanEntryAdapter(private val context: Context,
                       private var entries: List<PlanEntry>) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)


    override fun getItem(position: Int) = entries[position]

    override fun getCount() = entries.size

    override fun getItemId(position: Int) = 0L


    fun setEntries(ents: List<PlanEntry>) {
        entries = ents
    }


    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = when (convertView) {
            null -> inflater.inflate(R.layout.adapter_entry_plan, parent, false)
            else -> convertView
        }

        val entry = entries[position]


        // Init lessons

        val lessonsText = view.findViewById<TextView>(R.id.adapter_planentry_lessons)
        lessonsText.text = entry.lessons

        // Init description (format subject/room)

        val descriptionText = view.findViewById<TextView>(R.id.adapter_planentry_description)
        val subject = entry.subject
        val room = if (entry.room.isNotBlank()) {
            String.format(context.getString(R.string.adapter_planEntry_roomRormat), entry.room)
        } else ""
        descriptionText.text = "$subject$DESCRIPTION_DELIMITER$room"
                .removePrefix(DESCRIPTION_DELIMITER)
                .removeSuffix(DESCRIPTION_DELIMITER)
        handleVisibility(descriptionText, descriptionText.text.isNotBlank())

        // Init comment

        val commentText = view.findViewById<TextView>(R.id.adapter_planentry_comment)
        commentText.text = entry.comment
        handleVisibility(commentText, commentText.text.isNotBlank())

        // Init course chip

        val courseChip = view.findViewById<TextView>(R.id.adapter_planentry_course)
        courseChip.text = entry.course
        handleVisibility(courseChip, entry.course.isNotBlank())

        // Init cancellation chip

        val cancellationChip = view.findViewById<TextView>(R.id.adapter_planentry_cancellation)
        handleVisibility(cancellationChip, entry.cancellation)


        return view
    }

    private fun handleVisibility(view: View, condition: Boolean) {
        view.visibility = when (condition) {
            true -> View.VISIBLE
            false -> View.GONE
        }
    }
}