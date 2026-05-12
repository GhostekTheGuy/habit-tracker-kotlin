package com.habittracker.data

import kotlinx.coroutines.flow.Flow

class HabitRepository(private val dao: HabitDao) {

    val allHabits: Flow<List<Habit>> = dao.getAllHabits()

    fun completedHabitIdsForDate(date: String): Flow<List<Long>> =
        dao.getCompletedHabitIdsForDate(date)

    fun logsForHabit(habitId: Long): Flow<List<HabitLog>> = dao.getLogsForHabit(habitId)

    fun completedCount(habitId: Long): Flow<Int> = dao.getCompletedCount(habitId)

    fun logsSince(habitId: Long, sinceDate: String): Flow<List<HabitLog>> =
        dao.getLogsSince(habitId, sinceDate)

    suspend fun getHabitById(id: Long): Habit? = dao.getHabitById(id)

    suspend fun insert(habit: Habit): Long = dao.insert(habit)

    suspend fun update(habit: Habit) = dao.update(habit)

    suspend fun delete(habit: Habit) = dao.delete(habit)

    suspend fun getHabitCount(): Int = dao.getHabitCount()

    suspend fun toggleCompletion(habitId: Long, date: String) {
        val existing = dao.getLogForDate(habitId, date)
        if (existing != null) {
            dao.deleteLog(existing)
        } else {
            dao.insertLog(HabitLog(habitId = habitId, date = date, isCompleted = true))
        }
    }

    /** Ustawia konkretny stan wykonania na dany dzien (bez przelaczania). */
    suspend fun setCompletion(habitId: Long, date: String, completed: Boolean) {
        val existing = dao.getLogForDate(habitId, date)
        if (completed) {
            if (existing == null) {
                dao.insertLog(HabitLog(habitId = habitId, date = date, isCompleted = true))
            }
        } else {
            if (existing != null) {
                dao.deleteLog(existing)
            }
        }
    }
}
