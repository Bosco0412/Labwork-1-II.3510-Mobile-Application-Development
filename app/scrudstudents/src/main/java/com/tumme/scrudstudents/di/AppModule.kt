package com.tumme.scrudstudents.di

import android.content.Context
import androidx.room.Room
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
/**
 * Hilt Module for providing application-level dependencies.
 * This object contains instructions for Hilt on how to create instances of various classes
 * that are needed throughout the application, such as the database, DAOs, and the repository.
 *
 * @Module annotation marks this object as a Hilt module.
 * @InstallIn(SingletonComponent::class) specifies that the dependencies provided here will have an
 * application-wide scope, meaning they are created once and shared across the entire app.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /**
     * Provides a singleton instance of the Room database (AppDatabase).
     * @Provides annotation tells Hilt that this function provides a dependency.
     * @Singleton ensures that only one instance of the database is ever created.
     *
     * @param context The application context, automatically injected by Hilt using the @ApplicationContext qualifier.
     * @return The singleton AppDatabase instance.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "scrud-db")
            .fallbackToDestructiveMigration()
            .build()

    /**
     * Provides an instance of StudentDao.
     * Hilt knows how to provide the `db: AppDatabase` parameter because of the `provideDatabase` function above.
     */
    @Provides fun provideStudentDao(db: AppDatabase): StudentDao = db.studentDao()
    // Provides an instance of CourseDao.
    @Provides fun provideCourseDao(db: AppDatabase): CourseDao = db.courseDao()
    // Provides an instance of SubscribeDao.
    @Provides fun provideSubscribeDao(db: AppDatabase): SubscribeDao = db.subscribeDao()
    // Provides an instance of UserDao.
    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    // Provides an instance of TeacherDao.
    @Provides fun provideTeacherDao(db: AppDatabase): TeacherDao = db.teacherDao()
    // Provides an instance of StudentUserDao.
    @Provides fun provideStudentUserDao(db: AppDatabase): StudentUserDao = db.studentUserDao()

    /**
     * Provides a singleton instance of the SCRUDRepository.
     * This function demonstrates the power of dependency injection:
     * Hilt sees that this function needs StudentDao, CourseDao, and SubscribeDao.
     * It then automatically calls the provider functions above to get instances of those DAOs
     * and passes them as arguments to create the SCRUDRepository.
     *
     * @return The singleton SCRUDRepository instance.
     */
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