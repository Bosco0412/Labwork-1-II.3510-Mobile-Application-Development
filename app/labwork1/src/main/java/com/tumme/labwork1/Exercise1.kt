package com.tumme.labwork1

fun greetUser() {
    print("Enter your name: ")
    val name = readLine() ?: "User"
    println("Hello, $name! Welcome to the Task Manager.")
}

fun addNumbers(a: Int, b: Int): Int {
    return a + b
}

fun main() {
    greetUser()

    // Test addNumbers
    println("Testing addNumbers: 5 + 7 = ${addNumbers(5, 7)}")

    // Test reading an integer from console
    print("Enter a number: ")
    val number = readLine()?.toIntOrNull()
    if (number != null) {
        println("You entered: $number")
    } else {
        println("Invalid number entered!")
    }
}
