package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val username: String,
    val password: String, // In a real app, this would be hashed
    val role: UserRole,
    val email: String,
    val firstName: String,
    val lastName: String
)

enum class UserRole {
    STUDENT, TEACHER
}
