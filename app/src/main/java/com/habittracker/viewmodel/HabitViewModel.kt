package com.habittracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.data.Habit
import com.habittracker.data.HabitDatabase
import com.habittracker.data.HabitRepository
import com.habittracker.reminder.ReminderScheduler
import com.habittracker.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HabitUi(
    val habit: Habit,
    val isCompletedToday: Boolean,
)

data class HabitListState(
    val habits: List<HabitUi> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application

    private val repository = HabitRepository(
        HabitDatabase.getInstance(application).habitDao()
    )

    // Reaktywne "dzis" - odswieza sie co minute, wiec status wykonania
    // poprawnie resetuje sie po polnocy nawet gdy apka dziala w tle.
    private val todayFlow = flow {
        while (true) {
            emit(DateUtils.today())
            delay(60_000L)
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val listState: StateFlow<HabitListState> =
        combine(
            repository.allHabits,
            todayFlow.flatMapLatest { repository.completedHabitIdsForDate(it) }
        ) { habits, completedIds ->
            val completed = completedIds.toSet()
            val items = habits.map { HabitUi(it, it.id in completed) }
            HabitListState(
                habits = items,
                completedCount = items.count { it.isCompletedToday },
                totalCount = items.size,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitListState()
        )

    private val _selectedHabit = MutableStateFlow<Habit?>(null)
    val selectedHabit: StateFlow<Habit?> = _selectedHabit.asStateFlow()

    fun loadHabit(id: Long) {
        viewModelScope.launch {
            _selectedHabit.value = repository.getHabitById(id)
        }
    }

    fun clearSelectedHabit() {
        _selectedHabit.value = null
    }

    fun addHabit(habit: Habit) = viewModelScope.launch {
        val id = repository.insert(habit)
        syncReminder(habit.copy(id = id))
    }

    fun updateHabit(habit: Habit) = viewModelScope.launch {
        repository.update(habit)
        syncReminder(habit)
    }

    fun deleteHabit(habit: Habit) = viewModelScope.launch {
        ReminderScheduler.cancel(app, habit.id)
        repository.delete(habit)
    }

    /** Planuje lub anuluje alarm w zaleznosci od ustawien nawyku. */
    private fun syncReminder(habit: Habit) {
        if (habit.reminderEnabled) {
            ReminderScheduler.schedule(app, habit.id, habit.title, habit.reminderTime)
        } else {
            ReminderScheduler.cancel(app, habit.id)
        }
    }

    fun toggleToday(habitId: Long) = viewModelScope.launch {
        repository.toggleCompletion(habitId, DateUtils.today())
    }

    /** Oznacza wykonanie na dzis wprost (swipe w prawo = wykonany, w lewo = niewykonany). */
    fun setCompletedToday(habitId: Long, completed: Boolean) = viewModelScope.launch {
        repository.setCompletion(habitId, DateUtils.today(), completed)
    }

    fun logsForHabit(habitId: Long) = repository.logsForHabit(habitId)

    fun completedCount(habitId: Long) = repository.completedCount(habitId)
}
