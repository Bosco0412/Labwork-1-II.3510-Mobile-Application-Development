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
fun StudentCourseScreen(
    onBack: () -> Unit,
    viewModel: StudentCourseViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val availableCourses by viewModel.availableCourses.collectAsState()
    val enrolledCourses by viewModel.enrolledCourses.collectAsState()
    val studentLevel by viewModel.studentLevel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    val userId = remember(currentUser, authState) {
        val localCurrentUser = currentUser
        when {
            localCurrentUser != null -> localCurrentUser.id
            authState is com.tumme.scrudstudents.ui.auth.AuthState.Success ->
                (authState as com.tumme.scrudstudents.ui.auth.AuthState.Success).user.id
            else -> null
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            authViewModel.restoreUser()
        }
    }

    LaunchedEffect(userId) {
        userId?.let { id ->
            android.util.Log.d("StudentCourse", "Loading courses for userId: $id")
            viewModel.loadInitialCourses(id)
        } ?: android.util.Log.w("StudentCourse", "No userId available to load courses")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Courses") },
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
            // Student Level Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Your Level: ${studentLevel?.value ?: "Unknown"}",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enrolled Courses Section
            Text(
                text = "Enrolled Courses (${enrolledCourses.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (enrolledCourses.isEmpty()) {
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
                            Icons.Default.School,
                            contentDescription = "No courses",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No enrolled courses",
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
                    items(
                        items = enrolledCourses,
                        key = { it.courseId }
                    ) { course ->
                        // 【修复点 1】将 onUnenroll 传递给 EnrolledCourseCard
                        EnrolledCourseCard(
                            course = course,
                            onUnenroll = {
                                userId?.let { id ->
                                    viewModel.unenrollFromCourse(course.courseId, id)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available Courses Section
            Text(
                text = "Available Courses for ${studentLevel?.value ?: "Your Level"}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (availableCourses.isEmpty()) {
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
                            text = "No courses available for your level",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = availableCourses,
                        key = { it.courseId }
                    ) { course ->
                        AvailableCourseCard(
                            course = course,
                            // isEnrolled 现在永远是 false, 但我们保持字段以防未来逻辑变化
                            isEnrolled = false,
                            onEnroll = {
                                userId?.let { id ->
                                    viewModel.enrollInCourse(course.courseId, id)
                                }
                            }
                            // 【修复点 2】移除 onUnenroll 属性，因为此列表只显示可注册的课程
                        )
                    }
                }
            }
        }
    }
}

// 【修复点 3】更新 EnrolledCourseCard 定义以包含 onUnenroll 动作
@Composable
fun EnrolledCourseCard(
    course: EnrolledCourseInfo,
    onUnenroll: () -> Unit // 新增 onUnenroll 动作
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = "Course",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.nameCourse,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${course.ectsCourse} ECTS • Teacher: ${course.teacherName}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = if (course.grade > 0) String.format("%.1f", course.grade) else "No grade",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (course.grade >= 4.0) Color.Green else if (course.grade > 0) Color.Red else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 【修复点 4】添加 Unenroll 按钮
            Button(
                onClick = onUnenroll,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error // 红色用于取消操作
                )
            ) {
                Text(
                    text = "Unenroll",
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

// 【修复点 5】更新 AvailableCourseCard 定义，移除不必要的 onUnenroll
@Composable
fun AvailableCourseCard(
    course: AvailableCourseInfo,
    isEnrolled: Boolean, // 仍保留此字段，但由于 ViewModel 过滤，它应始终为 false
    onEnroll: () -> Unit
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
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${course.ectsCourse} ECTS • ${course.levelCourse.value}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Teacher: ${course.teacherName}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (course.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = course.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 只显示 Enroll 按钮
            Button(
                onClick = onEnroll,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Enroll",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

data class EnrolledCourseInfo(
    val courseId: Int,
    val nameCourse: String,
    val ectsCourse: Float,
    val teacherName: String,
    val grade: Float
)

data class AvailableCourseInfo(
    val courseId: Int,
    val nameCourse: String,
    val ectsCourse: Float,
    val levelCourse: com.tumme.scrudstudents.data.local.model.LevelCourse,
    val teacherName: String,
    val description: String
)
