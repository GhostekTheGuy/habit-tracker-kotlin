package com.habittracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "icon_name")
    val iconName: String = "star",

    @ColumnInfo(name = "color")
    val color: String = "#8B8BCD",

    @ColumnInfo(name = "category")
    val category: String = "other",

    @ColumnInfo(name = "frequency_type")
    val frequencyType: String = "daily",

    // bitmask dni (pon=1, wt=2, ... ndz=64) lub liczba razy w tygodniu
    @ColumnInfo(name = "frequency_value")
    val frequencyValue: Int = 7,

    @ColumnInfo(name = "target_count")
    val targetCount: Int = 1,

    @ColumnInfo(name = "target_unit")
    val targetUnit: String = "razy",

    // czy przypomnienie wlaczone
    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = false,

    // godzina przypomnienia jako minuty od polnocy (np. 480 = 8:00)
    @ColumnInfo(name = "reminder_time")
    val reminderTime: Int = 480,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
) {
    fun isDueOnDay(dayOfWeek: Int): Boolean = when (frequencyType) {
        "daily" -> true
        "specific_days" -> (frequencyValue and (1 shl (dayOfWeek - 1))) != 0
        else -> true
    }

    val categoryDisplayName: String
        get() = when (category) {
            "health" -> "Zdrowie"
            "productivity" -> "Produktywnosc"
            "sport" -> "Sport"
            "mindfulness" -> "Mindfulness"
            "learning" -> "Nauka"
            else -> "Inne"
        }
}
