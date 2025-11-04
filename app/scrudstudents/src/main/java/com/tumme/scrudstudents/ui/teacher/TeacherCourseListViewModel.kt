package com.tumme.scrudstudents.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import com.tumme.scrudstudents.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherCourseListViewModel @Inject constructor(
    private val scrudRepository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _teacherId = MutableStateFlow<Int?>(null)
    
    private val _courses = MutableStateFlow<List<TeacherCourseInfo>>(emptyList())
    val courses: StateFlow<List<TeacherCourseInfo>> = _courses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _teacherEntityId: Int? = null

    init {
        // Listen to course changes and automatically update the list
        viewModelScope.launch {
            combine(
                scrudRepository.getAllCourses(),
                scrudRepository.getAllSubscribes(),
                _teacherId
            ) { allCourses, allSubscriptions, teacherId ->
                if (teacherId != null && _teacherEntityId != null) {
                    val teacherCourses = allCourses.filter { it.teacherId == _teacherEntityId }
                    
                    teacherCourses.map { course ->
                        val studentCount = allSubscriptions.count { it.courseId == course.idCourse }
                        TeacherCourseInfo(
                            idCourse = course.idCourse,
                            nameCourse = course.nameCourse,
                            ectsCourse = course.ectsCourse,
                            levelCourse = course.levelCourse,
                            studentCount = studentCount
                        )
                    }
                } else {
                    emptyList()
                }
            }.collect { coursesList ->
                _courses.value = coursesList
            }
        }
    }

    fun loadTeacherCourses(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val teacher = authRepository.getTeacherByUserId(userId)
                if (teacher != null) {
                    _teacherId.value = userId
                    _teacherEntityId = teacher.id
                    // Immediately refresh courses
                    refreshCourses()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun refreshCourses() {
        val allCourses = scrudRepository.getAllCourses().first()
        val allSubscriptions = scrudRepository.getAllSubscribes().first()
        
        if (_teacherEntityId != null) {
            val teacherCourses = allCourses.filter { it.teacherId == _teacherEntityId }
            
            val coursesList = teacherCourses.map { course ->
                val studentCount = allSubscriptions.count { it.courseId == course.idCourse }
                TeacherCourseInfo(
                    idCourse = course.idCourse,
                    nameCourse = course.nameCourse,
                    ectsCourse = course.ectsCourse,
                    levelCourse = course.levelCourse,
                    studentCount = studentCount
                )
            }
            
            _courses.value = coursesList
        }
    }
}
