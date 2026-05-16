package com.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habittracker.R
import com.habittracker.util.HabitIcons
import com.habittracker.viewmodel.HabitUi
import com.habittracker.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHabitsScreen(
    viewModel: HabitViewModel,
    onBack: () -> Unit,
    onEditHabit: (Long) -> Unit,
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarzadzaj nawykami") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_ui_back), contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        if (state.habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Brak nawykow.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.habits, key = { it.habit.id }) { item ->
                    ManageHabitRow(
                        item = item,
                        onEdit = { onEditHabit(item.habit.id) },
                        onDelete = { viewModel.deleteHabit(item.habit) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ManageHabitRow(
    item: HabitUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val habit = item.habit
    val accent = HabitIcons.parseColor(habit.color)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(HabitIcons.forName(habit.iconName)),
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = habit.categoryDisplayName +
                        if (habit.reminderEnabled) "  •  ${formatMinutes(habit.reminderTime)}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    painterResource(R.drawable.ic_ui_edit),
                    contentDescription = "Edytuj",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painterResource(R.drawable.ic_ui_delete),
                    contentDescription = "Usun",
                    tint = Color(0xFFEF4444),
                )
            }
        }
    }
}
