package com.tumme.scrudstudents.domain.usecase.student

import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.repository.SCRUDRepository

class DeleteStudentUseCase(private val repo: SCRUDRepository) {
    suspend operator fun invoke(student: StudentEntity) = repo.deleteStudent(student)
}
