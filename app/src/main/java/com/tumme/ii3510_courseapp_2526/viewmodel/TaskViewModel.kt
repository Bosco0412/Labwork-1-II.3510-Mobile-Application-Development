package com.tumme.ii3510_courseapp_2526.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.tumme.ii3510_courseapp_2526.model.Task

class TaskViewModel : ViewModel() {

    private var nextId = 1
    var tasks = mutableStateListOf<Task>()
        private set

    fun addTask(title: String, description: String = "") {
        tasks.add(Task(nextId++, title, description))
    }

    fun removeTask(task: Task) {
        tasks.remove(task)
    }

    fun getTaskById(id: Int): Task? {
        return tasks.find { it.id == id }
    }
}