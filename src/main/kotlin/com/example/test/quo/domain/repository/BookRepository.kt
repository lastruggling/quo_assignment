package com.example.test.quo.domain.repository

import com.example.test.quo.domain.model.Author
import com.example.test.quo.domain.model.Book
import com.example.test.quo.domain.model.PublicationStatus
import java.math.BigDecimal
import java.util.UUID

interface BookRepository {
    fun findByAuthorId(authorId: UUID): List<Book>
    fun findByIdForUpdate(id: UUID): Book?
    fun insert(book: Book)
    fun update(book: Book)
}