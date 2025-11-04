package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
/**
 * Defines the data model for a Student and the table structure in the database.
 *
 * @Entity(tableName = "students") specifies the name of the table in the Room database.
 */
@Entity(tableName = "students")
data class StudentEntity(
    /**
     * The @PrimaryKey annotation marks this field as the primary key.
     * Each student entity will have a unique idStudent in the database.
     */
    @PrimaryKey val idStudent: Int,

    //    The last name of the student.
    val lastName: String,

    // The first name of the student.
    val firstName: String,
    //    The date of birth of the student.
    val dateOfBirth: Date,
    //    The gender of the student.
    val gender: Gender
)