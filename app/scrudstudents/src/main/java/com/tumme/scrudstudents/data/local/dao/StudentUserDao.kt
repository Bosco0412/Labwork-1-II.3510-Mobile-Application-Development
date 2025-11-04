package com.tumme.scrudstudents.data.local.dao

import androidx.room.*
import com.tumme.scrudstudents.data.local.model.StudentUserEntity
import com.tumme.scrudstudents.data.local.model.LevelCourse
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentUserDao {
    @Query("SELECT * FROM student_users WHERE userId = :userId LIMIT 1")
    suspend fun getStudentUserByUserId(userId: Int): StudentUserEntity?

    @Query("SELECT * FROM student_users WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentUserByStudentId(studentId: Int): StudentUserEntity?

    @Query("SELECT * FROM student_users WHERE levelOfStudy = :level")
    fun getStudentsByLevel(level: LevelCourse): Flow<List<StudentUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(studentUser: StudentUserEntity)

    @Delete
    suspend fun delete(studentUser: StudentUserEntity)

    @Update
    suspend fun update(studentUser: StudentUserEntity)
}
