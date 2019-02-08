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

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import de.haukesomm.vertretungsplan.R

class InfoActivity : AppCompatActivity() {

    private val version by lazy { findViewById<TextView>(R.id.activity_info_version_value) }

    private val buildType by lazy { findViewById<TextView>(R.id.activity_info_buildType_value) }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        initToolbar()
        initVersion()
        initLinks()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> this.finish()
        }

        return super.onOptionsItemSelected(item)
    }


    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = getString(R.string.activity_info)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun initVersion() {
        try {
            val versionString = packageManager.getPackageInfo(packageName, 0).versionName
            val versionNumber = versionString.replace(Regex("-.*$"), "")
            val versionBuildType = versionString.replace(Regex("^.*-"), "").capitalize()

            version.text = versionNumber
            buildType.text = versionBuildType
        } catch (n: PackageManager.NameNotFoundException) {
            Log.w("InfoActivity", "Cannot retrieve app version name: " + n.message)
        }

    }


    private fun registerViewIntent(view: View, uri: String) {
        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(uri)
            startActivity(intent)
        }
    }

    private fun initLinks() {
        registerViewIntent(
                findViewById(R.id.activity_info_playStore),
                "https://play.google.com/store/apps/developer?id=Hauke+Sommerfeld"
        )
        registerViewIntent(
                findViewById(R.id.activity_info_gitHub),
                "https://github.com/haukesomm"
        )
    }
}
