package com.tumme.scrudstudents.domain.usecase.student

import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.repository.SCRUDRepository

class InsertStudentUseCase(private val repo: SCRUDRepository) {
    suspend operator fun invoke(student: StudentEntity) = repo.insertStudent(student)
}
