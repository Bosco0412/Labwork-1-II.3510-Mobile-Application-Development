package com.tumme.scrudstudents.data.local.dao

import androidx.room.*
import com.tumme.scrudstudents.data.local.model.StudentEntity
import kotlinx.coroutines.flow.Flow
/**
 * Data Access Object (DAO).
 * This interface defines all database operations related to the "students" table.
 * Room will generate the implementation code for this interface at compile time.
 */
@Dao
interface StudentDao {
    /**
     * Queries and gets all students from the table, ordered by last and first name.
     * Flow<List<StudentEntity>>: A Flow of a list of StudentEntity. Flow is a stream of data that can emit multiple values asynchronously.
     * This means that whenever the student data in the database changes, this Flow will automatically emit the latest list of students,
     * allowing the UI to update reactively without manual refreshing.
     */
    @Query("SELECT * FROM students ORDER BY lastName, firstName")
    fun getAllStudents(): Flow<List<StudentEntity>>

    /**
     * Inserts a new student entity into the database.
     * `onConflict = OnConflictStrategy.REPLACE` means that if a student with the same primary key already exists, the old data will be replaced.
     * The "suspend" keyword indicates that this is a suspending function, which must be called from a coroutine to avoid blocking the main thread.
     * student: StudentEntity: The student entity to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity)

    /**
     * Deletes a student entity from the database.
     * This is a suspending function.
     * student: StudentEntity: The student entity to be deleted.
     */
    @Delete
    suspend fun delete(student: StudentEntity)

    /**
     * Queries and gets a single student by their ID.
     * This is a suspending function.
     * id: Int: The ID of the student to find.
     * : StudentEntity? : The found student entity, or null if not found.
     */
    @Query("SELECT * FROM students WHERE idStudent = :id LIMIT 1")
    suspend fun getStudentById(id: Int): StudentEntity?
}