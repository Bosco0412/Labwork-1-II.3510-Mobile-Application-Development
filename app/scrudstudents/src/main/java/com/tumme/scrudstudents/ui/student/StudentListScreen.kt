package com.tumme.scrudstudents.ui.student

import com.tumme.scrudstudents.ui.components.TableHeader
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * The main UI screen for displaying the list of students.
 * This Composable function is responsible for observing data from the ViewModel and rendering it on the screen.
 * It is a stateless Composable as it receives its state and event handlers from the outside.
 *
 * @Composable annotation marks this function as a Jetpack Compose UI component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    /**
     * An instance of StudentListViewModel, provided by Hilt.
     * `hiltViewModel()` is a helper function that finds and returns the correct ViewModel instance scoped to the navigation graph.
     */
    viewModel: StudentListViewModel = hiltViewModel(),
    /**
     * A lambda function to handle navigation to the student creation/edit form.
     * This is passed from the navigation graph.
     */
    onNavigateToForm: () -> Unit = {},
    //  A lambda function to handle navigation to the student detail screen, passing the student's ID.
    onNavigateToDetail: (Int) -> Unit = {}
) {
    /**
     * [Core of the Data Flow - Part 2: Collecting State in the UI]
     *
     * `viewModel.students` is the StateFlow from the ViewModel.
     * `.collectAsState()` is a Composable function that collects values from a Flow
     * and represents the latest value as a Compose State.
     * Whenever a new list of students is emitted by the StateFlow in the ViewModel, this `students` State object will be updated.
     * This update will trigger a "recomposition" of any Composable that reads its value, causing the UI to refresh automatically.
     *
     * The `by` keyword is a property delegate that unpacks the State<List<StudentEntity>> into a simple List<StudentEntity>.
     */
    val students by viewModel.students.collectAsState()
    //val scaffoldState = rememberScaffoldState()
    // A coroutine scope that can be used to launch coroutines in response to UI events.
    val coroutineScope = rememberCoroutineScope()

    // Scaffold provides a standard layout structure for Material Design screens.
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Students") })
        },
        floatingActionButton = {
            // The FAB triggers the onNavigateToForm lambda when clicked, to open the form screen.
            FloatingActionButton(onClick = onNavigateToForm) {
                Text("+")
            }
        }
    ) { padding -> // `padding` contains the inner padding values for the content area.
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
        ) {
            // A custom Composable to display the table header.
            TableHeader(cells = listOf("DOB","Last", "First", "Gender", "Actions"),
                weights = listOf(0.25f, 0.25f, 0.25f, 0.15f, 0.10f))

            Spacer(modifier = Modifier.height(8.dp))
            // LazyColumn is an efficient way to display a scrollable list.
            // It only composes and lays out the items that are currently visible on the screen.
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // `items` is a helper function to build the list from the `students` data.
                items(students) { student ->
                    // StudentRow is another Composable responsible for displaying a single student's data.
                    StudentRow(
                        student = student,
                        onEdit = { /* navigate to form prefilled (not implemented here) */ },
                        // When the delete action is triggered, it calls the corresponding function in the ViewModel.
                        onDelete = { viewModel.deleteStudent(student) },
                        // When the view action is triggered, it calls the navigation lambda with the student's ID.
                        onView = { onNavigateToDetail(student.idStudent) },
                        onShare = { /* share intent */ }
                    )
                }
            }
        }
    }
}
