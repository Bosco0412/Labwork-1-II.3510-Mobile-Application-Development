package com.tumme.scrudstudents.ui.course // Ensure the package name is correct

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.ui.components.TableHeader

// Assuming you have already created these classes
import com.tumme.scrudstudents.data.local.model.CourseEntity

/**
 * The main UI screen for displaying the list of courses.
 * This Composable function is responsible for observing data from the ViewModel and rendering it on the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    /**
     * An instance of CourseListViewModel, provided by Hilt.
     */
    viewModel: CourseListViewModel = hiltViewModel(),
    /**
     * A lambda function to handle navigation to the course creation form.
     */
    onNavigateToForm: () -> Unit = {},
    /**
     * A lambda function to handle navigation to the course detail screen, passing the course's ID.
     */
    onNavigateToDetail: (Int) -> Unit = {},
    /**
     * ✨ 1. Added a new parameter to handle navigation for editing a course.
     */
    onNavigateToEdit: (Int) -> Unit = {}
) {
    /**
     * Collects the list of courses from the ViewModel's StateFlow.
     */
    val courses by viewModel.courses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Courses") })
        },
        floatingActionButton = {
            // Clicking this button will navigate to the form page for creating a new course
            FloatingActionButton(onClick = onNavigateToForm) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Table header updated to reflect Course properties
            TableHeader(
                cells = listOf("Name", "ECTS", "Level", "Actions"),
                weights = listOf(0.4f, 0.2f, 0.2f, 0.2f) // Weights adjusted to fit new columns
            )

            Spacer(modifier = Modifier.height(8.dp))

            // LazyColumn efficiently displays the list of courses
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // `items` receives the `courses` list and creates a CourseRow for each course
                items(courses) { course ->
                    // CourseRow is a new Composable specifically for displaying a single row of course information
                    CourseRow(
                        course = course,
                        // ✨ 2. The onEdit action now correctly calls the navigation lambda with the course ID.
                        onEdit = { onNavigateToEdit(course.idCourse) },
                        // Call the delete function in the ViewModel
                        onDelete = { viewModel.deleteCourse(course) },
                        // Navigate to the detail page and pass the course's ID
                        onView = { onNavigateToDetail(course.idCourse) },
                        onShare = { /* Share function (not implemented here) */ }
                    )
                }
            }
        }
    }
}

