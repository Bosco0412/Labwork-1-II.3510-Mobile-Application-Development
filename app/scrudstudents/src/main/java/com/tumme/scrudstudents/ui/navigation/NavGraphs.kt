package com.tumme.scrudstudents.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tumme.scrudstudents.ui.course.CourseDetailScreen
import com.tumme.scrudstudents.ui.course.CourseFormScreen
import com.tumme.scrudstudents.ui.course.CourseListScreen
import com.tumme.scrudstudents.ui.student.StudentDetailScreen
import com.tumme.scrudstudents.ui.student.StudentFormScreen
import com.tumme.scrudstudents.ui.student.StudentListScreen
import com.tumme.scrudstudents.ui.subscribe.SubscribeDetailScreen
import com.tumme.scrudstudents.ui.subscribe.SubscribeFormScreen
import com.tumme.scrudstudents.ui.subscribe.SubscribeListScreen
import com.tumme.scrudstudents.ui.auth.AuthenticationScreen
import com.tumme.scrudstudents.ui.student.StudentDashboard
import com.tumme.scrudstudents.ui.student.StudentCourseScreen
import com.tumme.scrudstudents.ui.student.StudentGradesScreen
import com.tumme.scrudstudents.ui.teacher.TeacherDashboard
import com.tumme.scrudstudents.ui.teacher.GradeManagementScreen
import com.tumme.scrudstudents.ui.teacher.TeacherCourseListScreen
import com.tumme.scrudstudents.ui.teacher.TeacherStudentListScreen
import com.tumme.scrudstudents.ui.SplashScreen
import com.tumme.scrudstudents.data.local.model.UserRole

/**
 * An object to hold all the navigation routes as constants.
 */
object Routes {
    // Splash Screen
    const val SPLASH = "splash"
    
    // Authentication Routes
    const val AUTH = "auth"
    
    // Student Routes
    const val STUDENT_DASHBOARD = "student_dashboard"
    const val STUDENT_COURSES = "student_courses"
    const val STUDENT_GRADES = "student_grades"
    const val STUDENT_LIST = "student_list"
    const val STUDENT_FORM = "student_form"
    const val STUDENT_DETAIL = "student_detail/{studentId}"

    // Teacher Routes
    const val TEACHER_DASHBOARD = "teacher_dashboard"
    const val TEACHER_COURSES = "teacher_courses"
    const val TEACHER_STUDENTS = "teacher_students"
    const val GRADE_MANAGEMENT = "grade_management"

    // Course Routes
    const val COURSE_LIST = "course_list"
    const val COURSE_FORM = "course_form"
    const val COURSE_EDIT = "course_form/{courseId}"
    const val COURSE_DETAIL = "course_detail/{courseId}"

    // Subscribe Routes
    const val SUBSCRIBE_LIST = "subscribe_list"
    const val SUBSCRIBE_FORM = "subscribe_form"
    const val SUBSCRIBE_EDIT = "subscribe_form/{studentId}/{courseId}"
    const val SUBSCRIBE_DETAIL = "subscribe_detail/{studentId}/{courseId}"
}

