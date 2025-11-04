package com.tumme.scrudstudents.ui.course

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.CourseEntity

/**
 * A screen to display the details of a single course.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: Int,
    viewModel: CourseListViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    // This state will hold the fetched course. `produceState` is used for one-off async loads.
    val course by produceState<CourseEntity?>(initialValue = null, key1 = courseId) {
        value = viewModel.findCourse(courseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // FIX: Used the AutoMirrored version of the ArrowBack icon.
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Check if the course data has been loaded
            if (course != null) {
                Text("Name: ${course!!.nameCourse}", style = MaterialTheme.typography.titleLarge)
                Text("ECTS: ${course!!.ectsCourse}", style = MaterialTheme.typography.bodyMedium)
                Text("Level: ${course!!.levelCourse.value}", style = MaterialTheme.typography.bodyMedium)
                // Add more details as needed
            } else {
                // Show a loading indicator while the course is being fetched
                CircularProgressIndicator()
            }
        }
    }
}

