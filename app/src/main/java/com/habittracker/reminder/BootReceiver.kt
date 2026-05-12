package com.habittracker.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habittracker.data.HabitDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Po restarcie urzadzenia alarmy AlarmManagera znikaja.
 * Ten receiver odtwarza je dla wszystkich nawykow z wlaczonym przypomnieniem.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = HabitDatabase.getInstance(appContext).habitDao()
                dao.getHabitsWithReminders().forEach { habit ->
                    ReminderScheduler.schedule(
                        appContext, habit.id, habit.title, habit.reminderTime
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}