/**
 * The main navigation host for the application.
 */
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.SPLASH) {
        // Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onDataLoaded = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Authentication
        composable(Routes.AUTH) {
            AuthenticationScreen(
                onLoginSuccess = { role ->
                    val target = when (role) {
                        UserRole.STUDENT -> Routes.STUDENT_DASHBOARD
                        UserRole.TEACHER -> Routes.TEACHER_DASHBOARD
                    }
                    navController.navigate(target) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onAutoLogin = { role ->
                    val target = when (role) {
                        UserRole.STUDENT -> Routes.STUDENT_DASHBOARD
                        UserRole.TEACHER -> Routes.TEACHER_DASHBOARD
                    }
                    navController.navigate(target) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // Student Routes
        composable(Routes.STUDENT_DASHBOARD) {
            StudentDashboard(
                onNavigateToCourses = { navController.navigate(Routes.STUDENT_COURSES) },
                onNavigateToGrades = { navController.navigate(Routes.STUDENT_GRADES) },
                onLogout = { 
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.STUDENT_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.STUDENT_COURSES) {
            StudentCourseScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.STUDENT_GRADES) {
            StudentGradesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Teacher Routes
        composable(Routes.TEACHER_DASHBOARD) {
            TeacherDashboard(
                onNavigateToCourses = { navController.navigate(Routes.TEACHER_COURSES) },
                onNavigateToStudents = { navController.navigate(Routes.TEACHER_STUDENTS) },
                onNavigateToGrades = { navController.navigate(Routes.GRADE_MANAGEMENT) },
                onLogout = { 
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.TEACHER_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.TEACHER_COURSES) {
            TeacherCourseListScreen(
                onBack = { navController.popBackStack() },
                onNavigateToForm = { navController.navigate(Routes.COURSE_FORM) },
                onNavigateToDetail = { id -> navController.navigate("course_detail/$id") },
                onNavigateToEdit = { id -> navController.navigate("course_form/$id") }
            )
        }
        
        composable(Routes.TEACHER_STUDENTS) {
            TeacherStudentListScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.GRADE_MANAGEMENT) {
            GradeManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Original Admin Routes (for backward compatibility)
        composable(Routes.STUDENT_LIST) {
            StudentListScreen(
                onNavigateToForm = { navController.navigate(Routes.STUDENT_FORM) },
                onNavigateToDetail = { id -> navController.navigate("student_detail/$id") }
            )
        }
        composable(Routes.STUDENT_FORM) {
            StudentFormScreen(onSaved = { navController.popBackStack() })
        }
        composable(
            route = Routes.STUDENT_DETAIL,
            arguments = listOf(navArgument("studentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("studentId") ?: 0
            StudentDetailScreen(studentId = id, onBack = { navController.popBackStack() })
        }

        // --- Course Screen Routes (No changes) ---
        composable(Routes.COURSE_LIST) {
            CourseListScreen(
                onNavigateToForm = { navController.navigate(Routes.COURSE_FORM) },
                onNavigateToDetail = { id -> navController.navigate("course_detail/$id") },
                onNavigateToEdit = { id -> navController.navigate("course_form/$id") }
            )
        }
        composable(Routes.COURSE_FORM) {
            CourseFormScreen(
                courseId = null,
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.COURSE_EDIT,
            arguments = listOf(navArgument("courseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("courseId") ?: 0
            CourseFormScreen(
                courseId = id,
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.COURSE_DETAIL,
            arguments = listOf(navArgument("courseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("courseId") ?: 0
            CourseDetailScreen(courseId = id, onBack = { navController.popBackStack() })
        }

        // --- Subscribe Screen Routes ---
        composable(Routes.SUBSCRIBE_LIST) {
            SubscribeListScreen(
                onNavigateToForm = { navController.navigate(Routes.SUBSCRIBE_FORM) },
                onNavigateToEdit = { studentId, courseId ->
                    navController.navigate("subscribe_form/$studentId/$courseId")
                },
                // 2. Connect onNavigateToDetail to the new route
                onNavigateToDetail = { studentId, courseId ->
                    navController.navigate("subscribe_detail/$studentId/$courseId")
                }
            )
        }
        composable(Routes.SUBSCRIBE_FORM) {
            SubscribeFormScreen(
                studentId = null,
                courseId = null,
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.SUBSCRIBE_EDIT,
            arguments = listOf(
                navArgument("studentId") { type = NavType.IntType },
                navArgument("courseId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val sId = backStackEntry.arguments?.getInt("studentId") ?: 0
            val cId = backStackEntry.arguments?.getInt("courseId") ?: 0
            SubscribeFormScreen(
                studentId = sId,
                courseId = cId,
                onSaved = { navController.popBackStack() }
            )
        }
        // 3. Add a Composable destination for the new detail page
        composable(
            route = Routes.SUBSCRIBE_DETAIL,
            arguments = listOf(
                navArgument("studentId") { type = NavType.IntType },
                navArgument("courseId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val sId = backStackEntry.arguments?.getInt("studentId") ?: 0
            val cId = backStackEntry.arguments?.getInt("courseId") ?: 0
            SubscribeDetailScreen(
                studentId = sId,
                courseId = cId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

