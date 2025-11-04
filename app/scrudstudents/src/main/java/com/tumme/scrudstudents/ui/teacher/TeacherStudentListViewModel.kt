package com.tumme.scrudstudents.ui.teacher

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
class TeacherStudentListViewModel @Inject constructor(
    private val scrudRepository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _courses = MutableStateFlow<List<CourseStudentInfo>>(emptyList())
    val courses: StateFlow<List<CourseStudentInfo>> = _courses.asStateFlow()

    private val _selectedCourse = MutableStateFlow<CourseStudentInfo?>(null)
    val selectedCourse: StateFlow<CourseStudentInfo?> = _selectedCourse.asStateFlow()

    private val _students = MutableStateFlow<List<TeacherStudentInfo>>(emptyList())
    val students: StateFlow<List<TeacherStudentInfo>> = _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadTeacherCourses(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val teacher = authRepository.getTeacherByUserId(userId)
                if (teacher != null) {
                    val allCourses = scrudRepository.getAllCourses().first()
                    val allSubscriptions = scrudRepository.getAllSubscribes().first()
                    
                    val teacherCourses = allCourses.filter { it.teacherId == teacher.id }
                    
                    val coursesList = teacherCourses.map { course ->
                        val studentCount = allSubscriptions.count { it.courseId == course.idCourse }
                        CourseStudentInfo(
                            courseId = course.idCourse,
                            courseName = course.nameCourse,
                            studentCount = studentCount
                        )
                    }
                    
                    _courses.value = coursesList
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCourse(course: CourseStudentInfo) {
        _selectedCourse.value = course
        loadStudentsForCourse(course.courseId)
    }

    private fun loadStudentsForCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                val allStudents = scrudRepository.getAllStudents().first()
                val allSubscriptions = scrudRepository.getAllSubscribes().first()
                
                val courseSubscriptions = allSubscriptions.filter { it.courseId == courseId }
                val studentsInCourse = courseSubscriptions.mapNotNull { subscription ->
                    val student = allStudents.find { it.idStudent == subscription.studentId }
                    val studentUser = student?.let { 
                        // Try to find student user by studentId
                        try {
                            authRepository.getStudentUserByStudentId(subscription.studentId)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    student?.let {
                        TeacherStudentInfo(
                            studentId = it.idStudent,
                            firstName = it.firstName,
                            lastName = it.lastName,
                            levelOfStudy = studentUser?.levelOfStudy,
                            grade = subscription.score
                        )
                    }
                }
                
                _students.value = studentsInCourse
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

data class CourseStudentInfo(
    val courseId: Int,
    val courseName: String,
    val studentCount: Int
)
