package com.tumme.scrudstudents.ui.student

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
fun StudentGradesScreen(
    onBack: () -> Unit,
    viewModel: StudentGradesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val grades by viewModel.grades.collectAsState()
    val finalGrade by viewModel.finalGrade.collectAsState()
    val levelOfStudy by viewModel.levelOfStudy.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId ->
            viewModel.loadGrades(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Grades") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Final Grade Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                            containerColor = if (finalGrade >= 10.0)
                                Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Final Grade for ${levelOfStudy?.value ?: "Level"}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                                color = if (finalGrade >= 10.0) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = String.format("%.2f", finalGrade),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                                color = if (finalGrade >= 10.0) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                                text = if (finalGrade >= 10.0) "PASS" else "FAIL",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                                color = if (finalGrade >= 10.0) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }

                // Grades List Header
                item {
                    Text(
                        text = "Course Grades",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }

                if (grades.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "No grades",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No grades yet",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Enroll in courses to see your grades",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(grades) { gradeInfo ->
                        GradeCard(gradeInfo = gradeInfo)
                    }
                }
            }
        }
    }
}

@Composable
fun GradeCard(
    gradeInfo: StudentGradeInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = "Course",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gradeInfo.courseName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${gradeInfo.ectsCourse} ECTS",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Teacher",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = gradeInfo.teacherName,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (gradeInfo.grade > 0) String.format("%.2f", gradeInfo.grade) else "N/A",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                            gradeInfo.grade >= 10.0 -> Color(0xFF2E7D32)
                            gradeInfo.grade > 0 -> Color(0xFFC62828)
                            else -> Color.Gray
                        }
                    )
                    if (gradeInfo.grade > 0) {
                        Text(
                            // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                            text = if (gradeInfo.grade >= 10.0) "PASS" else "FAIL",
                            fontSize = 12.sp,
                            // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                            color = if (gradeInfo.grade >= 10.0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

data class StudentGradeInfo(
    val courseName: String,
    val ectsCourse: Float,
    val teacherName: String,
    val grade: Float
)

