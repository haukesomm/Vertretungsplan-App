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

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import de.haukesomm.vertretungsplan.plan.Grade
import de.haukesomm.vertretungsplan.plan.PlanEntry
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.plan.PlanCache
import java.util.*

class PlanEntriesFragment : Fragment(), PlanCache.Observer {

    private lateinit var preferences: SharedPreferences


    private var plans = PlanCache.getAll()

    private val grades = Grade.defaults


    private lateinit var spinnerPlans: Spinner

    private lateinit var spinnerGrades: Spinner

    private lateinit var spinnerPlansAdapter: PlanAdapter

    private lateinit var spinnerGradesAdapter: GradeAdapter

    private var rememberGrade = false


    private lateinit var courseFilterBanner: View

    private var courseFilterEnabled = false

    private var allowedCourses = listOf<String>()


    private lateinit var output: ListView

    // Can be used after initSpinners() has been called
    private lateinit var outputAdapter: PlanEntryAdapter


    // Can be used after initSpinners() has been called
    private val selectedPlan
        get() = plans[spinnerPlans.selectedItemPosition]

    // Can be used after initSpinners() has been called
    private val selectedGrade
        get() = grades[spinnerGrades.selectedItemPosition]

    // Can be used after initSpinners() has been called
    private val currentEntries: List<PlanEntry>
        get() = (selectedPlan.entries[selectedGrade]?: ArrayList())
                .filter { !courseFilterEnabled || allowedCourses.contains(it.course) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view =  inflater.inflate(R.layout.fragment_entries_plan, container, false)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        initSpinners(view)
        initCourseFilter(view)
        initOutput(view)

        PlanCache.subscribe(this)

        return view
    }

    override fun onDestroy() {
        PlanCache.unsubscribe(this)
        super.onDestroy()
    }


    override fun onPlanCacheUpdate() = updatePlans()


    /* #############################################################################################
     * #                                                                                           #
     * #    Plan/Grade selection related code                                                      #
     * #                                                                                           #
     * #############################################################################################
     * */

    private fun initSpinners(view: View) {
        spinnerPlans = view.findViewById(R.id.fragment_entries_plan_spinnerPlans)
        spinnerGrades = view.findViewById(R.id.fragment_entries_plan_spinnerGrades)

        spinnerPlansAdapter = PlanAdapter(context!!, plans)
        spinnerPlans.adapter = spinnerPlansAdapter
        spinnerGradesAdapter = GradeAdapter(context!!, grades)
        spinnerGrades.adapter = spinnerGradesAdapter

        spinnerPlans.onItemSelectedListener = spinnerListener
        spinnerGrades.onItemSelectedListener = spinnerListener


        rememberGrade = preferences.getBoolean(getString(R.string.pref_isGradePersistenceEnabled),
                true)
        restorePersistedGradeIfPossible()
    }

    private fun persistSelectedGradeIfPossible() {
        val grade = selectedGrade
        if (rememberGrade && grade.type != Grade.Type.UNKNOWN) {
            preferences.edit()
                    .putString(getString(R.string.pref_persistedGradeName), grade.name)
                    .apply()
        }
    }

    private fun restorePersistedGradeIfPossible() {
        if (rememberGrade) {
            val gradeString = preferences.getString(
                    getString(R.string.pref_persistedGradeName), Grade.defaults[0].name)

            val grade = Grade(gradeString!!)
            val index = grades.indexOf(grade)
            // If the Grade no longer exists (index -1), the Spinner needs to be set to a valid
            // position. This is 0 until a new, valid Grade is persisted.
            spinnerGrades.setSelection(if (index != -1) index else 0)
        }
    }

    private fun updatePlans() {
        plans = PlanCache.getAll()
        spinnerPlansAdapter.setItems(plans)
        spinnerPlansAdapter.notifyDataSetChanged()
    }

    private val spinnerListener = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            persistSelectedGradeIfPossible()
            updateOutput()
        }


        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }


    /* #############################################################################################
     * #                                                                                           #
     * #    Course filter related code                                                             #
     * #                                                                                           #
     * #############################################################################################
     */

    private fun initCourseFilter(view: View) {
        courseFilterBanner = view.findViewById(R.id.fragment_entries_plan_courseFilterBanner)

        val turnOffButton = courseFilterBanner
                .findViewById<View>(R.id.fragment_entries_plan_courseFilterBanner_turnOff)
        turnOffButton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(context!!)
            dialogBuilder.setTitle(R.string.fragment_entries_plan_courseFilter_turnOff_title)
            dialogBuilder.setPositiveButton(
                    R.string.fragment_entries_plan_courseFilter_turnOff_posButton)
            { dialog: DialogInterface, _ ->
                disableCourseFilter()
                dialog.dismiss()
            }
            dialogBuilder.setNegativeButton(
                    R.string.fragment_entries_plan_courseFilter_turnOff_negButton)
            { dialog: DialogInterface, _ ->
                dialog.dismiss()
            }
            dialogBuilder.show()
        }

        obtainCourseFilterSettings()
    }

    private fun obtainCourseFilterSettings() {
        courseFilterEnabled = preferences.getBoolean(
                getString(R.string.pref_isCourseFilterEnabled), false)

        if (courseFilterEnabled) {
            courseFilterBanner.visibility = View.VISIBLE

            val allowedCourses = preferences.getString(
                    getString(R.string.pref_courseFilterCourses), "")!!
            this.allowedCourses =
                    allowedCourses.split(CourseFilterPreference.ENCODING_SEPARATOR).toList()
        } else {
            courseFilterBanner.visibility = View.GONE
        }
    }

    fun triggerCourseFilterUpdate() {
        obtainCourseFilterSettings()
        updateOutput()
    }

    private fun disableCourseFilter() {
        preferences.edit()
                .putBoolean(getString(R.string.pref_isCourseFilterEnabled), false)
                .apply()
        triggerCourseFilterUpdate()
    }


    /* #############################################################################################
     * #                                                                                           #
     * #    PlanEntry output related related code                                                  #
     * #                                                                                           #
     * #############################################################################################
     */

    private fun initOutput(view: View) {
        output = view.findViewById(R.id.fragment_entries_plan_output)

        val emptyView: View = view.findViewById(R.id.fragment_entries_plan_output_empty)
        output.emptyView = emptyView

        outputAdapter = PlanEntryAdapter(context!!, currentEntries)
        output.adapter = outputAdapter
    }

    private fun updateOutput() {
        outputAdapter.setEntries(currentEntries)
        outputAdapter.notifyDataSetChanged()
    }
}