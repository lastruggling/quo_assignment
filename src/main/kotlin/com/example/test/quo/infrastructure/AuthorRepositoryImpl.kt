package com.example.test.quo.infrastructure

import com.example.test.quo.domain.model.Author
import com.example.test.quo.domain.model.Book
import com.example.test.quo.domain.model.PublicationStatus
import com.example.test.quo.domain.repository.AuthorRepository
import com.example.test.quo.generated.tables.records.AuthorRecord
import com.example.test.quo.generated.tables.references.AUTHOR
import com.example.test.quo.generated.tables.references.BOOK
import com.example.test.quo.generated.tables.references.BOOK_AUTHOR_MAPPING
import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.impl.DSL.multiset
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class AuthorRepositoryImpl(
    private val dslContext: DSLContext
): AuthorRepository{
    override fun findByName(name: String): List<Author> {
        return dslContext.selectFrom(AUTHOR)
            .where(AUTHOR.NAME.contains(name))
            .fetch()
            .map { record -> record.toAuthor() }
    }

    override fun findByIds(ids: List<UUID>): List<Author> {
        return dslContext.selectFrom(AUTHOR)
            .where(AUTHOR.ID.`in`(ids))
            .fetch()
            .map { record -> record.toAuthor() }
    }

    override fun findByIdForUpdate(id: UUID): Author? {
        return dslContext.selectFrom(AUTHOR)
            .where(AUTHOR.ID.eq(id))
            .forUpdate()
            .fetchOne()
            ?.toAuthor()
    }

    override fun insert(author: Author) {
        dslContext.insertInto(AUTHOR)
            .set(AUTHOR.ID, author.id)
            .set(AUTHOR.NAME, author.name)
            .set(AUTHOR.BIRTH_DATE, author.birthDate)
            .execute()
    }

    override fun update(author: Author) {
        dslContext.update(AUTHOR)
            .set(AUTHOR.NAME, author.name)
            .set(AUTHOR.BIRTH_DATE, author.birthDate)
            .where(AUTHOR.ID.eq(author.id))
            .execute()
    }

    private fun AuthorRecord.toAuthor(): Author {
        return Author(
            id = this.id,
            name = this.name,
            birthDate = this.birthDate
        )
    }
}
