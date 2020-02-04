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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.plan.PlanCache

class PlanMessagesFragment : Fragment(), PlanCache.Observer {

    private lateinit var messageAdapter: PlanMessageAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_messages_plan, container, false)
        initMessageList(view)

        PlanCache.subscribe(this)

        return view
    }

    override fun onDestroy() {
        PlanCache.unsubscribe(this)
        super.onDestroy()
    }


    private fun getPlansContainingMessages() = PlanCache.getAll().filter { it.message.isNotEmpty() }


    private fun initMessageList(view: View) {
        val messageList = view.findViewById<ListView>(R.id.fragment_messages_plan_output)

        val messageListEmptyView = view.findViewById<View>(R.id.fragment_messages_plan_output_empty)
        messageList.emptyView = messageListEmptyView

        messageAdapter = PlanMessageAdapter(context!!, getPlansContainingMessages())
        messageList.adapter = messageAdapter
    }


    override fun onPlanCacheUpdate() {
        messageAdapter.setItems(getPlansContainingMessages())
        messageAdapter.notifyDataSetChanged()
    }
}