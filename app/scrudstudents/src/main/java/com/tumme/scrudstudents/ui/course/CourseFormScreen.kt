package com.tumme.scrudstudents.ui.course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.LevelCourse
import com.tumme.scrudstudents.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseFormScreen(
    courseId: Int?,
    viewModel: CourseListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onSaved: () -> Unit = {}
) {
    // --- State Variables ---
    var nameCourse by remember { mutableStateOf("") }
    var ectsCourseText by remember { mutableStateOf("") }
    var levelCourse by remember { mutableStateOf(LevelCourse.B1) }
    var isLevelMenuExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    // NEW 1. Stores the original teacherId for update operations, preventing loss of ownership.
    var originalTeacherId by remember { mutableStateOf<Int?>(null) }

    // Add new state variables for validation errors
    var isNameCourseError by remember { mutableStateOf(false) }
    var isEctsCourseError by remember { mutableStateOf(false) }

    // Get current user for teacherId
    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.restoreUser()
    }

    // Use LaunchedEffect to load existing course data in "Edit" mode
    LaunchedEffect(courseId) {
        if (courseId != null) {
            val existingCourse = viewModel.findCourse(courseId)
            if (existingCourse != null) {
                // Populate form state with fetched data
                nameCourse = existingCourse.nameCourse
                ectsCourseText = existingCourse.ectsCourse.toString()
                levelCourse = existingCourse.levelCourse
                description = existingCourse.description
                // NEW 2. Store original teacherId
                originalTeacherId = existingCourse.teacherId
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val title = if (courseId == null) "Add a New Course" else "Edit Course"
        Text(title, style = MaterialTheme.typography.headlineSmall)

        // Course Name Input Field
        OutlinedTextField(
            value = nameCourse,
            onValueChange = {
                nameCourse = it
                // Clear error state when user starts typing
                isNameCourseError = false
            },
            label = { Text("Course Name") },
            modifier = Modifier.fillMaxWidth(),
            // Determine whether to show error style based on error state
            isError = isNameCourseError,
            // Display specific error message if there is an error
            supportingText = {
                if (isNameCourseError) {
                    Text("Course name cannot be empty.")
                }
            }
        )

        // ECTS Credits Input Field
        OutlinedTextField(
            value = ectsCourseText,
            onValueChange = {
                ectsCourseText = it
                isEctsCourseError = false
            },
            label = { Text("ECTS Credits") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = isEctsCourseError,
            supportingText = {
                if (isEctsCourseError) {
                    Text("ECTS must be a number greater than 0.")
                }
            }
        )

        // Description field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        // Course Level Dropdown Menu
        ExposedDropdownMenuBox(
            expanded = isLevelMenuExpanded,
            onExpandedChange = { isLevelMenuExpanded = !isLevelMenuExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = levelCourse.value,
                onValueChange = {},
                readOnly = true,
                label = { Text("Level") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLevelMenuExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isLevelMenuExpanded,
                onDismissRequest = { isLevelMenuExpanded = false }
            ) {
                LevelCourse.entries.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level.value) },
                        onClick = {
                            levelCourse = level
                            isLevelMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                // Execute detailed validation logic on click
                val ects = ectsCourseText.toFloatOrNull()
                isNameCourseError = nameCourse.isBlank()
                isEctsCourseError = ects == null || ects <= 0

                // Only proceed with save operation if all validation passes
                if (!isNameCourseError && !isEctsCourseError) {
                    // Try multiple sources to get userId
                    val userId = when {
                        currentUser != null -> currentUser?.id
                        authState is com.tumme.scrudstudents.ui.auth.AuthState.Success ->
                            (authState as com.tumme.scrudstudents.ui.auth.AuthState.Success).user.id
                        else -> {
                            // Try from SharedPreferences via AuthViewModel
                            val savedId = authViewModel.getLastUserId()
                            if (savedId != null) {
                                // Restore user first
                                authViewModel.restoreUser()
                            }
                            savedId
                        }
                    }

                    android.util.Log.d("CourseForm", "Saving course with userId: $userId")

                    val courseToSave = CourseEntity(
                        idCourse = courseId ?: (0..Int.MAX_VALUE).random(),
                        nameCourse = nameCourse,
                        ectsCourse = ects!!, // We are sure ects is not null here
                        levelCourse = levelCourse,
                        // FIX: If in edit mode (courseId != null), use the loaded originalTeacherId.
                        // If in add mode (courseId == null), use 0 (CourseListViewModel will handle setting it).
                        teacherId = if (courseId != null) (originalTeacherId ?: 0) else 0,
                        description = description
                    )

                    if (courseId == null) {
                        if (userId != null) {
                            viewModel.insertCourse(courseToSave, userId)
                        } else {
                            android.util.Log.e("CourseForm", "Cannot save course: userId is null")
                            // Show error to user
                        }
                    } else {
                        // The update operation uses the original teacherId retained in courseToSave
                        viewModel.updateCourse(courseToSave)
                    }
                    onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Course")
        }
    }
}
