package com.example.test.quo.infrastructure

import com.example.test.quo.domain.model.Author
import com.example.test.quo.domain.model.Book
import com.example.test.quo.domain.model.PublicationStatus
import com.example.test.quo.domain.repository.BookRepository
import com.example.test.quo.generated.tables.records.BookRecord
import com.example.test.quo.generated.tables.references.AUTHOR
import com.example.test.quo.generated.tables.references.BOOK
import com.example.test.quo.generated.tables.references.BOOK_AUTHOR_MAPPING
import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.impl.DSL.multiset
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
class BookRepositoryImpl (
    private val dslContext: DSLContext
): BookRepository {
    private fun multisetAuthor() = multiset(
        select(AUTHOR.ID, AUTHOR.NAME, AUTHOR.BIRTH_DATE)
            .from(AUTHOR)
            .join(BOOK_AUTHOR_MAPPING).on(AUTHOR.ID.eq(BOOK_AUTHOR_MAPPING.AUTHOR_ID))
            .where(BOOK_AUTHOR_MAPPING.BOOK_ID.eq(BOOK.ID))
    ).`as`("authors").convertFrom { r ->
        r.map(Records.mapping { authorId, name, birthDate ->
            Author(
                id = authorId!!,
                name = name!!,
                birthDate = birthDate!!
            )
        })
    }

    private fun toBook(
        id: UUID,
        title: String?,
        price: BigDecimal?,
        status: String?,
        authors: List<Author>
    ): Book = Book(
        id = id,
        title = title!!,
        price = price!!,
        authors = authors,
        publicationStatus = PublicationStatus.of(status)
    )

    override fun findByIdForUpdate(id: UUID): Book? {
        return dslContext.select(
            BOOK.ID,
            BOOK.TITLE,
            BOOK.PRICE,
            BOOK.STATUS,
            multisetAuthor()
        ).from(BOOK)
            .where(BOOK.ID.eq(id))
            .forUpdate()
            .fetchOne(Records.mapping(::toBook))
    }

    override fun findByAuthorId(authorId: UUID): List<Book> {
        return dslContext.select(
            BOOK.ID,
            BOOK.TITLE,
            BOOK.PRICE,
            BOOK.STATUS,
            multisetAuthor()
        )
            .from(BOOK)
            .where(BOOK.ID.`in`(
                select(BOOK_AUTHOR_MAPPING.BOOK_ID)
                    .from(BOOK_AUTHOR_MAPPING)
                    .where(BOOK_AUTHOR_MAPPING.AUTHOR_ID.eq(authorId))
            )).fetch(Records.mapping(::toBook))
    }

    override fun insert(book: Book) {
        val insertBookQuery = dslContext.insertInto(BOOK)
            .set(BOOK.ID, book.id)
            .set(BOOK.TITLE, book.title)
            .set(BOOK.PRICE, book.price)
            .set(BOOK.STATUS, book.publicationStatus.name)

        val insertMappingQueries = book.authors.map { author ->
            dslContext.insertInto(BOOK_AUTHOR_MAPPING)
                .set(BOOK_AUTHOR_MAPPING.BOOK_ID, book.id)
                .set(BOOK_AUTHOR_MAPPING.AUTHOR_ID, author.id)
        }

        dslContext.batch(
            listOf(insertBookQuery) + insertMappingQueries
        ).execute()
    }

    override fun update(book: Book) {
        val updateBookQuery = dslContext.update(BOOK)
            .set(BOOK.TITLE, book.title)
            .set(BOOK.PRICE, book.price)
            .set(BOOK.STATUS, book.publicationStatus.name)
            .where(BOOK.ID.eq(book.id))

        val deleteMappingQuery = dslContext.deleteFrom(BOOK_AUTHOR_MAPPING)
            .where(BOOK_AUTHOR_MAPPING.BOOK_ID.eq(book.id))
            .and(BOOK_AUTHOR_MAPPING.AUTHOR_ID.notIn(book.authors.map { it.id }))

        val insertMappingQueries = book.authors.map { author ->
            dslContext.insertInto(BOOK_AUTHOR_MAPPING)
                .set(BOOK_AUTHOR_MAPPING.BOOK_ID, book.id)
                .set(BOOK_AUTHOR_MAPPING.AUTHOR_ID, author.id)
                .onConflictDoNothing()
        }

        dslContext.batch(
            listOf(updateBookQuery, deleteMappingQuery) + insertMappingQueries
        ).execute()
    }
}
