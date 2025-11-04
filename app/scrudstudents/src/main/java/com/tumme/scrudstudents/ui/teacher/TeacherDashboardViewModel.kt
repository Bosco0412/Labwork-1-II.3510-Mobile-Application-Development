package com.tumme.scrudstudents.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.local.model.TeacherEntity
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class TeacherInfo(
    val firstName: String,
    val lastName: String,
    val department: String,
    val specialization: String
)

@HiltViewModel
class TeacherDashboardViewModel @Inject constructor(
    private val scrudRepository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _teacherInfo = MutableStateFlow<TeacherInfo?>(null)
    val teacherInfo: StateFlow<TeacherInfo?> = _teacherInfo.asStateFlow()

    private val _myCourses = MutableStateFlow<List<CourseEntity>>(emptyList())
    val myCourses: StateFlow<List<CourseEntity>> = _myCourses.asStateFlow()

    private val _totalStudents = MutableStateFlow(0)
    val totalStudents: StateFlow<Int> = _totalStudents.asStateFlow()

    fun loadTeacherData(userId: Int) {
        viewModelScope.launch {
            try {
                val user = authRepository.getUserById(userId)
                val teacher = authRepository.getTeacherByUserId(userId)
                
                if (user == null) {
                    android.util.Log.e("TeacherDashboard", "User not found for userId: $userId")
                    return@launch
                }
                
                if (teacher == null) {
                    android.util.Log.e("TeacherDashboard", "Teacher not found for userId: $userId")
                    // Still set basic info from user even if teacher is missing
                    _teacherInfo.value = TeacherInfo(
                        firstName = user.firstName,
                        lastName = user.lastName,
                        department = "General",
                        specialization = "General"
                    )
                    return@launch
                }
                
                _teacherInfo.value = TeacherInfo(
                    firstName = user.firstName,
                    lastName = user.lastName,
                    department = teacher.department,
                    specialization = teacher.specialization
                )

                // Load courses taught by this teacher
                loadMyCourses(teacher.id)
            } catch (e: Exception) {
                android.util.Log.e("TeacherDashboard", "Error loading teacher data: ${e.message}", e)
            }
        }
    }

    private suspend fun loadMyCourses(teacherId: Int) {
        try {
            val allCourses = scrudRepository.getAllCourses().first()
            val myCoursesList = allCourses.filter { it.teacherId == teacherId }
            _myCourses.value = myCoursesList

            // Count total students across all courses
            val subscriptions = scrudRepository.getAllSubscribes().first()
            val totalStudentsCount = myCoursesList.sumOf { course ->
                subscriptions.count { it.courseId == course.idCourse }
            }
            _totalStudents.value = totalStudentsCount
        } catch (e: Exception) {
            // Handle error
        }
    }
}
