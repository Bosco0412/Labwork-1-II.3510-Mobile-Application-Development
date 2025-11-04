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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.LevelCourse
import com.tumme.scrudstudents.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    onNavigateToCourses: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onLogout: () -> Unit,
    viewModel: StudentDashboardViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val studentInfo by viewModel.studentInfo.collectAsState()
    val enrolledCourses by viewModel.enrolledCourses.collectAsState()
    val finalGrade by viewModel.finalGrade.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // 2. 监听 currentUser 和 authState 的变化，加载数据
    LaunchedEffect(currentUser, authState) {
        android.util.Log.d("StudentDashboard", "LaunchedEffect triggered - currentUser: ${currentUser?.id}, authState: $authState")

        val localCurrentUser = currentUser

        val idToLoad: Int? = when {
            // 优先级1: 只有当 currentUser 明确存在时才加载数据
            localCurrentUser != null -> {
                android.util.Log.d("StudentDashboard", "Got userId from currentUser: ${localCurrentUser.id}")
                localCurrentUser.id
            }
            // 优先级2: 从 authState.Success 获取（如果它刚刚成功登录）
            authState is com.tumme.scrudstudents.ui.auth.AuthState.Success -> {
                val userId = (authState as com.tumme.scrudstudents.ui.auth.AuthState.Success).user.id
                android.util.Log.d("StudentDashboard", "Got userId from authState.Success: $userId")
                userId
            }
            else -> null
        }

        // 如果成功获取到ID，就调用 ViewModel 加载数据
        if (idToLoad != null) {
            android.util.Log.d("StudentDashboard", "Loading data for userId: $idToLoad")
            viewModel.loadStudentData(idToLoad)
        } else {
            // 如果用户是 null，则清空仪表板数据
            viewModel.clearStudentData()
            android.util.Log.w("StudentDashboard", "No valid user found or session ended. Clearing data.")
        }
    }

    // --- (以下是 UI 代码) ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Student Dashboard",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // FIX: Call authViewModel.logout() before navigating away
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Welcome back!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Display name from currentUser if studentInfo is not loaded yet
                        val userName = studentInfo?.let { "${it.firstName} ${it.lastName}" }
                            ?: currentUser?.let { "${it.firstName} ${it.lastName}" }
                            ?: "Loading..."
                        Text(
                            text = "Name: $userName",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val levelText = studentInfo?.levelOfStudy?.value ?: "Loading..."
                        Text(
                            text = "Level: $levelText",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCourses,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = "Courses",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Browse Courses",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToGrades,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Grade,
                                contentDescription = "Grades",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "View Grades",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Enrolled Courses Summary
            item {
                Text(
                    text = "Enrolled Courses (${enrolledCourses.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            if (enrolledCourses.isEmpty()) {
                item {
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
                                text = "No enrolled courses yet",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Browse courses to enroll",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(enrolledCourses.take(3)) { course ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Book,
                                contentDescription = "Course",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = course.nameCourse,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${course.ectsCourse} ECTS",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${course.score}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                                color = if (course.score >= 10.0) Color.Green else Color.Red
                            )
                        }
                    }
                }
            }

            // Final Grade Card
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
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Final Grade for ${studentInfo?.levelOfStudy?.value}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%.2f", finalGrade),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                            color = if (finalGrade >= 10.0) Color.Green else Color.Red
                        )
                        Text(
                            // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                            text = if (finalGrade >= 10.0) "PASS" else "FAIL",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            // CHANGED: Grade threshold from 4.0 to 10.0 (for 0-20 scale)
                            color = if (finalGrade >= 10.0) Color.Green else Color.Red
                        )
                    }
                }
            }
        }
    }
}
