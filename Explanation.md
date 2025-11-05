# Explanation

This document outlines the final architecture, data flow, and technology stack choices for the Lab work Android application.

The main path: `\app\scrudstudents\src\main\java\com\tumme\scrudstudents`

I have achieved one of the **Bonus**: Add a photo for each student. Now the students can upload their photos on the dashboard. 

## 1. Final Architecture

This project adopts modern Android architecture: **MVVM (Model-View-ViewModel)**, combined with **reactive programming** and **Dependency Injection (Hilt)**. This creates a maintainable, scalable, and robust "Offline-First" application.

The architecture is divided into the following layers:

* **UI Layer (View):**
    * The UI is built entirely with **Jetpack Compose** (e.g., `StudentCourseScreen.kt`, `TeacherCourseScreen.kt`).
    * The application uses a **Single-Activity Architecture** (`MainActivity`), with all screens implemented as Composable functions.
    * The UI **observes** a `StateFlow` from the ViewModel using `viewModel.state.collectAsState()`. When the data changes, the UI automatically recomposes to display the latest state.
    * The UI triggers business logic by calling functions on the ViewModel (e.g., `viewModel.deleteStudent()`).
    * The UI uses `rememberLauncherForActivityResult` (in `StudentDashboard.kt`) to interact with system components, such as the photo gallery, to provide rich features (e.g., selecting a profile photo).
    
* **ViewModel Layer:**
    * (e.g., `StudentCoureseViewModel.kt`, `TeacherCourseScreen.kt`) Manages the UI state and business logic for a specific screen.
    * It **holds the UI state** (using `StateFlow`) and exposes it to the UI Layer.
    * It uses `viewModelScope` (Coroutines) to call `suspend` functions or collect `Flow`s from the Repository.
    * It has **no knowledge** of the View; it only exposes state.

* **Repository Layer:**
    * (e.g., `SCRUDRepository.kt`) Acts as the **Single Source of Truth (SSOT)**. Here I combine  StudentRepository, TeacherRepository, CourseRepository together.
    * It abstracts the complexity of the data sources. In this project, it encapsulates calls to the **Room** database DAOs (Data Access Objects).
    * It provides a clean API for the ViewModels (e.g., `getAllStudents(): Flow` or `deleteStudent(student): suspend fun`).

* **Data Layer:**
    * **Room Database**: Serves as the **local persistence** solution. Room is the **core** of this architecture because it not only stores data but also provides **Observable Queries** via `Flow`.
    * **DAOs (Data Access Objects):** Such as `StudentDao`, `CourseDao`, `SubscribeDao`. They are the interfaces between the Repository and the Room database.

* **Dependency Injection (DI):**
    * **Hilt** is used to automatically manage dependencies.
    * Hilt is responsible for creating and injecting the `Repository` into the `ViewModel`, and injecting the `DAOs` into the `Repository`.

```

├── MainActivity.kt                # Application Entry Point (Single Activity)
│
├── data/                            # Data Layer
│   ├── local/                       # Local Data Source (Room)
│   │   ├── dao/                     # Data Access Objects (DAOs)
│   │   │   ├── StudentDao.kt
│   │   │   ├── CourseDao.kt
│   │   │   ├── StudentUserDao.kt
│   │   │   ├── CourseDao.kt
│   │   │   ├── SubscribeDao.kt
│   │   │   ├── TeacherDao.kt
│   │   │   └── UserDao.kt
│   │   └── model/                   # Database Entities
│   │       ├── StudentEntity.kt
│   │       ├── StudentUserEntity.kt
│   │       ├── TeacherEntity.kt
│   │       ├── UserEntity.kt
│   │       ├── CourseEntity.kt
│   │       ├── SubscribeEntity.kt
│   │       ├── Gender.kt            # Enum for Gender
│   │       └── LevelCourse.kt       # Enum for Course Level
│   │
│   └── repository/                  # Repositories
│       ├── SCRUDRepository.kt       # Main repository for CRUD operations
│       └── AuthRepository.kt        # Repository for authentication
│
├── ui/                              # UI Layer (View Layer - Jetpack Compose)
│   ├── auth/                        # Authentication Module
│   │   ├── AuthenticationScreen.kt  # Main auth screen (likely holds Login/Register)
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   └── AuthViewModel.kt         # Shared ViewModel for authentication state
│   │
│   ├── student/                     # Student Role Module
│   │   ├── StudentDashboard.kt
│   │   ├── StudentDashboardViewModel.kt
│   │   ├── StudentCourseScreen.kt
│   │   ├── StudentCourseViewModel.kt
│   │   ├── StudentGradesScreen.kt
│   │   ├── StudentGradesViewModel.kt
│   │   ├── StudentListScreen.kt     # (Legacy/Admin) Student list
│   │   └── StudentListViewModel.kt  # (Legacy/Admin) List VM
│   │
│   ├── teacher/                     # Teacher Role Module
│   │   ├── TeacherDashboard.kt
│   │   ├── TeacherDashboardViewModel.kt
│   │   ├── GradeManagementScreen.kt
│   │   ├── GradeManagementViewModel.kt
│   │   ├── TeacherCourseListScreen.kt
│   │   ├── TeacherCourseListViewModel.kt
│   │   ├── TeacherStudentListScreen.kt
│   │   └── TeacherStudentListViewModel.kt
│   │
│   ├── course/                      # (Legacy/Admin) Course Management
│   │   ├── CourseListScreen.kt
│   │   ├── CourseListViewModel.kt
│   │   ├── CourseFormScreen.kt
│   │   └── CourseDetailScreen.kt
│   │
│   ├── subscribe/                   # (Legacy/Admin) Subscription Management
│   │   ├── SubscribeListScreen.kt
│   │   ├── SubscribeListViewModel.kt
│   │   ├── SubscribeFormScreen.kt
│   │   └── SubscribeDetailScreen.kt
│   │
│   ├── navigation/                  # Navigation
│   │   └── NavGraphs.kt             # Navigation Graph (Replaces AppNavHost.kt)
│   │
│   └── SplashScreen.kt              # Splash Screen
│
└── di/                              # Dependency Injection (Hilt)
    └── AppModule.kt                 # Hilt module for providing Repositories and DAOs
```



