package com.habittracker.util

import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {
    private val storageFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("pl"))

    fun today(): String = storageFormat.format(java.util.Date())

    fun format(millis: Long): String = storageFormat.format(java.util.Date(millis))

    /** Zamienia date "yyyy-MM-dd" na czytelna postac, np. "6 wrzesnia 2026, sobota". */
    fun toDisplay(date: String): String = try {
        storageFormat.parse(date)?.let { displayFormat.format(it) } ?: date
    } catch (e: Exception) {
        date
    }
}
