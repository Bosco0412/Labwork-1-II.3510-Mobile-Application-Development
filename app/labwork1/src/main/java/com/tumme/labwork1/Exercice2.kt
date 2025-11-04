package com.tumme.labwork1

data class Task(
    val id: Int,
    var description: String,
    var isDone: Boolean = false
)

class TaskManager {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    fun addTask(description: String) {
        val task = Task(id = nextId++, description = description)
        tasks.add(task)
        println("Task added: $task")
    }

    fun listTasks() {
        if (tasks.isEmpty()) {
            println("No tasks found.")
        } else {
            println("Tasks List:")
            tasks.forEach { t ->
                println("${t.id} - ${t.description} [${if (t.isDone) "✓" else "✗"}]")
            }
        }
    }
}

fun main() {
    val manager = TaskManager()
    manager.addTask("Learn Kotlin basics")
    manager.addTask("Write first console app")
    manager.listTasks()
}
