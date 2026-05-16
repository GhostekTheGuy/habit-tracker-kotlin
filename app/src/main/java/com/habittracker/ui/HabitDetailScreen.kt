package com.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habittracker.R
import com.habittracker.data.HabitLog
import com.habittracker.util.DateUtils
import com.habittracker.util.HabitIcons
import com.habittracker.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    viewModel: HabitViewModel,
    habitId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    LaunchedEffect(habitId) { viewModel.loadHabit(habitId) }
    val habit by viewModel.selectedHabit.collectAsStateWithLifecycle()

    val logsFlow = remember(habitId) { viewModel.logsForHabit(habitId) }
    val logs: List<HabitLog> by logsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegoly") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_ui_back), contentDescription = "Wstecz")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(painterResource(R.drawable.ic_ui_edit), contentDescription = "Edytuj")
                    }
                }
            )
        }
    ) { padding ->
        val h = habit
        if (h == null) {
            Box(Modifier.padding(padding)) { Text("Ladowanie...", Modifier.padding(16.dp)) }
            return@Scaffold
        }

        val accent = HabitIcons.parseColor(h.color)
        val completed = logs.count { it.isCompleted }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(HabitIcons.forName(h.iconName)),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(h.title, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        h.categoryDisplayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            h.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard("Wykonania", completed.toString(), Modifier.weight(1f))
                StatCard("Cel dzienny", "${h.targetCount} ${h.targetUnit}", Modifier.weight(1f))
            }

            Text("Historia", fontWeight = FontWeight.SemiBold)
            if (logs.isEmpty()) {
                Text(
                    "Brak wpisow. Odznacz nawyk podwojnym tapnieciem na liscie.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    logs.take(14).forEach { log ->
                        HistoryRow(dateLabel = DateUtils.toDisplay(log.date), accent = accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(dateLabel: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_ui_check),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            dateLabel.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
