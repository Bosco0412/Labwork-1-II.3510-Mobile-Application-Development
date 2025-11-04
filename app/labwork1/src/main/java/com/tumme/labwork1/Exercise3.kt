package com.tumme.labwork1
class TaskManager3 {
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

    fun markTaskDone(id: Int) {
        val task = tasks.find { it.id == id }
        if (task != null) {
            task.isDone = true
            println("Task marked as done: $task")
        } else {
            println("Task not found.")
        }
    }

    fun removeTask(id: Int) {
        val removed = tasks.removeIf { it.id == id }
        if (removed) println("Task removed.") else println("Task not found.")
    }

    // Optional: Search task by keyword
    fun searchTask(keyword: String) {
        val results = tasks.filter { it.description.contains(keyword, ignoreCase = true) }
        if (results.isEmpty()) println("No tasks match \"$keyword\"")
        else results.forEach { println("${it.id} - ${it.description} [${if (it.isDone) "✓" else "✗"}]") }
    }
}

fun main() {
    val manager = TaskManager3()

    while (true) {
        println("\n===== Task Manager Menu =====")
        println("1. Add a task")
        println("2. List tasks")
        println("3. Mark a task as done")
        println("4. Remove a task")
        println("5. Search a task (optional)")
        println("6. Quit")
        print("Choose an option: ")

        when (readLine()?.toIntOrNull()) {
            1 -> {
                print("Task description: ")
                val desc = readLine() ?: ""
                manager.addTask(desc)
            }
            2 -> manager.listTasks()
            3 -> {
                print("Enter task ID to mark done: ")
                val id = readLine()?.toIntOrNull()
                if (id != null) manager.markTaskDone(id) else println("Invalid ID!")
            }
            4 -> {
                print("Enter task ID to remove: ")
                val id = readLine()?.toIntOrNull()
                if (id != null) manager.removeTask(id) else println("Invalid ID!")
            }
            5 -> {
                print("Enter keyword to search: ")
                val keyword = readLine() ?: ""
                manager.searchTask(keyword)
            }
            6 -> {
                println("Goodbye!")
                break
            }
            else -> println("Invalid choice, try again.")
        }
    }
}
