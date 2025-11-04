# SCRUD Students - Course Management System

A comprehensive Android application built with Jetpack Compose that provides role-based access management for Students and Teachers in an educational institution.

## Features

### 🔐 Authentication System
- **User Registration**: Students and Teachers can register with role-specific information
- **Login System**: Secure authentication with role-based navigation
- **Role Management**: Distinct user types with different access levels

### 👨‍🎓 Student Features
- **Dashboard**: Personalized view with enrolled courses and final grade
- **Course Enrollment**: Browse and enroll in courses matching their study level
- **Grade Viewing**: View grades for enrolled courses
- **Final Grade Calculation**: ECTS-weighted final grade calculation for each level
- **Level-based Filtering**: Only see courses appropriate for their study level

### 👨‍🏫 Teacher Features
- **Dashboard**: Overview of assigned courses and student statistics
- **Course Management**: Declare and manage courses they teach
- **Grade Entry**: Enter and update student grades for their courses
- **Student Lists**: View enrolled students for each course
- **Course Assignment**: Manage course assignments and descriptions

### 🎨 UI/UX Features
- **Modern Design**: Material Design 3 with beautiful color schemes
- **Responsive Layout**: Handles screen rotation and different screen sizes
- **Intuitive Navigation**: Role-based navigation with clear user flows
- **Loading States**: Proper loading indicators and error handling
- **Card-based Layout**: Clean, organized information display

## Technical Architecture

### 🏗️ Architecture Pattern
- **MVVM (Model-View-ViewModel)**: Clean separation of concerns
- **Repository Pattern**: Centralized data access layer
- **Dependency Injection**: Hilt for dependency management

### 🗄️ Database
- **Room Database**: Local SQLite database with Room ORM
- **Entities**: User, Teacher, Student, Course, and Subscription entities
- **Relationships**: Proper foreign key relationships between entities
- **Type Converters**: Custom converters for Date and Enum types

### 🔄 Data Flow
- **Flow-based**: Reactive programming with Kotlin Flow
- **State Management**: MutableStateFlow for UI state
- **Repository Layer**: Centralized data operations

## Database Schema

### Core Entities
- **UserEntity**: Base user information (username, password, role, email)
- **TeacherEntity**: Teacher-specific information (department, specialization)
- **StudentUserEntity**: Student-specific information (level of study)
- **StudentEntity**: Student personal information (name, birth date, gender)
- **CourseEntity**: Course information (name, ECTS, level, teacher, description)
- **SubscribeEntity**: Student-course enrollment with grades

### Relationships
- User → Teacher (1:1)
- User → StudentUser (1:1)
- StudentUser → Student (1:1)
- Teacher → Course (1:many)
- Student → Subscribe (1:many)
- Course → Subscribe (1:many)

## Sample Data

The application includes sample data for testing:
- **2 Students**: Alice Johnson (P1), Bob Smith (P2)
- **2 Teachers**: Dr. Sarah Wilson (CS), Prof. Michael Brown (Math)
- **4 Courses**: Programming, Data Structures, Calculus, Linear Algebra
- **Sample Grades**: Pre-populated with realistic grade data

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Kotlin 1.8+
- Android API 24+

### Installation
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run the application

### Sample Login Credentials
- **Student**: username: `student1`, password: `password123`
- **Teacher**: username: `teacher1`, password: `password123`

## User Roles and Permissions

### Student Role
- View personal dashboard with enrolled courses
- Browse available courses for their level
- Enroll/unenroll in courses
- View grades for enrolled courses
- See final grade calculation based on ECTS

### Teacher Role
- View teaching dashboard with course statistics
- Manage courses they teach
- Enter and update student grades
- View student lists for each course
- Access comprehensive course management tools

## Grade Calculation System

The final grade is calculated using ECTS-weighted average:
```
Final Grade = Σ(Grade × ECTS) / Σ(ECTS)
```

This ensures that courses with more credits have proportionally more impact on the final grade.

## Screen Rotation Support

The application properly handles screen rotation by:
- Using `rememberSaveable` for critical state
- Proper configuration changes handling
- Responsive layouts that adapt to different orientations
- State preservation across configuration changes

## Design System

### Color Scheme
- **Primary**: Blue tones for main actions
- **Secondary**: Green tones for success states
- **Tertiary**: Orange tones for highlights
- **Error**: Red tones for warnings and errors
- **Surface**: Neutral tones for backgrounds

### Typography
- **Headlines**: Bold, large text for titles
- **Body**: Regular text for content
- **Captions**: Small text for metadata
- **Labels**: Medium weight for form labels

### Spacing
- **8dp**: Small spacing
- **16dp**: Medium spacing
- **24dp**: Large spacing
- **32dp**: Extra large spacing

## Future Enhancements

- [ ] Real authentication with JWT tokens
- [ ] Push notifications for grade updates
- [ ] Offline synchronization
- [ ] Advanced reporting and analytics
- [ ] File upload for assignments
- [ ] Real-time chat between students and teachers
- [ ] Calendar integration for course schedules

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or support, please contact the development team.

---

**Note**: This is a demo application for educational purposes. In a production environment, additional security measures, proper authentication, and data validation would be required.
