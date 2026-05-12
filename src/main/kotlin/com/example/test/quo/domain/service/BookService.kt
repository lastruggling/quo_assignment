package com.example.test.quo.domain.service

import com.example.test.quo.domain.exception.BookException
import com.example.test.quo.domain.model.Author
import com.example.test.quo.domain.model.Book
import com.example.test.quo.domain.model.PublicationStatus
import com.example.test.quo.domain.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID
import kotlin.String

@Service
class BookService (
    private val authorService: AuthorService,
    private val bookRepository: BookRepository
) {
    /**
     * 書籍を登録する
     *
     * @param title タイトル
     * @param price 価格
     * @param authorIds 著者のID（UUID）リスト
     * @param publicationStatus 出版状況
     * @return 登録成功した書籍のドメインモデル
     * @throws BookException.Register 書籍登録失敗のカスタム例外
     */
    @Transactional
    fun registerBook(
        title: String,
        price: Long,
        authorIds: List<UUID>,
        publicationStatus: PublicationStatus
    ): Book {
        val authors = authorService.searchByIds(authorIds)
            .also { searchedAuthors ->
                validateBookAuthors(authorIds, searchedAuthors)
            }

        val newBook = Book(
            id = UUID.randomUUID(),
            title = title,
            price = BigDecimal.valueOf(price),
            authors = authors,
            publicationStatus = publicationStatus,
        )

        runCatching {
            bookRepository.insert(newBook)
        }.onFailure { th ->
            throw BookException.Register("Failed to register book.", th)
        }

        return newBook
    }

    /**
     * 書籍を更新する
     *
     * @param id 更新対象書籍のID（UUID）
     * @param title 新しい（更新後）タイトル
     * @param price 新しい（更新後）誕生日
     * @param authorIds 新しい（更新後）著者リスト
     * @param publicationStatus 新しい（更新後）出版状況
     * @return 新しい（更新後）書籍のドメインモデル
     * @throws BookException.BookNotFound 更新対象書籍検索失敗のカスタム例外
     * @throws BookException.AuthorNotFound 登録されてない著者が含まれた場合のカスタム例外
     * @throws BookException.PublicationStatus 出版済みから出版前へ更新する場合のカスタム例外
     * @throws BookException.Update 更新失敗時のカスタム例外
     */
    @Transactional
    fun updateBook(
        id: UUID,
        title: String?,
        price: Long?,
        authorIds: List<UUID>?,
        publicationStatus: PublicationStatus?
    ): Book {
        val targetBook = bookRepository.findByIdForUpdate(id)
            ?: throw BookException.BookNotExists("Book not found.")

        val newAuthors = authorIds?.let {
            authorService.searchByIds(authorIds)
        }?.also { searchedAuthors ->
            validateBookAuthors(authorIds, searchedAuthors)
        }
        validatePublicationStatus(targetBook.publicationStatus, publicationStatus)

        val updatedBook = Book(
            id = id,
            title = title ?: targetBook.title,
            price = price?.let { BigDecimal.valueOf(it) } ?: targetBook.price,
            authors = newAuthors ?: targetBook.authors,
            publicationStatus = publicationStatus ?: targetBook.publicationStatus
        )

        runCatching {
            bookRepository.update(updatedBook)
        }.onFailure { th ->
            throw BookException.Update("Failed to update book.", th)
        }

        return updatedBook
    }

    /**
     * 特定著者に紐づく書籍を検索する
     *
     * @param authorId 検索対象の著者ID（UUID）
     * @return 著者と著者に紐づく書籍リストのPair
     * @throws BookException.AuthorNotFound 著者検索失敗・未登録著者の場合のカスタム例外
     * @throws BookException.BookNotFound 書籍検索に失敗した場合のカスタム例外
     */
    fun searchByAuthorId(authorId: UUID): Pair<Author, List<Book>> {
        val targetAuthor = runCatching {
            authorService.searchByIds(listOf(authorId)).firstOrNull()
        }.onFailure { th ->
            throw BookException.AuthorNotFound("Failed to find author.", th)
        }.getOrThrow() ?: throw BookException.AuthorNotExists("Author not found.")

        val books = runCatching {
            bookRepository.findByAuthorId(authorId)
        }.onFailure { th ->
            throw BookException.BookNotFound("Failed to find book.", th)
        }.getOrThrow()

        return Pair(targetAuthor, books)
    }

    /**
     * 著者リストが正しいか検証する
     *
     * 登録済みでない著者IDが含まれてるかを件数で検証する
     *
     * @param authorIds 検索対象の著者のIDリスト（UUID）
     * @param authors 検索結果の著者リスト
     * @throws BookException.AuthorNotFound リクエストされた著者ID件数と検索結果が一致しない場合の例外
     */
    private fun validateBookAuthors(authorIds: List<UUID>, authors: List<Author>) {
        if (authorIds.size != authors.size) {
            throw BookException.AuthorNotExists("Author list contains not registered author.")
        }
    }

    /**
     * 出版状況更新時の検証
     *
     * 更新前PUBLISHEDを、UNPUBLISHEDに更新しようとする場合例外発生する
     *
     * @param before 更新前の出版状況
     * @param after 更新後の出版状況
     * @throws BookException.PublicationStatus 出版済みから出版後に更新する場合の例外
     */
    private fun validatePublicationStatus(before: PublicationStatus, after: PublicationStatus?) {
        if(before == PublicationStatus.PUBLISHED && after == PublicationStatus.UNPUBLISHED) {
            throw BookException.PublicationStatus("PublicationStatus cannot be updated to unpublished.")
        }
    }
}
