package com.habittracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("SELECT * FROM habits WHERE is_archived = 0 ORDER BY sort_order ASC, title ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabitById(habitId: Long): Habit?

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int

    // nawyki z wlaczonym przypomnieniem (do reschedule po boot)
    @Query("SELECT * FROM habits WHERE reminder_enabled = 1 AND is_archived = 0")
    suspend fun getHabitsWithReminders(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog)

    @Delete
    suspend fun deleteLog(log: HabitLog)

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND date = :date LIMIT 1")
    suspend fun getLogForDate(habitId: Long, date: String): HabitLog?

    @Query("SELECT habit_id FROM habit_logs WHERE date = :date AND is_completed = 1")
    fun getCompletedHabitIdsForDate(date: String): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habit_id = :habitId AND is_completed = 1")
    fun getCompletedCount(habitId: Long): Flow<Int>

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND date >= :sinceDate ORDER BY date ASC")
    fun getLogsSince(habitId: Long, sinceDate: String): Flow<List<HabitLog>>
}
