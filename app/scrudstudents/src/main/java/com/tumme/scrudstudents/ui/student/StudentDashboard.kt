package com.tumme.scrudstudents.ui.student

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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

    // --- Setup for Photo Picker ---
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUserId = currentUser?.id // Get current user ID

    // 1. Create a launcher to start the gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            // 3. When the user picks a photo, the URI is received here
            if (uri != null && currentUserId != null) {
                // We must copy this file, as the original URI might be temporary
                val filename = "user_${currentUserId}_profile.jpg"
                val newPhotoPath = saveImageToInternalStorage(context, uri, filename)

                if (newPhotoPath != null) {
                    // 4. Update the ViewModel
                    scope.launch {
                        authViewModel.updatePhotoUrl(currentUserId, newPhotoPath)
                    }
                    android.util.Log.d("PhotoUploader", "New photo saved to: $newPhotoPath")
                }
            }
        }
    )
    // --- End of Photo Picker Setup ---

    // Listen for changes in currentUser and authState to load data
    LaunchedEffect(currentUser, authState) {
        android.util.Log.d("StudentDashboard", "LaunchedEffect triggered - currentUser: ${currentUser?.id}, authState: $authState")

        val localCurrentUser = currentUser

        val idToLoad: Int? = when {
            // Priority 1: Load data only if currentUser explicitly exists
            localCurrentUser != null -> {
                android.util.Log.d("StudentDashboard", "Got userId from currentUser: ${localCurrentUser.id}")
                localCurrentUser.id
            }
            // Priority 2: Get from authState.Success (if just logged in)
            authState is com.tumme.scrudstudents.ui.auth.AuthState.Success -> {
                val userId = (authState as com.tumme.scrudstudents.ui.auth.AuthState.Success).user.id
                android.util.Log.d("StudentDashboard", "Got userId from authState.Success: $userId")
                userId
            }
            else -> null
        }

        // If an ID is successfully obtained, call the ViewModel to load data
        if (idToLoad != null) {
            android.util.Log.d("StudentDashboard", "Loading data for userId: $idToLoad")
            viewModel.loadStudentData(idToLoad)
        } else {
            // If the user is null, clear the dashboard data
            viewModel.clearStudentData()
            android.util.Log.w("StudentDashboard", "No valid user found or session ended. Clearing data.")
        }
    }


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
            // --- Welcome Card ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    // Changed to a Row for side-by-side layout
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically // Vertically center content
                    ) {
                        // --- Clickable Profile Image ---
                        AsyncImage(
                            model = studentInfo?.photoUrl ?: currentUser?.photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable(enabled = currentUserId != null) { // Make clickable
                                    // Trigger the gallery launcher
                                    galleryLauncher.launch("image/*") // Show images only
                                },
                            contentScale = ContentScale.Crop, // Ensure image fills the circle
                            placeholder = rememberVectorPainter(Icons.Default.Person),
                            error = rememberVectorPainter(Icons.Default.Person),
                            fallback = rememberVectorPainter(Icons.Default.Person)
                        )
                        // --- End of Image ---

                        Spacer(modifier = Modifier.width(16.dp))

                        // Column for text content
                        Column(
                            modifier = Modifier.weight(1f) // Occupy remaining space
                        ) {
                            Text(
                                text = "Welcome back!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))

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
            }
            // --- End of Welcome Card ---


            // --- Quick Actions ---
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
            // --- End of Quick Actions ---


            // --- Enrolled Courses Summary ---
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
                                color = if (course.score >= 10.0) Color.Green else Color.Red
                            )
                        }
                    }
                }
            }
            // --- End of Enrolled Courses ---


            // --- Final Grade Card ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
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
                            color = if (finalGrade >= 10.0) Color.Green else Color.Red
                        )
                        Text(
                            text = if (finalGrade >= 10.0) "PASS" else "FAIL",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (finalGrade >= 10.0) Color.Green else Color.Red
                        )
                    }
                }
            }
            // --- End of Final Grade ---
        }
    }
}


/**
 * Helper function to copy an image from a content URI (like the gallery)
 * to the app's internal private storage.
 */
private fun saveImageToInternalStorage(context: Context, uri: Uri, filename: String): String? {
    return try {
        // Open an input stream to read the selected image
        val inputStream = context.contentResolver.openInputStream(uri)

        // Create a new destination file in the app's private files directory
        val file = File(context.filesDir, filename)

        // Open an output stream to write the new file
        val outputStream = FileOutputStream(file)

        // Copy the data
        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()

        // Return the new file's absolute path (Coil can load this path)
        file.absolutePath

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}