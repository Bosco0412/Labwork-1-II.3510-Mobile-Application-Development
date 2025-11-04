package com.tumme.scrudstudents.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeManagementScreen(
    onBack: () -> Unit,
    viewModel: GradeManagementViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val courses by viewModel.courses.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    val students by viewModel.students.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.restoreUser()
    }

    val userId = remember(currentUser, authState) {
        when {
            currentUser != null -> currentUser?.id
            authState is com.tumme.scrudstudents.ui.auth.AuthState.Success -> 
                (authState as com.tumme.scrudstudents.ui.auth.AuthState.Success).user.id
            else -> null
        }
    }

    LaunchedEffect(userId) {
        userId?.let { id ->
            viewModel.loadTeacherCourses(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grade Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Course Selection
            Text(
                text = "Select Course",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (courses.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "No courses",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No courses assigned to you",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(courses) { course ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.selectCourse(course) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedCourse?.idCourse == course.idCourse) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = "Course",
                                    tint = if (selectedCourse?.idCourse == course.idCourse)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = course.nameCourse,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedCourse?.idCourse == course.idCourse)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${course.ectsCourse} ECTS • ${course.levelCourse.value}",
                                        fontSize = 14.sp,
                                        color = if (selectedCourse?.idCourse == course.idCourse)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (selectedCourse?.idCourse == course.idCourse) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Students List
            val sc = selectedCourse
            if (sc != null) {
                Text(
                    text = "Students in ${sc.nameCourse}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (students.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = "No students",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No students enrolled in this course",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(students) { student ->
                            StudentGradeCard(
                                student = student,
                                onGradeChange = { newGrade ->
                                    viewModel.updateGrade(student.studentId, sc.idCourse, newGrade)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentGradeCard(
    student: StudentGradeInfo,
    onGradeChange: (Float) -> Unit
) {
    var grade by remember { mutableStateOf(student.grade.toString()) }
    var showGradeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Student",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${student.firstName} ${student.lastName}",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Student ID: ${student.studentId}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (student.grade > 0) String.format("%.1f", student.grade) else "No grade",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (student.grade >= 4.0) Color.Green else if (student.grade > 0) Color.Red else Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { showGradeDialog = true }
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Grade",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showGradeDialog) {
        GradeInputDialog(
            currentGrade = student.grade,
            onDismiss = { showGradeDialog = false },
            onConfirm = { newGrade ->
                onGradeChange(newGrade)
                showGradeDialog = false
            }
        )
    }
}

@Composable
fun GradeInputDialog(
    currentGrade: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var grade by remember { mutableStateOf(if (currentGrade > 0) currentGrade.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Grade") },
        text = {
            Column {
                Text("Enter grade (0.0 - 20.0):")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Grade") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val gradeValue = grade.toFloatOrNull()
                    if (gradeValue != null && gradeValue >= 0.0 && gradeValue <= 20.0) {
                        onConfirm(gradeValue)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class StudentGradeInfo(
    val studentId: Int,
    val firstName: String,
    val lastName: String,
    val grade: Float
)