## 2. Final Data Flow

The data flow in this project is **reactive**, centered around the Room database.

### 2.1 Data Loading and Display (Reactive Data Flow)

Example: Displaying the student list (`StudentListScreen`):

1.  **UI (Screen):** When `StudentListScreen` is created, it gets its `StudentListViewModel` instance via `hiltViewModel()`.
2.  **UI (Observe):** The UI calls `viewModel.students.collectAsState()` to subscribe to a `StateFlow`.
3.  **ViewModel:** The `StudentListViewModel` (during its initialization) calls `repo.getAllStudents()`.
4.  **Repository:** The `SCRUDRepository` calls `studentDao.getAllStudents()`.
5.  **Data (Room):** The `StudentDao` returns a **`Flow<List<StudentEntity>>`**. This is a "cold" `Flow` that will **automatically** emit a new list whenever the `StudentEntity` table changes.
6.  **ViewModel (Transform):** The `ViewModel` uses `.stateIn(viewModelScope, ...)` to convert this "cold" `Flow` into a "hot" `StateFlow` for the UI to subscribe to.
7.  **UI (Display):** The UI receives the initial list and displays it.

### 2.2 Data Submission and Auto-Update (Event Flow)

Example: Deleting a student:

1.  **UI (Event):** The user clicks the "delete" button. The `onDelete` callback in `StudentRow` is triggered, which calls `viewModel.deleteStudent(student)`.
2.  **ViewModel (Logic):** The `ViewModel` launches a coroutine in `viewModelScope` and calls `repo.deleteStudent(student)`.
3.  **Repository (Suspend):** The `Repository` calls `studentDao.delete(student)` (which is a `suspend` function).
4.  **Data (Room):** The Room database performs the delete operation, modifying the `StudentEntity` table.
5.  **Automatic Reaction (Reactive Loop):**
    * Because the database table was modified, the `Flow` created in **Step 2.1.5** **automatically emits** a new list that no longer contains the deleted student.
    * This new list flows through the `Repository` to the `ViewModel`'s `StateFlow`.
    * The UI's `collectAsState()` receives this new state and automatically triggers a recomposition.
    * The `LazyColumn` updates, and the student disappears from the list.

> **Key Advantage:** The UI and ViewModel **do not** need to manually refresh the list (e.g., call `loadStudents()`) after the delete operation. The data flow is automatic and reactive, which greatly reduces state management complexity.

### 2.3 Data Submission & Manual/Automatic State Update (Hybrid Flow)



**Example: Updating the User's Profile Photo in `StudentDashboard`:**

This flow is more complex than a delete operation because it involves interacting with a system Activity (the gallery), file I/O, and a manual update of the ViewModel's state to provide immediate UI feedback.

1. **UI (Event):** The user clicks the `AsyncImage` component in `StudentDashboard.kt`, which is wrapped in a `clickable` modifier.
2. **UI (Action):** The `onClick` callback triggers `galleryLauncher.launch("image/*")`, which starts the system's photo picker.
3. **UI (Result):** The user selects a photo. The `onResult` callback from `rememberLauncherForActivityResult` is triggered, returning a **temporary `Uri`**.
4. **UI (File Logic):** A private helper function, `saveImageToInternalStorage`, is called. It:
   - Uses `context.contentResolver` to read the image data from the temporary `Uri`.
   - Creates a new file (e.g., `user_1_profile.jpg`) inside the app's **private internal storage** (`context.filesDir`).
   - Copies the image data into this new file.
   - Returns the **permanent local file path** for this new file (a `String`, e.g., `/data/data/com.tumme.scrudstudents/files/user_1_profile.jpg`).
