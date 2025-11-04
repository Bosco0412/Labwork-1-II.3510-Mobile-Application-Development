package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val idCourse: Int,
    val nameCourse: String,
    val ectsCourse: Float,
    val levelCourse: LevelCourse,
    val teacherId: Int = 0, // Foreign key to TeacherEntity
    val description: String = ""
)