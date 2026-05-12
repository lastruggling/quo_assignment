package com.example.test.quo.domain.model

import java.time.LocalDate
import java.util.UUID

data class Author(
    val id: UUID,
    val name: String,
    val birthDate: LocalDate,
)
