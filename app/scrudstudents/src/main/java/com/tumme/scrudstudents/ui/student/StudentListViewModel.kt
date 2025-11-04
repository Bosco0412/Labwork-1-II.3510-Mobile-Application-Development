package com.tumme.scrudstudents.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * It is responsible for providing data to the UI and handling user interaction logic.
 * The ViewModel does not hold a direct reference to the UI, allowing it to survive configuration changes like screen rotations.
 *
 * @HiltViewModel annotation indicates that Hilt can provide dependencies for this ViewModel.
 */
@HiltViewModel
class StudentListViewModel @Inject constructor(
    /**
     * The SCRUDRepository is injected via the constructor.
     * Hilt will automatically find and provide an instance of SCRUDRepository.
     */
    private val repo: SCRUDRepository
) : ViewModel() {

    /**
     * [Core of the Data Flow - Part 1: Creating StateFlow in ViewModel]
     *
     * 1. `repo.getAllStudents()` gets a Flow of the student list from the repository.
     * 2. `.stateIn(...)` is an operator that converts a regular Flow into a StateFlow.
     * - A StateFlow is a special hot data stream that always has a value.
     * - When a UI component collects from it, it immediately receives the most recent value.
     *
     * @param viewModelScope Specifies that the lifecycle of this StateFlow is tied to the ViewModel.
     * @param SharingStarted.Lazily Indicates that data collection from the upstream Flow (repo.getAllStudents()) starts only when the first UI observer (collector) appears.
     * @param emptyList() The initial value; the list is empty before data is loaded from the database.
     *
     * `_students` is a private, mutable StateFlow.
     */
    private val _students: StateFlow<List<StudentEntity>> =
        repo.getAllStudents().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    // The UI will observe this `students` state to get data.
    val students: StateFlow<List<StudentEntity>> = _students

    // UI event / error Flow
    //    SharedFlow is used because it can broadcast events to multiple collectors, and each event is consumed only once.
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    /**
     * Function to delete a student. This is triggered by a UI event.
     * @param student The student entity to be deleted.
     */
    fun deleteStudent(student: StudentEntity) = viewModelScope.launch {
        // `viewModelScope.launch` starts a coroutine whose lifecycle is tied to the ViewModel.

        repo.deleteStudent(student)
        _events.emit("Student deleted")
    }

    //  Function to insert a student.
    fun insertStudent(student: StudentEntity) = viewModelScope.launch {
        repo.insertStudent(student)
        _events.emit("Student inserted")
    }

    // Function to find a student by their ID.
    suspend fun findStudent(id: Int) = repo.getStudentById(id)

}