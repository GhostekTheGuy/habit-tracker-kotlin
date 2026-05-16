package com.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habittracker.R
import com.habittracker.data.Habit
import com.habittracker.util.HabitIcons
import com.habittracker.viewmodel.HabitViewModel
import kotlin.math.roundToInt

private val CATEGORIES = listOf(
    "health" to "Zdrowie",
    "productivity" to "Produktywnosc",
    "sport" to "Sport",
    "mindfulness" to "Mindfulness",
    "learning" to "Nauka",
    "other" to "Inne",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    viewModel: HabitViewModel,
    habitId: Long?,
    onDone: () -> Unit,
) {
    val isEdit = habitId != null
    val existing by viewModel.selectedHabit.collectAsStateWithLifecycle()

    LaunchedEffect(habitId) {
        if (habitId != null) viewModel.loadHabit(habitId) else viewModel.clearSelectedHabit()
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("other") }
    var iconName by remember { mutableStateOf("star") }
    var color by remember { mutableStateOf("#8B8BCD") }
    var targetCount by remember { mutableStateOf(1f) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableIntStateOf(480) }
    var showTimePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(existing) {
        val h = existing
        if (isEdit && h != null && !initialized) {
            title = h.title
            description = h.description ?: ""
            category = h.category
            iconName = h.iconName
            color = h.color
            targetCount = h.targetCount.toFloat()
            reminderEnabled = h.reminderEnabled
            reminderTime = h.reminderTime
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edytuj nawyk" else "Nowy nawyk") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(painterResource(R.drawable.ic_ui_back), contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (titleError != null) titleError = null
                },
                label = { Text("Nazwa nawyku *") },
                isError = titleError != null,
                supportingText = { titleError?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis (opcjonalnie)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Kategoria", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CATEGORIES.forEach { (key, label) ->
                    FilterChip(
                        selected = category == key,
                        onClick = { category = key },
                        label = { Text(label) },
                    )
                }
            }

            Text("Ikona", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HabitIcons.available.forEach { (name, iconRes) ->
                    val selected = iconName == name
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(HabitIcons.parseColor(color))
                            .then(
                                if (selected) Modifier.border(3.dp, Color.Black, CircleShape)
                                else Modifier
                            )
                            .clickable { iconName = name },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painterResource(iconRes), contentDescription = name, tint = Color.White)
                    }
                }
            }

            Text("Kolor", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HabitIcons.palette.forEach { hex ->
                    val selected = color == hex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(HabitIcons.parseColor(hex))
                            .then(
                                if (selected) Modifier.border(3.dp, Color.Black, CircleShape)
                                else Modifier
                            )
                            .clickable { color = hex },
                    )
                }
            }

            Text("Cel dzienny: ${targetCount.roundToInt()} razy", fontWeight = FontWeight.SemiBold)
            Slider(
                value = targetCount,
                onValueChange = { targetCount = it },
                valueRange = 1f..10f,
                steps = 8,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Przypomnienie")
                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
            }

            if (reminderEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Godzina")
                    OutlinedButton(onClick = { showTimePicker = true }) {
                        Text(formatMinutes(reminderTime))
                    }
                }
            }

            if (showTimePicker) {
                val state = rememberTimePickerState(
                    initialHour = reminderTime / 60,
                    initialMinute = reminderTime % 60,
                    is24Hour = true,
                )
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            reminderTime = state.hour * 60 + state.minute
                            showTimePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") }
                    },
                    text = { TimePicker(state = state) },
                )
            }

            Spacer(Modifier.size(8.dp))

            Button(
                onClick = {
                    val trimmed = title.trim()
                    when {
                        trimmed.isEmpty() -> titleError = "Nazwa nie moze byc pusta"
                        trimmed.length < 2 -> titleError = "Nazwa musi miec min. 2 znaki"
                        else -> {
                            saveHabit(
                                viewModel = viewModel,
                                existing = existing,
                                isEdit = isEdit,
                                title = trimmed,
                                description = description.trim().ifEmpty { null },
                                category = category,
                                iconName = iconName,
                                color = color,
                                targetCount = targetCount.roundToInt(),
                                reminderEnabled = reminderEnabled,
                                reminderTime = reminderTime,
                            )
                            onDone()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isEdit) "Zapisz zmiany" else "Dodaj nawyk")
            }
        }
    }
}

private fun saveHabit(
    viewModel: HabitViewModel,
    existing: Habit?,
    isEdit: Boolean,
    title: String,
    description: String?,
    category: String,
    iconName: String,
    color: String,
    targetCount: Int,
    reminderEnabled: Boolean,
    reminderTime: Int,
) {
    if (isEdit && existing != null) {
        viewModel.updateHabit(
            existing.copy(
                title = title,
                description = description,
                category = category,
                iconName = iconName,
                color = color,
                targetCount = targetCount,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime,
            )
        )
    } else {
        viewModel.addHabit(
            Habit(
                title = title,
                description = description,
                category = category,
                iconName = iconName,
                color = color,
                targetCount = targetCount,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime,
            )
        )
    }
}

internal fun formatMinutes(minutes: Int): String =
    "%02d:%02d".format(minutes / 60, minutes % 60)
