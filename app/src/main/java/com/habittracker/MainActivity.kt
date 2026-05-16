package com.habittracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.habittracker.reminder.HabitNotifier
import com.habittracker.ui.AddEditHabitScreen
import com.habittracker.ui.HabitDetailScreen
import com.habittracker.ui.HabitListScreen
import com.habittracker.ui.ManageHabitsScreen
import com.habittracker.ui.theme.HabitTrackerTheme
import com.habittracker.viewmodel.HabitViewModel

object Routes {
    const val LIST = "list"
    const val ADD = "add"
    const val EDIT = "edit/{habitId}"
    const val DETAIL = "detail/{habitId}"
    const val MANAGE = "manage"

    fun edit(id: Long) = "edit/$id"
    fun detail(id: Long) = "detail/$id"
}

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        HabitNotifier.ensureChannel(this)
        maybeRequestNotificationPermission()

        setContent {
            HabitTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HabitApp()
                }
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}


@Composable
fun HabitApp() {
    val navController = rememberNavController()
    val viewModel: HabitViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            HabitListScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate(Routes.ADD) },
                onHabitClick = { id -> navController.navigate(Routes.detail(id)) },
                onHabitLongPress = { id -> navController.navigate(Routes.edit(id)) },
                onManageClick = { navController.navigate(Routes.MANAGE) },
            )
        }
        composable(Routes.MANAGE) {
            ManageHabitsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditHabit = { id -> navController.navigate(Routes.edit(id)) },
            )
        }
        composable(Routes.ADD) {
            AddEditHabitScreen(
                viewModel = viewModel,
                habitId = null,
                onDone = { navController.popBackStack() },
            )
        }
        composable(
            Routes.EDIT,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("habitId") ?: return@composable
            AddEditHabitScreen(
                viewModel = viewModel,
                habitId = id,
                onDone = { navController.popBackStack() },
            )
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("habitId") ?: return@composable
            HabitDetailScreen(
                viewModel = viewModel,
                habitId = id,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.edit(id)) },
            )
        }
    }
}
