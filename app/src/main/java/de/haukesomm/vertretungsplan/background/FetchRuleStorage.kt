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

package de.haukesomm.vertretungsplan.background

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import de.haukesomm.vertretungsplan.plan.Grade


private const val FILENAME = "rules.db"

private const val VERSION = 1


private const val TABLE_RULES = "rules"

private const val TABLE_RULES_ID = "id"

private const val TABLE_RULES_HOUR = "hour"

private const val TABLE_RULES_MINUTE = "minute"

private const val TABLE_RULES_GRADE = "grade"


internal class FetchRuleStorage(context: Context)
    : SQLiteOpenHelper(context, FILENAME, null, VERSION), AutoCloseable {

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Do nothing
    }

    override fun onCreate(db: SQLiteDatabase?) {
        initDatabase(db!!)
    }


    private fun initDatabase(db: SQLiteDatabase) = db.execSQL("CREATE TABLE $TABLE_RULES ("
            + "$TABLE_RULES_ID INTEGER NOT NULL PRIMARY KEY,"
            + "$TABLE_RULES_HOUR INTEGER NOT NULL,"
            + "$TABLE_RULES_MINUTE INTEGER NOT NULL,"
            + "$TABLE_RULES_GRADE TEXT NOT NULL"
            + ");")


    fun addRule(rule: FetchRule) {
        val values = ContentValues()
        values.put(TABLE_RULES_ID, rule.id)
        values.put(TABLE_RULES_HOUR, rule.hour)
        values.put(TABLE_RULES_MINUTE, rule.minute)
        values.put(TABLE_RULES_GRADE, rule.grade.name)
        writableDatabase.insert(TABLE_RULES, null, values)
    }


    fun removeRule(id: Int) = writableDatabase.delete(TABLE_RULES, "$TABLE_RULES_ID = $id", null)


    fun get(id: Int): FetchRule {
        val cursor = writableDatabase.query(TABLE_RULES, null, "$TABLE_RULES_ID = $id", null, null, null, null, null)
        var rule: FetchRule? = null
        cursor.moveToFirst()
        cursor.use {
            rule = ruleFromCursor(it)
        }
        return rule!!
    }

    fun listRules(): List<FetchRule> {
        val rules = ArrayList<FetchRule>()

        val cursor = writableDatabase.query(TABLE_RULES, null, null, null, null, null, null)
        cursor.use {
            it.moveToFirst()
            while (!it.isAfterLast) {
                val rule = ruleFromCursor(it)
                rules += rule
                it.moveToNext()
            }
        }

        return rules
    }

    private fun ruleFromCursor(cursor: Cursor): FetchRule {
        return FetchRule(
                cursor.getInt(0), // ID
                cursor.getInt(1), // Hour
                cursor.getInt(2), // Minute
                Grade(cursor.getString(3))) // Grade
    }
}