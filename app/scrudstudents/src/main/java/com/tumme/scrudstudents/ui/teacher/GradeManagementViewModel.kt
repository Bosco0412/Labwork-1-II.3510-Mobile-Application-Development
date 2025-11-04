package com.tumme.scrudstudents.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class GradeManagementViewModel @Inject constructor(
    private val scrudRepository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _courses = MutableStateFlow<List<CourseEntity>>(emptyList())
    val courses: StateFlow<List<CourseEntity>> = _courses.asStateFlow()

    private val _selectedCourse = MutableStateFlow<CourseEntity?>(null)
    val selectedCourse: StateFlow<CourseEntity?> = _selectedCourse.asStateFlow()

    private val _students = MutableStateFlow<List<StudentGradeInfo>>(emptyList())
    val students: StateFlow<List<StudentGradeInfo>> = _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadTeacherCourses(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val teacher = authRepository.getTeacherByUserId(userId)
                if (teacher != null) {
            val allCourses = scrudRepository.getAllCourses().first()
                    val teacherCourses = allCourses.filter { it.teacherId == teacher.id }
                    _courses.value = teacherCourses
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCourse(course: CourseEntity) {
        _selectedCourse.value = course
        loadStudentsForCourse(course.idCourse)
    }

    private fun loadStudentsForCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                val allStudents = scrudRepository.getAllStudents().first()
                val allSubscriptions = scrudRepository.getAllSubscribes().first()
                
                val courseSubscriptions = allSubscriptions.filter { it.courseId == courseId }
                val studentsInCourse = courseSubscriptions.mapNotNull { subscription ->
                    val student = allStudents.find { it.idStudent == subscription.studentId }
                    student?.let {
                        StudentGradeInfo(
                            studentId = it.idStudent,
                            firstName = it.firstName,
                            lastName = it.lastName,
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

    fun updateGrade(studentId: Int, courseId: Int, newGrade: Float) {
        viewModelScope.launch {
            try {
                val subscription = SubscribeEntity(
                    studentId = studentId,
                    courseId = courseId,
                    score = newGrade
                )
                scrudRepository.insertSubscribe(subscription)
                
                // Update local state
                val updatedStudents = _students.value.map { student ->
                    if (student.studentId == studentId) {
                        student.copy(grade = newGrade)
                    } else {
                        student
                    }
                }
                _students.value = updatedStudents
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
