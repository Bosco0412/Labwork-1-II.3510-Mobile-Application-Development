package com.tumme.scrudstudents.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.local.model.LevelCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentGradesViewModel @Inject constructor(
    private val scrudRepository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _grades = MutableStateFlow<List<StudentGradeInfo>>(emptyList())
    val grades: StateFlow<List<StudentGradeInfo>> = _grades.asStateFlow()

    private val _finalGrade = MutableStateFlow(0.0f)
    val finalGrade: StateFlow<Float> = _finalGrade.asStateFlow()

    private val _levelOfStudy = MutableStateFlow<LevelCourse?>(null)
    val levelOfStudy: StateFlow<LevelCourse?> = _levelOfStudy.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadGrades(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val studentUser = authRepository.getStudentUserByUserId(userId)
                if (studentUser != null) {
                    _levelOfStudy.value = studentUser.levelOfStudy
                    
                    // Get all subscriptions and courses
                    val subscriptions = scrudRepository.getAllSubscribes().first()
                    val courses = scrudRepository.getAllCourses().first()
                    
                    // Get student's subscriptions
                    val studentSubscriptions = subscriptions.filter { it.studentId == studentUser.studentId }
                    
                    // Create grade info list
                    val gradesList = studentSubscriptions.mapNotNull { subscription ->
                        val course = courses.find { it.idCourse == subscription.courseId }
                        if (course != null) {
                            // Get teacher name
                            android.util.Log.d("StudentGrades", "Loading grade for course: ${course.nameCourse}, teacherId: ${course.teacherId}")
                            val teacher = if (course.teacherId > 0) {
                                authRepository.getTeacherById(course.teacherId)
                            } else {
                                android.util.Log.w("StudentGrades", "Course ${course.nameCourse} has invalid teacherId: ${course.teacherId}")
                                null
                            }
                            val teacherName = if (teacher != null) {
                                val teacherUser = authRepository.getUserById(teacher.userId)
                                teacherUser?.let { 
                                    android.util.Log.d("StudentGrades", "Found teacher: ${it.firstName} ${it.lastName}")
                                    "${it.firstName} ${it.lastName}" 
                                } ?: {
                                    android.util.Log.w("StudentGrades", "TeacherUser not found for teacher.userId: ${teacher.userId}")
                                    "Unknown Teacher"
                                }()
                            } else {
                                android.util.Log.w("StudentGrades", "Teacher not found for teacherId: ${course.teacherId}")
                                "Unknown Teacher"
                            }
                            
                            StudentGradeInfo(
                                courseName = course.nameCourse,
                                ectsCourse = course.ectsCourse,
                                teacherName = teacherName,
                                grade = subscription.score
                            )
                        } else {
                            null
                        }
                    }
                    
                    _grades.value = gradesList
                    
                    // Calculate final grade based on ECTS
                    calculateFinalGrade(gradesList)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateFinalGrade(grades: List<StudentGradeInfo>) {
        if (grades.isEmpty()) {
            _finalGrade.value = 0.0f
            return
        }

        val totalEcts = grades.sumOf { it.ectsCourse.toDouble() }
        val weightedSum = grades.sumOf { (it.grade * it.ectsCourse).toDouble() }
        
        _finalGrade.value = if (totalEcts > 0) {
            (weightedSum / totalEcts).toFloat()
        } else {
            0.0f
        }
    }
}