5. **UI (Event):** The UI calls `authViewModel.updatePhotoUrl(userId, newPhotoPath)` within a `CoroutineScope`.
6. **ViewModel (Logic):** The `AuthViewModel` launches a coroutine in its `viewModelScope` and uses `withContext(Dispatchers.IO)` to switch to a background thread, calling `authRepository.updateUserPhoto(userId, newPhotoPath)`.
7. **Repository (Suspend):** The `AuthRepository` calls `userDao.updateUserPhoto(userId, newPhotoPath)`.
8. **Data (Room):** The `UserDao` executes the `@Query("UPDATE users SET photoUrl = ...")`, saving the new file path string into the database.
9. **ViewModel (Manual State Update):**
   - After the database update succeeds, the `AuthViewModel` **immediately and manually updates** its `_currentUser` state: `_currentUser.value = _currentUser.value?.copy(photoUrl = newPhotoPath)`
10. **UI (Observe & Recompose):**
    - The `AsyncImage` in `StudentDashboard` is observing the `currentUser` state.
    - Because the `_currentUser` value changed, the `AsyncImage` automatically recomposes.
    - The **Coil library** (powering `AsyncImage`) is smart enough to read the new `photoUrl` value (a local file path) and automatically loads and displays the new profile photo from the device's storage.

> **Key Advantage:** This flow demonstrates a hybrid approach. We do **not** rely on Room's `Flow` to refresh (as `currentUser` is a one-time load on login). Instead, the `ViewModel` **manually updates its own state (`copy()`)** to provide the fastest, most immediate UI feedback to the user.



## 3. Justification About the Libraries Used

| Library                                | Purpose              | Justification for Use                                        |
| :------------------------------------- | :------------------- | :----------------------------------------------------------- |
| **Jetpack Compose**                    | UI Toolkit           | Google's official modern UI framework. Its declarative paradigm fits perfectly with MVVM and reactive data streams (Flow). |
| **Navigation Compose**                 | Navigation           | The official navigation library for Compose. It integrates deeply with the Composable lifecycle and is easy to manage. |
| **Hilt**                               | Dependency Injection | A DI library optimized for Android. It simplifies dependency management between ViewModels, Repositories, and DAOs, reducing boilerplate code. |
| **ViewModel**                          | State Management     | Follows the MVVM pattern. It is lifecycle-aware and used to retain UI state across configuration changes (like screen rotation). |
| **Room**                               | Local Database       | Google's recommended persistence library. The **most critical reason** for choosing it is its support for **Observable Queries (returning `Flow`)**, which is the foundation of this reactive architecture. |
| **Kotlin Coroutines (Flow/StateFlow)** | Async / State Flow   | The standard for modern Android asynchronous programming. `Flow` is used to handle reactive data streams from Room, and `StateFlow` is used to safely share state between the ViewModel and the UI. |
| **Coil (coil-compose)**                | Async Image Loading  | A modern, coroutine-based image loading library for Android. It is essential for asynchronously loading images. It integrates seamlessly with Jetpack Compose (`AsyncImage`) and, most critically, **it can load from both network URLs (`https://...`) and local file paths (`file:///...`)**, which makes our "local upload" feature possible. |



## 4. Difficulties Encountered

* **Difficulty 1: Understanding Reactive Data Reading**
    * *Description:* Initially, it was confusing to understand how to read saved data from the Room database (e.g., which courses a student was subscribed to from the `Subscribe` table). It wasn't clear how data automatically traveled from the DAO to the ViewModel and finally to the UI.
    * *Solution:* The solution was to learn Room's core feature: **Observable Queries**. We understood that a DAO can directly return a `Flow<List<...>>`, which automatically emits new data whenever the table changes. By using `.stateIn(viewModelScope, ...)` in the ViewModel to convert this to a `StateFlow`, and `collectAsState()` in the UI, we achieved a fully automatic, reactive data flow from the database to the UI with no manual refreshes.

* **Difficulty 2: Designing Role-Based Navigation**
    * *Description:* The `AppNavHost` needed to navigate to completely different Dashboards based on the logged-in user's role (`UserRole.STUDENT` or `UserRole.TEACHER`), and it had to prevent the user from navigating back to the login screen.
    * *Solution:* After a successful login in `AuthenticationScreen`, we used `navController.navigate(target)` combined with `popUpTo(Routes.AUTH) { inclusive = true }` to clear the login screen from the navigation stack, ensuring the user lands in the correct UI and cannot go back.
* **Difficulty 3: Implementing an "Offline" Image Upload Feature**
    - *Description:* A feature was needed to allow users to upload a profile photo on the `StudentDashboard`. In a normal app, this would involve a web server and cloud storage. For a demo app that is "offline-first" and only uses a local Room database, persisting the user's selected image was a challenge.
    - *Solution:* We implemented a purely local solution:
      1. Used `rememberLauncherForActivityResult` to let the user pick an image from their gallery, which returns a **temporary `Uri`**.
      2. Instead of trying to save this `Uri` (which would become invalid), we created a helper function to **copy the image from the `Uri` into the app's private internal storage** (`context.filesDir`).
      3. We saved this new, **permanent local file path** (as a `String`) into the `UserEntity`'s `photoUrl` field.
      4. Finally, we used the **Coil** library (`AsyncImage`), which can perfectly load this local file path (not just a web URL), to display the image in the UI.