package com.tumme.scrudstudents.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import com.tumme.scrudstudents.data.repository.AuthRepository
// ... other imports
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Add photoUrl to the StudentInfo data class
data class StudentInfo(
    val firstName: String,
    val lastName: String,
    val levelOfStudy: com.tumme.scrudstudents.data.local.model.LevelCourse,
    val photoUrl: String? // <-- ADDED THIS FIELD
)

data class EnrolledCourse(
    val nameCourse: String,
    val ectsCourse: Float,
    val score: Float
)

@HiltViewModel
class StudentDashboardViewModel @Inject constructor(
    private val scrudRepository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _studentInfo = MutableStateFlow<StudentInfo?>(null)
    val studentInfo: StateFlow<StudentInfo?> = _studentInfo.asStateFlow()

    private val _enrolledCourses = MutableStateFlow<List<EnrolledCourse>>(emptyList())
    val enrolledCourses: StateFlow<List<EnrolledCourse>> = _enrolledCourses.asStateFlow()

    private val _finalGrade = MutableStateFlow(0.0f)
    val finalGrade: StateFlow<Float> = _finalGrade.asStateFlow()

    fun loadStudentData(userId: Int) {
        viewModelScope.launch {
            android.util.Log.d("StudentDashboard", "loadStudentData called with userId: $userId")
            try {
                val user = authRepository.getUserById(userId)
                android.util.Log.d("StudentDashboard", "getUserById($userId) returned: ${user?.firstName} ${user?.lastName}")

                val studentUser = authRepository.getStudentUserByUserId(userId)
                android.util.Log.d("StudentDashboard", "getStudentUserByUserId($userId) returned: studentId=${studentUser?.studentId}, level=${studentUser?.levelOfStudy}")

                if (user == null) {
                    android.util.Log.e("StudentDashboard", "User not found for userId: $userId")
                    return@launch
                }

                if (studentUser == null) {
                    android.util.Log.e("StudentDashboard", "StudentUser not found for userId: $userId")
                    // Still set basic info from user even if studentUser is missing
                    _studentInfo.value = StudentInfo(
                        firstName = user.firstName,
                        lastName = user.lastName,
                        levelOfStudy = com.tumme.scrudstudents.data.local.model.LevelCourse.P1,
                        photoUrl = user.photoUrl // <-- 2. Pass photoUrl from user
                    )
                    return@launch
                }

                android.util.Log.d("StudentDashboard", "Setting studentInfo with level: ${studentUser.levelOfStudy}")
                _studentInfo.value = StudentInfo(
                    firstName = user.firstName,
                    lastName = user.lastName,
                    levelOfStudy = studentUser.levelOfStudy,
                    photoUrl = user.photoUrl // <-- 3. Pass photoUrl from user
                )

                // Load enrolled courses with grades
                loadEnrolledCourses(studentUser.studentId)
            } catch (e: Exception) {
                android.util.Log.e("StudentDashboard", "Error loading student data: ${e.message}", e)
            }
        }
    }

    private suspend fun loadEnrolledCourses(studentId: Int) {
        try {
            // Get all subscriptions and courses as lists
            val subscriptions = scrudRepository.getAllSubscribes().first()
            val courses = scrudRepository.getAllCourses().first()

            val enrolledCoursesList = subscriptions
                .filter { it.studentId == studentId }
                .mapNotNull { subscription ->
                    val course = courses.find { it.idCourse == subscription.courseId }
                    course?.let {
                        EnrolledCourse(
                            nameCourse = it.nameCourse,
                            ectsCourse = it.ectsCourse,
                            score = subscription.score
                        )
                    }
                }

            _enrolledCourses.value = enrolledCoursesList

            // Calculate final grade based on ECTS
            calculateFinalGrade(enrolledCoursesList)
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun calculateFinalGrade(courses: List<EnrolledCourse>) {
        if (courses.isEmpty()) {
            _finalGrade.value = 0.0f
            return
        }

        val totalEcts = courses.sumOf { it.ectsCourse.toDouble() }
        val weightedSum = courses.sumOf { (it.score * it.ectsCourse).toDouble() }

        _finalGrade.value = if (totalEcts > 0) {
            (weightedSum / totalEcts).toFloat()
        } else {
            0.0f
        }
    }

    fun clearStudentData() {
        _studentInfo.value = null
        _enrolledCourses.value = emptyList()
        _finalGrade.value = 0.0f
        android.util.Log.d("StudentDashboard", "Dashboard data cleared.")
    }
}