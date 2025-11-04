package com.tumme.scrudstudents.domain.usecase.student

import com.tumme.scrudstudents.data.repository.SCRUDRepository

class GetStudentsUseCase(private val repo: SCRUDRepository) {
    operator fun invoke() = repo.getAllStudents()
}