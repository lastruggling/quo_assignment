package com.example.test.quo.domain.repository

import com.example.test.quo.domain.model.Author
import java.time.LocalDate
import java.util.UUID

interface AuthorRepository {
    fun findByName(name: String): List<Author>
    fun findByIds(ids: List<UUID>): List<Author>
    fun findByIdForUpdate(id: UUID): Author?
    fun insert(author: Author)
    fun update(author: Author)
}
