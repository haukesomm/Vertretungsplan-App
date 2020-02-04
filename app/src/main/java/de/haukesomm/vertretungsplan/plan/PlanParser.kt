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

package de.haukesomm.vertretungsplan.plan

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.lang.StringBuilder


// HTML related constants:

private const val HTML_TITLE_CLASS = "mon_title"


private const val HTML_MESSAGES_TABLE_CLASS = "info"

private const val HTML_MESSAGES_ROW_TAG = "td"


private const val HTML_ENTRIES_TABLE_CLASS = "mon_list"

private const val HTML_ENTRIES_ROW_TAG = "tr"

private const val HTML_ENTRIES_HEADER_TAG = "th"

private const val HTML_ENTRIES_SUBHEADER_CLASS = "inline_header"

private const val HTML_ENTRIES_ATTRIBUTE_CLASS = "list"


// Flags used in the format(String, Int) method:

private enum class Flag {
    SUBSTITUTION,
    SUBJECT,
    POSSIBLY_EMPTY
}


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
        return document.getElementsByClass(HTML_TITLE_CLASS).first().text()
    }

    private fun parseMessage(document: Document): String {
        val info: Element? = document.getElementsByClass(HTML_MESSAGES_TABLE_CLASS).first()
        return when (info) {
            null -> "" // There might not be any announcements
            else -> {
                val messages = info.getElementsByTag(HTML_MESSAGES_ROW_TAG)

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
        val table = document.getElementsByClass(HTML_ENTRIES_TABLE_CLASS).first()
        val rows = table.getElementsByTag(HTML_ENTRIES_ROW_TAG)

        val map = HashMap<Grade, MutableList<PlanEntry>>()

        lateinit var currentGrade: Grade

        rows@ for (row in rows) {
            val attrs = row.getElementsByClass(HTML_ENTRIES_ATTRIBUTE_CLASS)
            val first = attrs[1] // 0 is the root tag!

            /*
             * First case: Tag is table header and should be omitted.
             */
            if (first.tagName() == HTML_ENTRIES_HEADER_TAG) {
                continue@rows
            }
            /*
             * Second case: Tag is an inline header.
             * Create a map entry for the Grade of the inline-header and assign an isEmpty List to it.
             * The List is then used to store the respective PlanEntries.
             */
            else if (first.hasClass(HTML_ENTRIES_SUBHEADER_CLASS)) {
                val grade = Grade(row.text())
                map[grade] = ArrayList()
                currentGrade = grade
            }
            /*
             * Third case: Tag is a regular table row containing an entry.
             * Try to parse the entry and add a PlanEntry-object to the map.
             * If an entry cannot be parsed it will be omitted.
             */
            else {
                try {
                    val entry = parseEntry(currentGrade, attrs)
                    map[currentGrade]?.add(entry)
                } catch (e: Exception) {
                    // Debug:
                    System.err.println("Invalid: '${attrs.toString().replace("\n", "").replace(Regex("</tr>(.*)$"), "")}'")
                }
            }
        }

        return map
    }

    private fun parseEntry(grade: Grade, elements: Elements): PlanEntry {
        val lessons = elements[1]
        val subject = elements[2]
        val room = elements[3]
        val comment = elements[4]


        val formattedLessons = lessons.text()
        val formattedSubject = format(subject.text(), Flag.SUBSTITUTION, Flag.SUBJECT)
        val formattedRoom = format(room.text(), Flag.SUBSTITUTION, Flag.POSSIBLY_EMPTY)
        val formattedComment = format(comment.text(), Flag.POSSIBLY_EMPTY)

        val entry = PlanEntry(formattedLessons, formattedSubject, formattedRoom, formattedComment)


        // An entry contains course-specific information either if it's grade is of type SENIOR or
        // the original subject (without any substitution information) ends with a number:

        // "Subject without substitution": Subject string without it's substitution info at the end
        // (e.g. "WuN2?Ver" => "WuN2")
        val subjectWithoutSubstitution = subject.text().replace(Regex("\\?.+$"), "")
        // If the above generated string ends with a number, it is a course regardless of the
        // grade's type (DEFAULT or SENIOR).
        val subjectIsCourse = subjectWithoutSubstitution.matches(Regex("^.*[0-9]$"))

        if (grade.type == Grade.Type.SENIOR || subjectIsCourse) {
            // Extract course name by removing trailing substitution info ('?...')
            entry.course = subjectWithoutSubstitution
        }


        // An entry should be flagged as a cancellation if the HTML source of the subject is
        // formatted as a strike through and is no substitution.

        if (subject.children().isNotEmpty()
                && subject.children().first().tagName() == "s"
                && !subject.text().contains("?")) {
            entry.cancellation = true
        }


        return entry
    }


    private fun format(string: String, vararg flags: Flag): String {
        var formatted = string

        if (Flag.SUBSTITUTION in flags) {
            // Remove substitution-prefix ('Eng?')
            formatted = formatted.replace(Regex("^.+\\?"), "")
        }

        if (Flag.SUBJECT in flags) {
            formatted = when (formatted) {
                "Deu" -> "Deutsch"
                "Eng" -> "Englisch"
                "Fra" -> "Französisch"
                "Lat" -> "Latein"
                "Mat" -> "Mathematik"
                "Ges" -> "Geschichte"
                "Pol" -> "Politik"
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
                // Other:
                "Vertr" -> "Vertretung"
                else -> formatted
            }
        }

        if (Flag.POSSIBLY_EMPTY in flags) {
            formatted = formatted
                    .replace(Regex("(---)-*"), "")  // Remove strike-through
                    .replace("\u00A0", "")          // Remove NO-BREAK-SPACE
        }

        return formatted
    }
}