package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_users")
data class StudentUserEntity(
    @PrimaryKey val id: Int,
    val userId: Int, // Foreign key to UserEntity
    val studentId: Int, // Foreign key to StudentEntity
    val levelOfStudy: LevelCourse
)
