package com.tumme.scrudstudents.data.local.dao

import androidx.room.*
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscribeDao {
    @Query("SELECT * FROM subscribes")
    fun getAllSubscribes(): Flow<List<SubscribeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscribe: SubscribeEntity)

    @Delete
    suspend fun delete(subscribe: SubscribeEntity)

    @Query("SELECT * FROM subscribes WHERE studentId = :sId")
    fun getSubscribesByStudent(sId: Int): Flow<List<SubscribeEntity>>

    @Query("SELECT * FROM subscribes WHERE courseId = :cId")
    fun getSubscribesByCourse(cId: Int): Flow<List<SubscribeEntity>>

    /**
     * Updates an existing subscription.
     */
    @Update
    suspend fun update(subscribe: SubscribeEntity)

    /**
     * Finds a specific subscription by its composite primary key (studentId and courseId).
     */
    @Query("SELECT * FROM subscribes WHERE studentId = :sId AND courseId = :cId LIMIT 1")
    suspend fun findSubscriptionByIds(sId: Int, cId: Int): SubscribeEntity?
}

