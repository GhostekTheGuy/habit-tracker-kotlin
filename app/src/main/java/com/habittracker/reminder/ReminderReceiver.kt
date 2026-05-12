package com.habittracker.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Odbiera alarm z AlarmManagera i wystawia powiadomienie.
 * Po wystawieniu przeklada alarm na kolejny dzien (alarm jednorazowy + reschedule).
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Nawyk do wykonania"
        val minutes = intent.getIntExtra(EXTRA_MINUTES, -1)
        android.util.Log.d("ReminderReceiver", "onReceive habitId=$habitId title=$title minutes=$minutes")
        if (habitId == -1L) return

        HabitNotifier.show(context, habitId, title)

        // Przeloz na jutro o tej samej porze.
        if (minutes >= 0) {
            ReminderScheduler.schedule(context, habitId, title, minutes)
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MINUTES = "minutes"
    }
}
