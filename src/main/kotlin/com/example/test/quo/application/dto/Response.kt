package com.example.test.quo.application.dto

import com.example.test.quo.domain.model.Author
import com.example.test.quo.domain.model.Book
import java.math.BigDecimal
import java.util.UUID

data class AuthorResponse (
    val id: UUID,
    val name: String,
    val birthDate: String,
) {
    companion object {
        fun from(author: Author): AuthorResponse {
            return AuthorResponse(
                id = author.id,
                name = author.name,
                birthDate = author.birthDate.toString()
            )
        }
    }
}

data class AuthorWithBookResponse (
    val id: UUID,
    val name: String,
    val birthDate: String,
    val books: List<InnerBook>,
) {
    data class InnerBook(
        val id: UUID,
        val title: String
    )

    companion object {
        fun from(author: Author, books: List<Book>): AuthorWithBookResponse {
            return AuthorWithBookResponse(
                id = author.id,
                name = author.name,
                birthDate = author.birthDate.toString(),
                books = books.map { InnerBook(it.id, it.title) }
            )
        }
    }
}

data class BookResponse (
    val id: UUID,
    val title: String,
    val price: BigDecimal,
    val authors: List<InnerAuthor>,
    val status: String
) {
    data class InnerAuthor (
        val id: UUID,
        val name: String,
    )

    companion object {
        fun from(book: Book): BookResponse {
            return BookResponse(
                id = book.id,
                title = book.title,
                price = book.price,
                authors = book.authors.map { author ->
                    InnerAuthor(
                        id = author.id,
                        name = author.name,
                    )
                },
                status = book.publicationStatus.name,
            )
        }
    }
}

data class ErrorResponse(
    val detail: String?
)
