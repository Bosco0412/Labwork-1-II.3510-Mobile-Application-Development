package com.tumme.scrudstudents.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase // <-- 1. ADD IMPORT
import androidx.sqlite.db.SupportSQLiteDatabase // <-- 2. ADD IMPORT
import com.tumme.scrudstudents.data.local.AppDatabase
import com.tumme.scrudstudents.data.local.dao.CourseDao
import com.tumme.scrudstudents.data.local.dao.StudentDao
import com.tumme.scrudstudents.data.local.dao.SubscribeDao
import com.tumme.scrudstudents.data.local.dao.UserDao
import com.tumme.scrudstudents.data.local.dao.TeacherDao
import com.tumme.scrudstudents.data.local.dao.StudentUserDao
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.repository.SampleDataSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope // <-- 3. ADD IMPORT
import kotlinx.coroutines.Dispatchers // <-- 4. ADD IMPORT
import kotlinx.coroutines.launch // <-- 5. ADD IMPORT
import javax.inject.Provider // <-- 6. ADD IMPORT (VERY IMPORTANT)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        // 7. Use Provider<> to lazily inject the seeder
        // This breaks the circular dependency loop (DB -> Seeder -> DAOs -> DB)
        seederProvider: Provider<SampleDataSeeder>
    ): AppDatabase {

        // 8. Create a Room callback
        val dbCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // 9. Run the seeder in a background IO thread
                CoroutineScope(Dispatchers.IO).launch {
                    val seeder = seederProvider.get()
                    seeder.seedData() // This now runs in the background
                }
            }
        }

        // 10. Build the database and add the callback
        return Room.databaseBuilder(context, AppDatabase::class.java, "scrud-db")
            .fallbackToDestructiveMigration()
            .addCallback(dbCallback) // <-- 11. ATTACH THE CALLBACK
            .build()
    }

    // --- ALL OTHER PROVIDERS BELOW REMAIN EXACTLY THE SAME ---

    @Provides fun provideStudentDao(db: AppDatabase): StudentDao = db.studentDao()
    @Provides fun provideCourseDao(db: AppDatabase): CourseDao = db.courseDao()
    @Provides fun provideSubscribeDao(db: AppDatabase): SubscribeDao = db.subscribeDao()
    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideTeacherDao(db: AppDatabase): TeacherDao = db.teacherDao()
    @Provides fun provideStudentUserDao(db: AppDatabase): StudentUserDao = db.studentUserDao()

    @Provides
    @Singleton
    fun provideRepository(studentDao: StudentDao, courseDao: CourseDao,
                          subscribeDao: SubscribeDao): SCRUDRepository =
        SCRUDRepository(studentDao, courseDao, subscribeDao)

    @Provides
    @Singleton
    fun provideAuthRepository(
        userDao: UserDao,
        teacherDao: TeacherDao,
        studentUserDao: StudentUserDao,
        studentDao: StudentDao
    ): AuthRepository =
        AuthRepository(userDao, teacherDao, studentUserDao, studentDao)

    @Provides
    @Singleton
    fun provideSampleDataSeeder(
        userDao: UserDao,
        teacherDao: TeacherDao,
        studentUserDao: StudentUserDao,
        studentDao: StudentDao,
        courseDao: CourseDao,
        subscribeDao: SubscribeDao
    ): SampleDataSeeder =
        SampleDataSeeder(userDao, teacherDao, studentUserDao, studentDao, courseDao, subscribeDao)
}