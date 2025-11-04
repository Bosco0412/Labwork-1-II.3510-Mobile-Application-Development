package com.tumme.scrudstudents.ui.subscribe

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.SubscribeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribeDetailScreen(
    studentId: Int,
    courseId: Int,
    viewModel: SubscribeListViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    // Collect the full lists of students and courses from the ViewModel
    val students by viewModel.students.collectAsState()
    val courses by viewModel.courses.collectAsState()
    // Asynchronously load the specific subscription information
    val subscription by produceState<SubscribeEntity?>(initialValue = null, studentId, courseId) {
        value = viewModel.findSubscription(studentId, courseId)
    }

    // Find the corresponding student and course names from the lists
    val student = students.find { it.idStudent == studentId }
    val course = courses.find { it.idCourse == courseId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            if (student != null && course != null && subscription != null) {
                Text("Student: ${student.firstName} ${student.lastName}", style = MaterialTheme.typography.titleLarge)
                Text("Course: ${course.nameCourse}", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Score: ${subscription!!.score}", style = MaterialTheme.typography.bodyLarge)
            } else {
                // Show a loading indicator while the data is being loaded
                CircularProgressIndicator()
            }
        }
    }
}

