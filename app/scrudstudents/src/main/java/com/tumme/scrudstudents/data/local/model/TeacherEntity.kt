package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val id: Int,
    val userId: Int, // Foreign key to UserEntity
    val department: String,
    val specialization: String
)
