package com.habittracker.util

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.habittracker.R

object HabitIcons {

    /** Dostepne ikony nawykow (nazwa zapisywana w bazie -> drawable Phosphor). */
    val available: List<Pair<String, Int>> = listOf(
        "star" to R.drawable.ic_hab_star,
        "water" to R.drawable.ic_hab_water,
        "run" to R.drawable.ic_hab_run,
        "fitness" to R.drawable.ic_hab_fitness,
        "book" to R.drawable.ic_hab_book,
        "meditation" to R.drawable.ic_hab_meditation,
        "food" to R.drawable.ic_hab_food,
        "music" to R.drawable.ic_hab_music,
        "sleep" to R.drawable.ic_hab_sleep,
    )

    private val iconMap: Map<String, Int> = available.toMap()

    @DrawableRes
    fun forName(name: String?): Int = iconMap[name] ?: R.drawable.ic_hab_star

    val palette: List<String> = listOf(
        "#8B8BCD", "#22C55E", "#3B82F6", "#EF4444",
        "#F59E0B", "#EC4899", "#14B8A6", "#8B5CF6",
    )

    fun parseColor(hex: String?): Color = try {
        val value = (hex ?: "#8B8BCD").removePrefix("#").toLong(16)
        Color(0xFF000000 or value)
    } catch (e: NumberFormatException) {
        Color(0xFF8B8BCD)
    }
}
