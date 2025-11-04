package com.tumme.scrudstudents.ui.subscribe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Subscribe feature.
 * It provides data for both the subscription list and the subscription form.
 */
@HiltViewModel
class SubscribeListViewModel @Inject constructor(
    private val repo: SCRUDRepository
) : ViewModel() {

    // --- Data Flows for UI ---

    /**
     * A StateFlow that holds the list of all subscriptions.
     * The UI will collect this flow to display the list.
     */
    val subscribes: StateFlow<List<SubscribeEntity>> =
        repo.getAllSubscribes().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * A StateFlow that holds the list of all students.
     * This is used by the SubscribeFormScreen to populate the student selection dropdown.
     */
    val students: StateFlow<List<StudentEntity>> =
        repo.getAllStudents().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * A StateFlow that holds the list of all courses.
     * This is used by the SubscribeFormScreen to populate the course selection dropdown.
     */
    val courses: StateFlow<List<CourseEntity>> =
        repo.getAllCourses().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * A SharedFlow for broadcasting one-time events to the UI, like snackbar messages.
     */
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    // --- CRUD Functions ---

    /**
     * Inserts a new subscription into the database.
     * @param subscribe The subscription entity to be inserted.
     */
    fun insertSubscribe(subscribe: SubscribeEntity) = viewModelScope.launch {
        repo.insertSubscribe(subscribe)
        _events.emit("Subscription added")
    }

    /**
     * Deletes a subscription from the database.
     * @param subscribe The subscription entity to be deleted.
     */
    fun deleteSubscribe(subscribe: SubscribeEntity) = viewModelScope.launch {
        repo.deleteSubscribe(subscribe)
        _events.emit("Subscription removed")
    }

    /**
     * Updates an existing subscription in the database.
     * @param subscribe The subscription entity to be updated.
     */
    fun updateSubscribe(subscribe: SubscribeEntity) = viewModelScope.launch {
        repo.updateSubscribe(subscribe)
        _events.emit("Subscription updated")
    }

    /**
     * Finds a specific subscription by its student and course IDs.
     * This is used by the form screen when in edit mode.
     */
    suspend fun findSubscription(studentId: Int, courseId: Int): SubscribeEntity? {
        return repo.findSubscription(studentId, courseId)
    }
}

