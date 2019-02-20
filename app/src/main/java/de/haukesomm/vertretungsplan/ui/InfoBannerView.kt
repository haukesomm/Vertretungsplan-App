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
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import de.haukesomm.vertretungsplan.R

class InfoBannerView : FrameLayout {

    private val titleView by lazy { findViewById<TextView>(R.id.view_banner_info_title) }

    private val explanationView by lazy { findViewById<TextView>(R.id.view_banner_info_explanation) }

    private val iconView by lazy { findViewById<ImageView>(R.id.view_banner_info_icon) }


    var title
        get() = titleView.text as String
        set(value) { titleView.text = value }

    var explanation
        get() = explanationView.text as String
        set(value) { explanationView.text = value }

    var drawable
        get() = iconView.drawable
        set(value) = iconView.setImageDrawable(value)


    constructor(context: Context) : super(context) {
        inflate(context, R.layout.view_banner_info, this)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        inflate(context, R.layout.view_banner_info, this)

        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.InfoBannerView, 0, 0)
        val title = ta.getString(R.styleable.InfoBannerView_title)
        val explanation = ta.getString(R.styleable.InfoBannerView_explanation)
        val drawable = ta.getDrawable(R.styleable.InfoBannerView_drawable)
        ta.recycle()

        if (title != null) this.title = title
        if (explanation != null) this.explanation = explanation
        if (drawable != null) this.drawable = drawable
    }
}