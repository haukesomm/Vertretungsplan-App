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
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import de.haukesomm.vertretungsplan.R

class CourseFilterPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {

    companion object {

        const val ENCODING_SEPARATOR = ","
    }


    private lateinit var chips: ChipGroup

    private lateinit var courses: MutableList<String>


    init {
        layoutResource = R.layout.preference_filter_course
    }


    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        holder!!

        val preferenceTitle = holder.findViewById(R.id.preference_filter_course_title) as TextView
        preferenceTitle.text = title

        chips = holder.findViewById(R.id.preference_filter_course_chips) as ChipGroup
        courses = getPersistedCourses()

        displayCourses(courses)

        onPreferenceClickListener = mainOnClickListener
    }


    private fun displayCourses(courses: List<String>) {
        chips.removeAllViews()
        if (courses.isEmpty()) {
            chips.visibility = View.GONE
        } else {
            courses.forEach { course -> addCourseChip(course) }
            chips.visibility = View.VISIBLE
        }
    }

    private fun addCourseChip(course: String) {
        val chip = Chip(context)
        chip.text = course
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener(chipOnClickListener)
        chips.addView(chip)
    }


    private fun getPersistedCourses(): MutableList<String> {
        val encoded = getPersistedString("")
        return when {
            encoded.isNotBlank() -> encoded.split(ENCODING_SEPARATOR).toMutableList()
            else -> mutableListOf()
        }
    }

    private fun persistCourses(courses: List<String>) {
        var encoded = ""
        courses.forEach { encoded += "$it$ENCODING_SEPARATOR" }
        encoded = encoded.removeSuffix(ENCODING_SEPARATOR)
        persistString(encoded)
    }


    private val mainOnClickListener = OnPreferenceClickListener {
        val dialogBuilder = AlertDialog.Builder(context)

        dialogBuilder.setTitle(context.getString(R.string.fragment_preferences_senior_courses_add_title))

        val frame = FrameLayout(context)

        val input = EditText(context)
        val inputMargin = context.resources.getDimension(R.dimen.margin_default).toInt()
        val inputParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT)
        inputParams.setMargins(inputMargin, inputMargin, inputMargin, inputMargin)
        input.layoutParams = inputParams
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = summary

        frame.addView(input)
        dialogBuilder.setView(frame)

        dialogBuilder.setPositiveButton(context.getString(R.string.fragment_preferences_senior_courses_add_save))
        { _, _ ->
            val course = input.text.toString()
            if (course.isNotBlank()) {
                addCourseChip(course)

                val courses = getPersistedCourses()
                courses += course
                persistCourses(courses)

                displayCourses(courses)
            }
        }
        dialogBuilder.setNegativeButton(context.getString(R.string.fragment_preferences_senior_courses_add_cancel))
        { dialog, _ -> dialog.dismiss() }

        dialogBuilder.show()
        input.requestFocus()

        true
    }

    private val chipOnClickListener = View.OnClickListener {
        val courses = getPersistedCourses()
        courses.remove((it as Chip).text)
        persistCourses(courses)
        displayCourses(courses)
    }
}