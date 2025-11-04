package com.tumme.scrudstudents.data.repository

import com.tumme.scrudstudents.data.local.dao.CourseDao
import com.tumme.scrudstudents.data.local.dao.StudentDao
import com.tumme.scrudstudents.data.local.dao.SubscribeDao
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository class.
 * It acts as a coordinator between data sources and the rest of the application.
 * It abstracts the data source, so the ViewModel doesn't need to know whether the data comes from a network or a local database.
 *
 * @param studentDao An instance of StudentDao passed via dependency injection.
 * @param courseDao An instance of CourseDao passed via dependency injection.
 * @param subscribeDao An instance of SubscribeDao passed via dependency injection.
 */
class SCRUDRepository(
    private val studentDao: StudentDao,
    private val courseDao: CourseDao,
    private val subscribeDao: SubscribeDao
) {
    // Students
    //    Gets the stream of all students from StudentDao.
    fun getAllStudents(): Flow<List<StudentEntity>> = studentDao.getAllStudents()
    //    Inserts a new student into the database.
    suspend fun insertStudent(student: StudentEntity) = studentDao.insert(student)
    //    Deletes a student from the database.
    suspend fun deleteStudent(student: StudentEntity) = studentDao.delete(student)
    //    Gets a single student by their ID.
    suspend fun getStudentById(id: Int) = studentDao.getStudentById(id)

    // Courses
    //    Gets the stream of all courses from CourseDao.
    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()
    //    Inserts a new course into the database.
    suspend fun insertCourse(course: CourseEntity) = courseDao.insert(course)
    //    Deletes a course from the database.
    suspend fun deleteCourse(course: CourseEntity) = courseDao.delete(course)
    //    Gets a single course by their ID.
    suspend fun getCourseById(id: Int) = courseDao.getCourseById(id)
    //    Updates an existing course in the database.
    suspend fun updateCourse(course: CourseEntity) = courseDao.update(course)
    // Subscribes
    //    Gets the stream of all subscribes from SubscribeDao.
    fun getAllSubscribes(): Flow<List<SubscribeEntity>> = subscribeDao.getAllSubscribes()
    //    Gets the stream of subscribes by student ID from SubscribeDao.
    fun getSubscribesByStudent(sId: Int): Flow<List<SubscribeEntity>> = subscribeDao.getSubscribesByStudent(sId)
    //    Gets the stream of subscribes by course ID from SubscribeDao.
    fun getSubscribesByCourse(cId: Int): Flow<List<SubscribeEntity>> = subscribeDao.getSubscribesByCourse(cId)
    //    Inserts a new subscribe into the database.
    suspend fun insertSubscribe(subscribe: SubscribeEntity) = subscribeDao.insert(subscribe)
    //    Deletes a subscribe from the database.
    suspend fun deleteSubscribe(subscribe: SubscribeEntity) = subscribeDao.delete(subscribe)
    /**
     * Updates an existing subscription in the database.
     */
    suspend fun updateSubscribe(subscribe: SubscribeEntity) = subscribeDao.update(subscribe)
    /**
     * Finds a specific subscription by its student and course IDs.
     */
    suspend fun findSubscription(sId: Int, cId: Int) = subscribeDao.findSubscriptionByIds(sId, cId)
}

