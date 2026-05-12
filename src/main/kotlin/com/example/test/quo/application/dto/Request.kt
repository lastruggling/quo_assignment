package com.example.test.quo.application.dto

import com.example.test.quo.domain.model.PublicationStatus
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.util.UUID

data class AuthorRegisterRequest(
    val name: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @field:Past("Birth Date must be in the past.")
    val birthDate: LocalDate,
)

data class AuthorUpdateRequest(
    val name: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @field:Past("Birth Date must be in the past.")
    val birthDate: LocalDate?,
)

data class BookRegisterRequest(
    val title: String,
    @field:Min(value = 0, message = "Price must cannot be negative.")
    val price: Long,
    @field:NotEmpty("At least 1 author required.")
    val authorIds: List<UUID>,
    @field:NotBlank(message = "Status is required.")
    @field:Pattern(
        regexp = "(?i)PUBLISHED|UNPUBLISHED",
        message = "Status must be one of: PUBLISHED, UNPUBLISHED"
    )
    val status: String
)

data class BookUpdateRequest(
    val title: String? = null,
    @field:Min(value = 0, message = "Price must cannot be negative.")
    val price: Long? = null,
    @field:Size(min = 1, message = "At least 1 author required.")
    val authorIds: List<UUID>? = null,
    @field:Pattern(
        regexp = "(?i)PUBLISHED|UNPUBLISHED",
        message = "Status must be one of: PUBLISHED, UNPUBLISHED"
    )
    val status: String? = null
)
