package de.haukesomm.vertretungsplan.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import de.haukesomm.vertretungsplan.R
import de.haukesomm.vertretungsplan.helper.ActivityHelper
import de.haukesomm.vertretungsplan.plan.Plan

class EndOfLifeActivity : AppCompatActivity() {

    private val openIservButton: View by lazy { findViewById<View>(R.id.activity_end_of_life_openWebsite) }

    private val openGitHubButton: View by lazy { findViewById<View>(R.id.activity_end_of_life_openGitHub_) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_of_life)
        initExternalLinks()
    }


    private fun initExternalLinks() {
        openIservButton.setOnClickListener {
            ActivityHelper(this).launch(Intent(Intent.ACTION_VIEW, Uri.parse(Plan.homepage)))
        }

        openGitHubButton.setOnClickListener {
            ActivityHelper(this).launch(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/haukesomm/Vertretungsplan-App")))
        }
    }
}
