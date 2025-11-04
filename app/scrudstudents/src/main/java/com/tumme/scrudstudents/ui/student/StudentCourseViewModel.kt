package com.tumme.scrudstudents.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.local.model.* // 导入所有必要的 Model 类
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // 导入 Flow 相关的类，包括 combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class StudentCourseViewModel @Inject constructor(
    private val scrudRepository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // --- State Flows for UI ---
    private val _availableCourses = MutableStateFlow<List<AvailableCourseInfo>>(emptyList())
    val availableCourses: StateFlow<List<AvailableCourseInfo>> = _availableCourses.asStateFlow()

    private val _enrolledCourses = MutableStateFlow<List<EnrolledCourseInfo>>(emptyList())
    val enrolledCourses: StateFlow<List<EnrolledCourseInfo>> = _enrolledCourses.asStateFlow()

    private val _studentLevel = MutableStateFlow<LevelCourse?>(null)
    val studentLevel: StateFlow<LevelCourse?> = _studentLevel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- Private State Flows for Flow Combination Logic ---
    private val _currentStudentId = MutableStateFlow<Int?>(null)
    private val _currentLevelFilter = MutableStateFlow<LevelCourse?>(null)

    init {
        // 【核心优化点】使用 Flow.combine 持续监听所有必要的底层数据
        // 只要任一底层数据 (所有课程或所有选课) 发生变化，都会触发 UI 列表的重新生成。
        viewModelScope.launch {
            combine(
                scrudRepository.getAllCourses(),      // Room Flow
                scrudRepository.getAllSubscribes(),   // Room Flow
                _currentLevelFilter,                  // ViewModel State Flow
                _currentStudentId                     // ViewModel State Flow
            ) { allCourses, allSubscriptions, level, studentId ->
                // 仅当学生信息加载完毕 (Level 和 ID 存在) 时才处理数据
                if (level == null || studentId == null) {
                    return@combine // 不处理，等待 loadInitialCourses 运行
                }
                _isLoading.value = true
                processCourseData(allCourses, allSubscriptions, level, studentId)
                _isLoading.value = false
            }.collect {
                // Flow 组合的结果会触发 processCourseData 内部的 StateFlow 更新，因此这里不需要做任何事情
                Log.d("StudentCourseVM", "Course data flow collected and processed.")
            }
        }
    }

    /**
     * Public function called by the UI to trigger the initial course load.
     * It loads the student's level and ID, which then triggers the 'init' Flow combination.
     */
    fun loadInitialCourses(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Find the StudentUser to get the studentId and level
                val studentUser = authRepository.getStudentUserByUserId(userId)
                Log.d("StudentCourseVM", "getStudentUserByUserId($userId) returned: studentId=${studentUser?.studentId}, level=${studentUser?.levelOfStudy}, userId=${studentUser?.userId}")

                if (studentUser != null) {
                    // 设置 Level 和 ID，这将触发 init 块中的 combine 逻辑
                    _studentLevel.value = studentUser.levelOfStudy // UI 显示
                    _currentLevelFilter.value = studentUser.levelOfStudy // 过滤 Flow
                    _currentStudentId.value = studentUser.studentId // 过滤 Flow
                } else {
                    // Fallback logic if StudentUserEntity is missing
                    Log.e("StudentCourseVM", "StudentUser not found for userId: $userId. Using default level.")
                    // 默认 Level 和 ID -1，会触发 processCourseData 中的默认逻辑
                    _studentLevel.value = LevelCourse.P1
                    _currentLevelFilter.value = LevelCourse.P1
                    _currentStudentId.value = -1 // -1 is a sentinel value for "invalid student"
                }
            } catch (e: Exception) {
                Log.e("StudentCourseVM", "Error loading student info: ${e.message}", e)
                _studentLevel.value = LevelCourse.P1
                _currentLevelFilter.value = LevelCourse.P1
                _currentStudentId.value = -1
            } finally {
                // 即使加载失败，也要设置 loading 为 false，但由于 combine 也会设置，这里可以省略，保持 clean code
                // _isLoading.value = false
            }
        }
    }

    /**
     * Private function that does the heavy lifting of sorting courses
     * into 'available' and 'enrolled' lists based on studentId.
     * This function is run automatically whenever underlying Flow data changes.
     */
    private suspend fun processCourseData(
        allCourses: List<CourseEntity>,
        allSubscriptions: List<SubscribeEntity>,
        level: LevelCourse,
        studentId: Int
    ) {
        if (studentId == -1) {
            Log.w("StudentCourseVM", "Invalid studentId (-1), loading only available courses for level $level")
            _enrolledCourses.value = emptyList()
            val levelCourses = allCourses.filter { it.levelCourse == level }
            // Logic to map and set _availableCourses...
            _availableCourses.value = mapCoursesToAvailableInfo(levelCourses)
            return
        }

        try {
            // Find subscriptions for this specific student
            // 使用 studentId (即 studentUser.studentId) 作为数据库中的外键进行过滤
            val studentSubscriptions = allSubscriptions.filter { it.studentId == studentId }
            val enrolledCourseIds = studentSubscriptions.map { it.courseId }.toSet()

            // Filter courses for the student's level
            val levelCourses = allCourses.filter { it.levelCourse == level }

            // 1. Available Courses (Level courses NOT in enrolled IDs)
            val availableCoursesList = levelCourses
                .filter { !enrolledCourseIds.contains(it.idCourse) }
                .let { courses -> mapCoursesToAvailableInfo(courses) }

            // 2. Enrolled Courses (Level courses that ARE in enrolled IDs)
            val enrolledCoursesList = levelCourses
                .filter { enrolledCourseIds.contains(it.idCourse) }
                .let { courses -> mapCoursesToEnrolledInfo(courses, studentSubscriptions) }

            // Update the StateFlows - This refreshes the UI
            _availableCourses.value = availableCoursesList
            _enrolledCourses.value = enrolledCoursesList

        } catch (e: Exception) {
            Log.e("StudentCourseVM", "Error in processCourseData", e)
            _availableCourses.value = emptyList()
            _enrolledCourses.value = emptyList()
        }
    }

    // Helper function to map CourseEntity to AvailableCourseInfo
    private suspend fun mapCoursesToAvailableInfo(courses: List<CourseEntity>): List<AvailableCourseInfo> {
        return courses.map { course ->
            val teacherName = getTeacherName(course.teacherId)
            AvailableCourseInfo(
                courseId = course.idCourse,
                nameCourse = course.nameCourse,
                ectsCourse = course.ectsCourse,
                levelCourse = course.levelCourse,
                teacherName = teacherName,
                description = course.description
            )
        }
    }

    // Helper function to map CourseEntity to EnrolledCourseInfo
    private suspend fun mapCoursesToEnrolledInfo(courses: List<CourseEntity>, subscriptions: List<SubscribeEntity>): List<EnrolledCourseInfo> {
        return courses.map { course ->
            val teacherName = getTeacherName(course.teacherId)
            val subscription = subscriptions.find { it.courseId == course.idCourse }
            EnrolledCourseInfo(
                courseId = course.idCourse,
                nameCourse = course.nameCourse,
                ectsCourse = course.ectsCourse,
                teacherName = teacherName,
                grade = subscription?.score ?: 0f
            )
        }
    }

    // Helper function to get teacher name
    private suspend fun getTeacherName(teacherId: Int): String {
        return if (teacherId > 0) {
            val teacher = authRepository.getTeacherById(teacherId)
            val teacherUser = teacher?.userId?.let { authRepository.getUserById(it) }
            teacherUser?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Teacher"
        } else "Unknown Teacher"
    }

    /**
     * Enrolls the student in a course.
     * Manual refresh logic (delay and loadStudentCourses) is REMOVED.
     */
    fun enrollInCourse(courseId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val studentUser = authRepository.getStudentUserByUserId(userId)
                if (studentUser != null) {
                    val subscription = SubscribeEntity(
                        // ✅ 外键修复：使用 studentId
                        studentId = studentUser.studentId,
                        courseId = courseId,
                        score = 0f // Initial grade
                    )
                    scrudRepository.insertSubscribe(subscription)
                    // 成功后，Room Flow 会自动触发 init 块中的 combine 逻辑，刷新 UI。

                } else {
                    Log.e("StudentCourseVM", "Cannot enroll, studentUser not found for userId: $userId")
                }
            } catch (e: Exception) {
                Log.e("StudentCourseVM", "Error enrolling in course: ${e.message}", e)
            }
        }
    }

    /**
     * Unenrolls the student from a course.
     * Manual refresh logic (delay and loadStudentCourses) is REMOVED.
     */
    fun unenrollFromCourse(courseId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val studentUser = authRepository.getStudentUserByUserId(userId)
                if (studentUser != null) {
                    // Find the subscription first
                    // ✅ 外键修复：使用 studentId
                    val subscription = scrudRepository.findSubscription(studentUser.studentId, courseId)
                    subscription?.let {
                        // Delete it if found
                        scrudRepository.deleteSubscribe(it)
                    }
                } else {
                    Log.e("StudentCourseVM", "Cannot unenroll, studentUser not found for userId: $userId")
                }
            } catch (e: Exception) {
                Log.e("StudentCourseVM", "Error unenrolling from course: ${e.message}", e)
            }
        }
    }
}
