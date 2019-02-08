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

package de.haukesomm.vertretungsplan.plan

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.lang.StringBuilder

class PlanParser {

    @Throws(PlanParserException::class)
    fun parse(plan: Document): Plan {
        lateinit var title: String
        lateinit var message: String
        lateinit var entries: Map<Grade, List<PlanEntry>>

        try {
            title = parseTitle(plan)
        } catch (e: Exception) {
            throw PlanParserException("Error parsing plan title: ${e.message}")
        }
        try {
            message = parseMessage(plan)
        } catch (e: Exception) {
            throw PlanParserException("Error parsing plan message: ${e.message}")
        }
        try {
            entries = parseEntries(plan)
        } catch (e: Exception) {
            throw PlanParserException("Error parsing plan entries: ${e.message}")
        }

        return Plan(title, message, entries)
    }

    private fun parseTitle(document: Document): String {
        return document.getElementsByClass("mon_title").first().text()
    }

    private fun parseMessage(document: Document): String {
        val info: Element? = document.getElementsByClass("info").first()
        return when (info) {
            null -> "" // There might not be any announcements
            else -> {
                val messages = info.getElementsByTag("td")

                val builder = StringBuilder()
                for (m in messages) {
                    builder.append(m.text()).append("\n")
                }
                builder.setLength(builder.length - 1)

                builder.toString()
            }
        }
    }

    private fun parseEntries(document: Document): Map<Grade, List<PlanEntry>> {
        val table = document.getElementsByClass("mon_list").first()
        val rows = table.getElementsByTag("tr")

        val map = HashMap<Grade, MutableList<PlanEntry>>()
        lateinit var grade: Grade

        rows@ for (row in rows) {
            val elements = row.getElementsByClass("list") // Only get tds
            val firstTag = elements[1] // 0 is the root tag!

            when {
                // First tag is table header
                firstTag.tagName() == "th" -> {
                    continue@rows
                }
                // First tag is inline header
                firstTag.hasClass("inline_header") -> {
                    val g = Grade(row.text())
                    map[g] = ArrayList()
                    grade = g
                }
                // Regular table row
                else -> {
                    // If an entry cannot be parsed it will be omitted
                    try {
                        val entry = parseEntry(grade, elements)
                        map[grade]!!.add(entry) // 'last' should always hold a grade
                    } catch (e: Exception) {
                        System.err.println(
                                "Invalid: '${elements.toString().replace("\n", "").replace(Regex("</tr>(.*)$"), "")}'")
                    }
                }
            }
        }

        return map
    }

    private fun parseEntry(grade: Grade, elements: Elements): PlanEntry {
        val lessons =   elements[1]
        val subject =   elements[2]
        val room =      elements[3]
        val comment =   elements[4]

        val entry = PlanEntry(
                lessons.text(),
                subject.text().formatAsSubstitution().formatAsSubject(),
                room.text().formatAsSubstitution().formatAsPossiblyEmpty(),
                comment.text().formatAsPossiblyEmpty())

        if (grade.type == Grade.Type.SENIOR) {
            entry.course = subject.text().extractCourseName()
        }

        if (subject.children().isNotEmpty()
                && subject.children().first().tagName() == "s"
                && !subject.text().contains("?")) {
            entry.cancellation = true
        }

        return entry
    }


    // String extension functions to format plan data:

    private fun String.formatAsPossiblyEmpty() = this
            .replace(Regex("(---)-*"), "")  // Remove strike-through
            .replace("\u00A0", "")          // Remove NO-BREAK-SPACE

    private fun String.formatAsSubstitution() = this
            .replace(Regex("^.+\\?"), "") // Remove substitution-prefix ('Eng?')

    private fun String.formatAsSubject() = when (this) {
        "Deu" -> "Deutsch"
        "Eng" -> "Englisch"
        "Fra" -> "Französisch"
        "Lat" -> "Latein"
        "Mat" -> "Mathematik"
        "Ges" -> "Geschichte"
        "Pol" -> "Politik/Wirtschaft"
        "Erd" -> "Erdkunde"
        "Bio" -> "Biologie"
        "Che" -> "Chemie"
        "Phy" -> "Physik"
        "Kun" -> "Kunst"
        "Mus" -> "Musik"
        "Spo" -> "Sport"
        "Swi" -> "Schwimmen"
        "Ver" -> "Klassenlehrerstunde"
        "Aufg" -> "Vertretungsaufgaben"
        // Randomly made up subjects:
        "Vertr" -> "Vertretung"
        else -> this
    }

    private fun String.extractCourseName() = this
            .replace(Regex("\\?.+$"), "")
}