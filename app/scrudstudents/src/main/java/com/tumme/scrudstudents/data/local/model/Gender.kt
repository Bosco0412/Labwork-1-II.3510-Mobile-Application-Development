package com.tumme.scrudstudents.data.local.model

enum class Gender(val value: String) {
    Male("Male"),
    Female("Female"),
    NotConcerned("Not concerned");

    companion object {
        fun from(value: String) = Gender.entries.firstOrNull { it.value == value } ?: NotConcerned
    }
}
