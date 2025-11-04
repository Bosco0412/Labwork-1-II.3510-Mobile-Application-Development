package com.tumme.ii3510_courseapp_2526

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tumme.ii3510_courseapp_2526.ui.TaskDetailScreen
import com.tumme.ii3510_courseapp_2526.ui.TaskListScreen
import com.tumme.ii3510_courseapp_2526.viewmodel.TaskViewModel
import kotlin.getValue

class TaskList : AppCompatActivity() {

    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

           /* MaterialTheme(
                colorScheme = lightColorScheme()
            ) {
                TaskApp(viewModel)
            }*/

            val darkColors = darkColorScheme(
                primary = Color(0xFF00BCD4),
                background = Color.Black
            )
            MaterialTheme(colorScheme = darkColors) {
                TaskApp(viewModel)
            }
        }
    }
}

@Composable
fun TaskApp(viewModel: TaskViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            TaskListScreen(
                viewModel = viewModel,
                onTaskClick = { id -> navController.navigate("detail/$id") }
            )
        }
        composable("detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toInt() ?: 0
            val task = viewModel.getTaskById(taskId)
            TaskDetailScreen(task = task, onBack = { navController.popBackStack() })
        }
    }
}