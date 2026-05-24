package com.example.student

import cz.petrchatrny.kreator.annotations.Dto
import cz.petrchatrny.kreator.annotations.FieldConstants
import cz.petrchatrny.kreator.annotations.Kreator

import com.example.student.StudentFields.id
import com.example.student.StudentFields.enrollmentYear

import java.time.LocalDate

@FieldConstants
@Kreator(
    Dto("Create", omit = ["id"]),
    Dto("Update", omit = ["id", "enrollmentYear"]),
    isSealed = true
)
data class Student(
    val id: Long? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val birthDate: LocalDate,
    val enrollmentYear: Int
)

