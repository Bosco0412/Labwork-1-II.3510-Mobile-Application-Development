package com.tumme.scrudstudents.ui.subscribe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.local.model.SubscribeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribeFormScreen(
    // Accept student and course IDs. If they are null, we are in "create" mode.
    studentId: Int?,
    courseId: Int?,
    viewModel: SubscribeListViewModel = hiltViewModel(),
    onSaved: () -> Unit
) {
    val students by viewModel.students.collectAsState()
    val courses by viewModel.courses.collectAsState()

    // --- State for the form fields ---
    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var selectedCourse by remember { mutableStateOf<CourseEntity?>(null) }
    var scoreText by remember { mutableStateOf("") }

    var isStudentMenuExpanded by remember { mutableStateOf(false) }
    var isCourseMenuExpanded by remember { mutableStateOf(false) }
    var isScoreError by remember { mutableStateOf(false) }

    val isEditMode = studentId != null && courseId != null

    // ✨ FIX: This effect is now keyed to the students and courses lists.
    // It will run when the lists are populated, fixing the race condition.
    LaunchedEffect(students, courses, studentId, courseId) {
        if (isEditMode && students.isNotEmpty() && courses.isNotEmpty()) {
            val existingSubscription = viewModel.findSubscription(studentId!!, courseId!!)
            if (existingSubscription != null) {
                // Populate the form fields with the existing data.
                selectedStudent = students.find { it.idStudent == studentId }
                selectedCourse = courses.find { it.idCourse == courseId }
                scoreText = existingSubscription.score.toString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val title = if (isEditMode) "Edit Subscription" else "Add Subscription"
        Text(title, style = MaterialTheme.typography.headlineSmall)

        // --- Student Dropdown Menu ---
        // In edit mode, this dropdown is disabled as you should only edit the score.
        ExposedDropdownMenuBox(
            expanded = isStudentMenuExpanded && !isEditMode,
            onExpandedChange = { if (!isEditMode) isStudentMenuExpanded = !isStudentMenuExpanded }
        ) {
            OutlinedTextField(
                value = selectedStudent?.let { "${it.firstName} ${it.lastName}" } ?: "Select a Student",
                onValueChange = {},
                readOnly = true,
                label = { Text("Student") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStudentMenuExpanded) },
                enabled = !isEditMode,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            if (!isEditMode) {
                ExposedDropdownMenu(
                    expanded = isStudentMenuExpanded,
                    onDismissRequest = { isStudentMenuExpanded = false }
                ) {
                    students.forEach { student ->
                        DropdownMenuItem(
                            text = { Text("${student.firstName} ${student.lastName}") },
                            onClick = {
                                selectedStudent = student
                                isStudentMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // --- Course Dropdown Menu ---
        ExposedDropdownMenuBox(
            expanded = isCourseMenuExpanded && !isEditMode,
            onExpandedChange = { if (!isEditMode) isCourseMenuExpanded = !isCourseMenuExpanded }
        ) {
            OutlinedTextField(
                value = selectedCourse?.nameCourse ?: "Select a Course",
                onValueChange = {},
                readOnly = true,
                label = { Text("Course") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCourseMenuExpanded) },
                enabled = !isEditMode,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            if (!isEditMode) {
                ExposedDropdownMenu(
                    expanded = isCourseMenuExpanded,
                    onDismissRequest = { isCourseMenuExpanded = false }
                ) {
                    courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text(course.nameCourse) },
                            onClick = {
                                selectedCourse = course
                                isCourseMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // --- Score Input Field ---
        OutlinedTextField(
            value = scoreText,
            onValueChange = {
                scoreText = it
                isScoreError = false
            },
            label = { Text("Score") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = isScoreError,
            supportingText = {
                if (isScoreError) {
                    Text("Score must be a valid number.")
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // --- Save Button ---
        Button(
            onClick = {
                val student = selectedStudent
                val course = selectedCourse
                val score = scoreText.toFloatOrNull()
                isScoreError = score == null

                if (student != null && course != null && !isScoreError) {
                    val subscription = SubscribeEntity(
                        studentId = student.idStudent,
                        courseId = course.idCourse,
                        score = score!!
                    )
                    if (isEditMode) {
                        viewModel.updateSubscribe(subscription)
                    } else {
                        viewModel.insertSubscribe(subscription)
                    }
                    onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedStudent != null && selectedCourse != null
        ) {
            Text("Save Subscription")
        }
    }
}

