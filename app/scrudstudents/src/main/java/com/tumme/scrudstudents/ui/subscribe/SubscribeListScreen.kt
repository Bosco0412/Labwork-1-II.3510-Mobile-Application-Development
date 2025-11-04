package com.tumme.scrudstudents.ui.subscribe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import com.tumme.scrudstudents.ui.components.TableHeader

// A helper data class to combine information for the UI, fulfilling the challenge requirement.
data class SubscriptionDetails(
    val studentName: String,
    val courseName: String,
    val score: Float,
    val studentId: Int,
    val courseId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribeListScreen(
    viewModel: SubscribeListViewModel = hiltViewModel(),
    onNavigateToForm: () -> Unit = {},
    // We will need these later to make edit/view buttons work
    onNavigateToEdit: (Int, Int) -> Unit = { _, _ -> },
    onNavigateToDetail: (Int, Int) -> Unit = { _, _ -> }
) {
    // Collect all necessary data streams from the ViewModel.
    val subscribes by viewModel.subscribes.collectAsState()
    val students by viewModel.students.collectAsState()
    val courses by viewModel.courses.collectAsState()

    // Challenge: Combine the data streams to create a list with details.
    val subscriptionDetails by remember(subscribes, students, courses) {
        derivedStateOf {
            subscribes.mapNotNull { subscribe ->
                val student = students.find { it.idStudent == subscribe.studentId }
                val course = courses.find { it.idCourse == subscribe.courseId }

                if (student != null && course != null) {
                    SubscriptionDetails(
                        studentName = "${student.firstName} ${student.lastName}",
                        courseName = course.nameCourse,
                        score = subscribe.score,
                        studentId = subscribe.studentId,
                        courseId = subscribe.courseId
                    )
                } else {
                    null
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Subscriptions") })
        },
        floatingActionButton = {
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
            // Header for the subscriptions list.
            TableHeader(
                cells = listOf("Student", "Course", "Score", "Actions"),
                weights = listOf(0.35f, 0.35f, 0.1f, 0.2f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(subscriptionDetails) { details ->
                    SubscribeRow(
                        details = details,
                        // ✨ FIX: Implement the delete logic here.
                        onDelete = {
                            // Recreate the entity from the details to pass to the ViewModel
                            val subscriptionToDelete = SubscribeEntity(
                                studentId = details.studentId,
                                courseId = details.courseId,
                                score = details.score
                            )
                            viewModel.deleteSubscribe(subscriptionToDelete)
                        },
                        onEdit = { onNavigateToEdit(details.studentId, details.courseId) },
                        onView = { onNavigateToDetail(details.studentId, details.courseId) }
                    )
                }
            }
        }
    }
}

