package com.tumme.scrudstudents.data.repository

import com.tumme.scrudstudents.data.local.dao.*
import com.tumme.scrudstudents.data.local.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataSeeder @Inject constructor(
    private val userDao: UserDao,
    private val teacherDao: TeacherDao,
    private val studentUserDao: StudentUserDao,
    private val studentDao: StudentDao,
    private val courseDao: CourseDao,
    private val subscribeDao: SubscribeDao
) {
    suspend fun seedData() = withContext(Dispatchers.IO) {
        try {
            // Check if data already exists
            val existingUsers = userDao.checkUsernameExists("student1")
            if (existingUsers > 0) return@withContext

            // Create sample users
            val student1 = UserEntity(
                id = 1,
                username = "student1",
                password = "password123",
                role = UserRole.STUDENT,
                email = "student1@university.edu",
                firstName = "Alice",
                lastName = "Johnson",
                photoUrl = null
            )

            val student2 = UserEntity(
                id = 2,
                username = "student2",
                password = "password123",
                role = UserRole.STUDENT,
                email = "student2@university.edu",
                firstName = "Bob",
                lastName = "Smith",
                photoUrl = null
            )

            val teacher1 = UserEntity(
                id = 3,
                username = "teacher1",
                password = "password123",
                role = UserRole.TEACHER,
                email = "teacher1@university.edu",
                firstName = "Dr. Sarah",
                lastName = "Wilson",
                photoUrl = null
            )

            val teacher2 = UserEntity(
                id = 4,
                username = "teacher2",
                password = "password123",
                role = UserRole.TEACHER,
                email = "teacher2@university.edu",
                firstName = "Prof. Michael",
                lastName = "Brown",
                photoUrl = null
            )

            userDao.insert(student1)
            userDao.insert(student2)
            userDao.insert(teacher1)
            userDao.insert(teacher2)

            // Create student user records
            val studentUser1 = StudentUserEntity(
                id = 1,
                userId = 1,
                studentId = 1001,
                levelOfStudy = LevelCourse.P1
            )

            val studentUser2 = StudentUserEntity(
                id = 2,
                userId = 2,
                studentId = 1002,
                levelOfStudy = LevelCourse.P2
            )

            studentUserDao.insert(studentUser1)
            studentUserDao.insert(studentUser2)

            // Create teacher records
            val teacherEntity1 = TeacherEntity(
                id = 3,
                userId = 3,
                department = "Computer Science",
                specialization = "Software Engineering"
            )

            val teacherEntity2 = TeacherEntity(
                id = 4,
                userId = 4,
                department = "Mathematics",
                specialization = "Applied Mathematics"
            )

            teacherDao.insert(teacherEntity1)
            teacherDao.insert(teacherEntity2)

            // Create student entities
            val studentEntity1 = StudentEntity(
                idStudent = 1001,
                lastName = "Johnson",
                firstName = "Alice",
                dateOfBirth = java.util.Date(),
                gender = Gender.Female
            )

            val studentEntity2 = StudentEntity(
                idStudent = 1002,
                lastName = "Smith",
                firstName = "Bob",
                dateOfBirth = java.util.Date(),
                gender = Gender.Male
            )

            studentDao.insert(studentEntity1)
            studentDao.insert(studentEntity2)

            // Create courses
            val course1 = CourseEntity(
                idCourse = 1,
                nameCourse = "Introduction to Programming",
                ectsCourse = 6.0f,
                levelCourse = LevelCourse.P1,
                teacherId = 3,
                description = "Learn the fundamentals of programming with Java and Kotlin."
            )

            val course2 = CourseEntity(
                idCourse = 2,
                nameCourse = "Data Structures and Algorithms",
                ectsCourse = 8.0f,
                levelCourse = LevelCourse.P2,
                teacherId = 3,
                description = "Advanced programming concepts and algorithm design."
            )

            val course3 = CourseEntity(
                idCourse = 3,
                nameCourse = "Calculus I",
                ectsCourse = 6.0f,
                levelCourse = LevelCourse.P1,
                teacherId = 4,
                description = "Introduction to differential and integral calculus."
            )

            val course4 = CourseEntity(
                idCourse = 4,
                nameCourse = "Linear Algebra",
                ectsCourse = 5.0f,
                levelCourse = LevelCourse.P2,
                teacherId = 4,
                description = "Vector spaces, linear transformations, and eigenvalues."
            )

            courseDao.insert(course1)
            courseDao.insert(course2)
            courseDao.insert(course3)
            courseDao.insert(course4)

            // Create some sample subscriptions with grades
            val subscription1 = SubscribeEntity(
                studentId = 1001,
                courseId = 1,
                score = 4.5f
            )

            val subscription2 = SubscribeEntity(
                studentId = 1001,
                courseId = 3,
                score = 3.8f
            )

            val subscription3 = SubscribeEntity(
                studentId = 1002,
                courseId = 2,
                score = 5.2f
            )

            val subscription4 = SubscribeEntity(
                studentId = 1002,
                courseId = 4,
                score = 4.0f
            )

            subscribeDao.insert(subscription1)
            subscribeDao.insert(subscription2)
            subscribeDao.insert(subscription3)
            subscribeDao.insert(subscription4)

        } catch (e: Exception) {
            // Handle seeding error
            e.printStackTrace()
        }
    }
}
