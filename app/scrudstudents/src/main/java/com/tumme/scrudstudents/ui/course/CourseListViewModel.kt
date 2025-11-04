package com.tumme.scrudstudents.ui.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import com.tumme.scrudstudents.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * It is responsible for providing data to the UI and handling user interaction logic.
 * The ViewModel does not hold a direct reference to the UI, allowing it to survive configuration changes like screen rotations.
 *
 * @HiltViewModel annotation indicates that Hilt can provide dependencies for this ViewModel.
 */
@HiltViewModel
class CourseListViewModel @Inject constructor(
    /**
     * The SCRUDRepository is injected via the constructor.
     * Hilt will automatically find and provide an instance of SCRUDRepository.
     */
    private val repo: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * [Core of the Data Flow - Part 1: Creating StateFlow in ViewModel]
     *
     * 1. `repo.getAllCourses()` gets a Flow of the courses list from the repository.
     * 2. `.stateIn(...)` is an operator that converts a regular Flow into a StateFlow.
     * - A StateFlow is a special hot data stream that always has a value.
     * - When a UI component collects from it, it immediately receives the most recent value.
     *
     * @param viewModelScope Specifies that the lifecycle of this StateFlow is tied to the ViewModel.
     * @param SharingStarted.Lazily Indicates that data collection from the upstream Flow (repo.getAllCourses()) starts only when the first UI observer (collector) appears.
     * @param emptyList() The initial value; the list is empty before data is loaded from the database.
     *
     * `_courses` is a private, mutable StateFlow.
     */
    private val _courses: StateFlow<List<CourseEntity>> =
        repo.getAllCourses().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    // The UI will observe this `courses` state to get data.
    val courses: StateFlow<List<CourseEntity>> = _courses

    // UI event / error Flow
    //    SharedFlow is used because it can broadcast events to multiple collectors, and each event is consumed only once.
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    /**
     * Function to delete a course. This is triggered by a UI event.
     * @param course The course entity to be deleted.
     */
    fun deleteCourse(course: CourseEntity) = viewModelScope.launch {
        // `viewModelScope.launch` starts a coroutine whose lifecycle is tied to the ViewModel.

        repo.deleteCourse(course)
        _events.emit("Course deleted")
    }

    /**
     * Function to insert a course.
     * @param course The course entity to be inserted.
     * @param userId Optional user ID to set teacherId for the course
     */
    fun insertCourse(course: CourseEntity, userId: Int? = null) = viewModelScope.launch {
        android.util.Log.d("CourseListViewModel", "insertCourse called with userId: $userId")
        val courseToInsert = if (userId != null) {
            // Get teacher entity to set teacherId
            val teacher = authRepository.getTeacherByUserId(userId)
            android.util.Log.d("CourseListViewModel", "getTeacherByUserId($userId) returned: ${teacher?.id}, department: ${teacher?.department}")
            if (teacher != null) {
                val updatedCourse = course.copy(teacherId = teacher.id)
                android.util.Log.d("CourseListViewModel", "Setting course.teacherId to: ${teacher.id}")
                updatedCourse
            } else {
                android.util.Log.e("CourseListViewModel", "Teacher not found for userId: $userId, course will be saved without teacherId")
                course
            }
        } else {
            android.util.Log.w("CourseListViewModel", "userId is null, course will be saved without teacherId")
            course
        }
        repo.insertCourse(courseToInsert)
        android.util.Log.d("CourseListViewModel", "Course inserted with teacherId: ${courseToInsert.teacherId}")
        _events.emit("Course inserted")
    }

    /**
     * Function to update an existing course. This is triggered by a UI event from the form screen.
     * @param course The course entity with updated information.
     */
    fun updateCourse(course: CourseEntity) = viewModelScope.launch {
        repo.updateCourse(course)
        _events.emit("Course updated")
    }

    /**
     * Function to find a course by its ID.
     * @param idCourse The ID of the course to find.
     */
    suspend fun findCourse(idCourse: Int): CourseEntity? = repo.getCourseById(idCourse)

}

