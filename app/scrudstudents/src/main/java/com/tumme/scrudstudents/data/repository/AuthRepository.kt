package com.tumme.scrudstudents.data.repository

import com.tumme.scrudstudents.data.local.dao.UserDao
import com.tumme.scrudstudents.data.local.dao.TeacherDao
import com.tumme.scrudstudents.data.local.dao.StudentUserDao
import com.tumme.scrudstudents.data.local.dao.StudentDao
import com.tumme.scrudstudents.data.local.model.UserEntity
import com.tumme.scrudstudents.data.local.model.TeacherEntity
import com.tumme.scrudstudents.data.local.model.StudentUserEntity
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.local.model.UserRole
import com.tumme.scrudstudents.data.local.model.LevelCourse
import com.tumme.scrudstudents.data.local.model.Gender // [COMPILE FIX 1] Import Gender
import java.util.Date // [COMPILE FIX 2] Import Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val teacherDao: TeacherDao,
    private val studentUserDao: StudentUserDao,
    private val studentDao: StudentDao
) {
    suspend fun authenticate(username: String, password: String): UserEntity? {
        return userDao.authenticate(username, password)
    }

    suspend fun registerUser(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String,
        role: UserRole,
        levelOfStudy: LevelCourse? = null,
        department: String? = null,
        specialization: String? = null
    ): Result<UserEntity> {
        return try {
            // Check if username already exists
            val existingUser = userDao.checkUsernameExists(username)
            if (existingUser > 0) {
                return Result.failure(Exception("Username already exists"))
            }

            // Generate new user ID (in a real app, this would be auto-generated)
            val userId = System.currentTimeMillis().toInt()
            val user = UserEntity(
                id = userId,
                username = username,
                password = password,
                role = role,
                email = email,
                firstName = firstName,
                lastName = lastName
            )

            userDao.insert(user)
            android.util.Log.d("AuthRepository", "Inserted UserEntity with id: $userId")

            // Create role-specific records
            when (role) {
                UserRole.STUDENT -> {
                    val studentId = userId + 1000 // Generate student ID
                    val studentUser = StudentUserEntity(
                        id = userId, // Use userId as primary key for this link table
                        userId = userId,
                        studentId = studentId,
                        levelOfStudy = levelOfStudy ?: LevelCourse.P1
                    )
                    studentUserDao.insert(studentUser)
                    android.util.Log.d("AuthRepository", "Inserted StudentUserEntity with userId: $userId, studentId: $studentId, level: ${studentUser.levelOfStudy}")

                    // [COMPILE FIX 3] CREATE THE STUDENT ENTITY USING THE *CORRECT* CONSTRUCTOR
                    // We must provide default values for fields not in registerUser
                    val student = StudentEntity(
                        idStudent = studentId, // Use the same generated studentId
                        lastName = lastName,
                        firstName = firstName,
                        dateOfBirth = Date(), // Default to current date
                        gender = Gender.NotConcerned // Assuming Gender.OTHER exists
                    )
                    studentDao.insert(student)
                    android.util.Log.d("AuthRepository", "Inserted StudentEntity with idStudent: $studentId")


                    // Verify insertion with small delay to ensure transaction is committed
                    kotlinx.coroutines.delay(100)
                    val inserted = studentUserDao.getStudentUserByUserId(userId)
                    if (inserted == null) {
                        android.util.Log.e("AuthRepository", "Verification failed: StudentUserEntity not found for userId: $userId")
                        throw Exception("Failed to create StudentUserEntity for userId: $userId")
                    } else {
                        android.util.Log.d("AuthRepository", "Verified StudentUserEntity exists for userId: $userId")
                    }
                }
                UserRole.TEACHER -> {
                    val teacher = TeacherEntity(
                        id = userId, // Use userId as primary key
                        userId = userId,
                        department = department ?: "General",
                        specialization = specialization ?: "General"
                    )
                    teacherDao.insert(teacher)
                    android.util.Log.d("AuthRepository", "Inserted TeacherEntity with userId: $userId")

                    // Verify insertion with small delay to ensure transaction is committed
                    kotlinx.coroutines.delay(100)
                    val inserted = teacherDao.getTeacherByUserId(userId)
                    if (inserted == null) {
                        android.util.Log.e("AuthRepository", "Verification failed: TeacherEntity not found for userId: $userId")
                        throw Exception("Failed to create TeacherEntity for userId: $userId")
                    } else {
                        android.util.Log.d("AuthRepository", "Verified TeacherEntity exists for userId: $userId")
                    }
                }
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTeacherByUserId(userId: Int): TeacherEntity? {
        return teacherDao.getTeacherByUserId(userId)
    }

    suspend fun getStudentUserByUserId(userId: Int): StudentUserEntity? {
        return studentUserDao.getStudentUserByUserId(userId)
    }

    suspend fun getUserById(userId: Int): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getTeacherById(teacherId: Int): TeacherEntity? {
        return teacherDao.getTeacherById(teacherId)
    }

    suspend fun getStudentUserByStudentId(studentId: Int): StudentUserEntity? {
        return studentUserDao.getStudentUserByStudentId(studentId)
    }
}

