package com.habittracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["habit_id"])]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    val logId: Long = 0,

    @ColumnInfo(name = "habit_id")
    val habitId: Long,

    // yyyy-MM-dd
    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = true,
)
