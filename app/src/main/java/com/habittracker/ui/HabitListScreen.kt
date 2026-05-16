package com.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habittracker.R
import com.habittracker.util.HabitIcons
import com.habittracker.viewmodel.HabitListState
import com.habittracker.viewmodel.HabitUi
import com.habittracker.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    viewModel: HabitViewModel,
    onAddClick: () -> Unit,
    onHabitClick: (Long) -> Unit,
    onHabitLongPress: (Long) -> Unit,
    onManageClick: () -> Unit,
) {
    val state: HabitListState by viewModel.listState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moje nawyki") },
                actions = {
                    IconButton(onClick = onManageClick) {
                        Icon(
                            painterResource(R.drawable.ic_ui_list),
                            contentDescription = "Zarzadzaj nawykami",
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(painterResource(R.drawable.ic_ui_add), contentDescription = "Dodaj nawyk")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ProgressHeader(completed = state.completedCount, total = state.totalCount)

            if (state.habits.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.habits, key = { it.habit.id }) { item ->
                        SwipeableHabitCard(
                            item = item,
                            onSetCompleted = { completed ->
                                viewModel.setCompletedToday(item.habit.id, completed)
                            },
                            onDoubleTap = { viewModel.toggleToday(item.habit.id) },
                            onClick = { onHabitClick(item.habit.id) },
                            onLongPress = { onHabitLongPress(item.habit.id) },
                            onEdit = { onHabitLongPress(item.habit.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(completed: Int, total: Int) {
    val ratio = if (total > 0) completed.toFloat() / total else 0f
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Dzisiaj: $completed / $total wykonane",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.size(8.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Brak nawykow.\nDodaj pierwszy przyciskiem +",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableHabitCard(
    item: HabitUi,
    onSetCompleted: (Boolean) -> Unit,
    onDoubleTap: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onEdit: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                // w prawo -> oznacz jako wykonany na dzis
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSetCompleted(true)
                    false
                }
                // w lewo -> oznacz jako niewykonany na dzis
                SwipeToDismissBoxValue.EndToStart -> {
                    onSetCompleted(false)
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isComplete = direction == SwipeToDismissBoxValue.StartToEnd
            // w prawo (approve) -> zielony, w lewo (decline) -> zolty
            val bg = if (isComplete) Color(0xFF22C55E) else Color(0xFFFACC15)
            val alignment = if (isComplete) Alignment.CenterStart else Alignment.CenterEnd
            val iconRes = if (isComplete) R.drawable.ic_ui_check else R.drawable.ic_ui_close
            val desc = if (isComplete) "Wykonany na dzis" else "Niewykonany na dzis"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bg)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment,
            ) {
                Icon(painterResource(iconRes), contentDescription = desc, tint = Color.White)
            }
        }
    ) {
        HabitCard(
            item = item,
            onDoubleTap = onDoubleTap,
            onClick = onClick,
            onLongPress = onLongPress,
            onEdit = onEdit,
        )
    }
}

@Composable
private fun HabitCard(
    item: HabitUi,
    onDoubleTap: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onEdit: () -> Unit,
) {
    val habit = item.habit
    val accent = HabitIcons.parseColor(habit.color)
    val cardColor = if (item.isCompletedToday) {
        Color(0xFFDCFCE7) // jasna zielen dla wykonanego nawyku
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(habit.id) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleTap() },
                    onLongPress = { onLongPress() },
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HabitIcons.forName(habit.iconName)),
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.isCompletedToday)
                        TextDecoration.LineThrough else TextDecoration.None,
                )
                Text(
                    text = habit.categoryDisplayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (item.isCompletedToday) {
                Icon(
                    painterResource(R.drawable.ic_ui_check),
                    contentDescription = "Wykonane dzis",
                    tint = accent,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    painterResource(R.drawable.ic_ui_edit),
                    contentDescription = "Edytuj nawyk",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
