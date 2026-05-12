import com.example.test.quo.domain.exception.AuthorException
import com.example.test.quo.domain.exception.BookException
import com.example.test.quo.domain.model.Author
import com.example.test.quo.domain.model.Book
import com.example.test.quo.domain.model.PublicationStatus
import com.example.test.quo.domain.repository.BookRepository
import com.example.test.quo.domain.service.AuthorService
import com.example.test.quo.domain.service.BookService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@ExtendWith(MockKExtension::class)
class BookServiceTest {

    @MockK
    lateinit var authorService: AuthorService

    @MockK
    lateinit var bookRepository: BookRepository

    @InjectMockKs
    lateinit var sut: BookService

    @Nested
    @DisplayName("正常系")
    inner class Regular {

        @Test
        @DisplayName("Book登録成功時、登録されたBookを返すこと")
        fun registerBook_success() {
            val testAuthorId = UUID.randomUUID()
            val authors = listOf(
                Author(
                    testAuthorId,
                    "Test Author",
                    LocalDate.of(1990, 1, 1)
                )
            )

            every { authorService.searchByIds(listOf(testAuthorId)) } returns authors
            justRun { bookRepository.insert(any()) }

            val actual = sut.registerBook(
                title = "Test Book",
                price = 1000L,
                authorIds = listOf(testAuthorId),
                publicationStatus = PublicationStatus.PUBLISHED
            )

            assertAll({
                assertEquals("Test Book", actual.title)
                assertEquals(BigDecimal.valueOf(1000L), actual.price)
                assertEquals(authors, actual.authors)
                assertEquals(PublicationStatus.PUBLISHED, actual.publicationStatus)
            })

            verify(exactly = 1) { authorService.searchByIds(any()) }
            verify(exactly = 1) { bookRepository.insert(actual) }
        }

        @Test
        @DisplayName("Book更新成功時、更新されたBookを返すこと")
        fun updateBook_success() {
            val targetBookId = UUID.randomUUID()
            val targetBook = Book(
                id = targetBookId,
                title = "Old Title",
                price = BigDecimal.valueOf(10000L),
                authors = emptyList(),
                publicationStatus = PublicationStatus.UNPUBLISHED
            )

            every { bookRepository.findByIdForUpdate(targetBookId) } returns targetBook
            justRun { bookRepository.update(any()) }

            val actual = sut.updateBook(
                id = targetBookId,
                title = "New Title",
                price = 20000L,
                authorIds = null,
                publicationStatus = PublicationStatus.PUBLISHED
            )

            assertAll({
                assertEquals("New Title", actual.title)
                assertEquals(BigDecimal.valueOf(20000L), actual.price)
                assertEquals(PublicationStatus.PUBLISHED, actual.publicationStatus)
            })

            verify(exactly = 1) { bookRepository.update(actual) }
        }

        @Test
        @DisplayName("AuthorIdでBook検索成功時、List<Book>を返すこと")
        fun searchByAuthorId_success() {
            val authorId = UUID.randomUUID()
            val author = Author(
                    authorId, "Test Author", LocalDate.of(1990, 1, 1)
                )
            val bookList = listOf(
                Book(
                    id = UUID.randomUUID(),
                    title = "First Title",
                    price = BigDecimal.valueOf(10000L),
                    authors = listOf(author),
                    publicationStatus = PublicationStatus.PUBLISHED
                ),
                Book(
                    id = UUID.randomUUID(),
                    title = "Second Title",
                    price = BigDecimal.valueOf(20000L),
                    authors = listOf(author),
                    publicationStatus = PublicationStatus.PUBLISHED
                )
            )

            every { authorService.searchByIds(listOf(authorId)) } returns listOf(author)
            every { bookRepository.findByAuthorId(authorId) } returns bookList

            val actual = sut.searchByAuthorId(authorId)

            assertEquals(Pair(author, bookList), actual)

            verify(exactly = 1) { authorService.searchByIds(any()) }
            verify(exactly = 1) { bookRepository.findByAuthorId(any()) }
        }
    }

    @Nested
    @DisplayName("異常系")
    inner class Irregular {
        @Test
        @DisplayName("Bookの登録時、存在しないAuthorにより失敗すると、BookException.AuthorNotExistsをthrowすること")
        fun registerBook_fail_author_not_found() {
            val authorId = UUID.randomUUID()
            every { authorService.searchByIds(listOf(authorId)) } returns emptyList()

            // Act & Assert
            assertThrows<BookException.AuthorNotExists> {
                sut.registerBook("Title", 1000L, listOf(authorId), PublicationStatus.UNPUBLISHED)
            }

            verify(exactly = 0) { bookRepository.insert(any()) }
        }

        @Test
        @DisplayName("Bookの更新時、存在しないBookにより失敗すると、BookException.BookNotExistsをthrowすること")
        fun updateBook_fail_book_not_found() {
            val targetBookId = UUID.randomUUID()
            every { bookRepository.findByIdForUpdate(any()) } returns null

            assertThrows<BookException.BookNotExists> {
                sut.updateBook(targetBookId, "Title", 1000L, null, PublicationStatus.UNPUBLISHED)
            }

            verify(exactly = 1) { bookRepository.findByIdForUpdate(any()) }
            verify(exactly = 0) { authorService.searchByIds(any()) }
            verify(exactly = 0) { bookRepository.insert(any()) }
        }

        @Test
        @DisplayName("Bookの更新時、存在しないAuthorにより失敗すると、BookException.AuthorNotExistsをthrowすること")
        fun updateBook_fail_author_not_found() {
            val targetBookId = UUID.randomUUID()
            val targetBook = Book(
                id = targetBookId,
                title = "Old Title",
                price = BigDecimal.valueOf(10000L),
                authors = listOf(
                    Author(
                        UUID.randomUUID(),
                        "Test Author",
                        LocalDate.of(1990, 1, 1)
                    )
                ),
                publicationStatus = PublicationStatus.UNPUBLISHED
            )

            every { bookRepository.findByIdForUpdate(any()) } returns targetBook
            every { authorService.searchByIds(any()) } returns emptyList()

            assertThrows<BookException.AuthorNotExists> {
                sut.updateBook(targetBookId, "Title", 1000L, listOf(UUID.randomUUID()), PublicationStatus.UNPUBLISHED)
            }

            verify(exactly = 1) { bookRepository.findByIdForUpdate(any()) }
            verify(exactly = 1) { authorService.searchByIds(any()) }
            verify(exactly = 0) { bookRepository.insert(any()) }
        }

        @Test
        @DisplayName("Bookの更新時、PublicationStatus更新により失敗すると、BookException.PublicationStatusをthrowすること")
        fun updateBook_fail_invalid_status_transition() {
            val targetBookId = UUID.randomUUID()
            val authors = listOf(
                Author(
                    UUID.randomUUID(),
                    "Test Author",
                    LocalDate.of(1990, 1, 1)
                )
            )
            val targetBook = Book(
                id = targetBookId,
                title = "Test Title",
                price = BigDecimal.valueOf(1000L),
                authors = authors,
                publicationStatus = PublicationStatus.PUBLISHED
            )

            every { bookRepository.findByIdForUpdate(targetBookId) } returns targetBook

            // Act & Assert
            assertThrows<BookException.PublicationStatus> {
                sut.updateBook(targetBookId, null, null, null, PublicationStatus.UNPUBLISHED)
            }

            verify(exactly = 1) { bookRepository.findByIdForUpdate(any()) }
            verify(exactly = 0) { bookRepository.update(any()) }
        }

        @Test
        @DisplayName("Bookの更新時、DB更新失敗起因だと、BookException.Updateをthrowすること")
        fun updateBook_fail_db_fault() {
            val targetBookId = UUID.randomUUID()
            val authors = listOf(
                Author(
                    UUID.randomUUID(),
                    "Test Author",
                    LocalDate.of(1990, 1, 1)
                )
            )
            val targetBook = Book(
                id = targetBookId,
                title = "Test Title",
                price = BigDecimal.valueOf(1000L),
                authors = authors,
                publicationStatus = PublicationStatus.UNPUBLISHED
            )

            every { bookRepository.findByIdForUpdate(targetBookId) } returns targetBook
            every { authorService.searchByIds(any()) } returns authors
            every { bookRepository.update(any()) } throws RuntimeException("DB error")

            assertThrows<BookException.Update> {
                sut.updateBook(targetBookId, null, null, null, PublicationStatus.PUBLISHED)
            }

            verify(exactly = 1) { bookRepository.findByIdForUpdate(any()) }
            verify(exactly = 1) { bookRepository.update(any()) }
        }

        @Test
        @DisplayName("Book検索失敗時、Author検索失敗が原因だと、BookException.AuthorNotFoundをthrowすること")
        fun searchByAuthorId_failure_search_author_fault() {
            val authorId = UUID.randomUUID()

            every { authorService.searchByIds(any()) } throws AuthorException.Search(
                "Failed to search authors by ID.", RuntimeException("DB error")
            )

            assertThrows<BookException.AuthorNotFound> {
                sut.searchByAuthorId(authorId)
            }

            verify(exactly = 1) { authorService.searchByIds(any()) }
            verify(exactly = 0) { bookRepository.findByAuthorId(any()) }
        }

        @Test
        @DisplayName("Book検索失敗時、Author検索結果なしが原因だと、BookException.AuthorEmptyをthrowすること")
        fun searchByAuthorId_failure_search_author_not_found() {
            val authorId = UUID.randomUUID()

            every { authorService.searchByIds(any()) } returns emptyList()

            assertThrows<BookException.AuthorNotExists> {
                sut.searchByAuthorId(authorId)
            }

            verify(exactly = 1) { authorService.searchByIds(any()) }
            verify(exactly = 0) { bookRepository.findByAuthorId(any()) }
        }

        @Test
        @DisplayName("Book検索失敗時、Book検索失敗が原因だと、BookException.BookNotFoundをthrowすること")
        fun searchByAuthorId_failure_search_book_db_fault() {
            val authorId = UUID.randomUUID()
            val author = Author(
                authorId, "Test Author", LocalDate.of(1990, 1, 1)
            )

            every { authorService.searchByIds(any()) } returns listOf(author)
            every { bookRepository.findByAuthorId(authorId) } throws RuntimeException("DB error")

            assertThrows<BookException.BookNotFound> {
                sut.searchByAuthorId(authorId)
            }

            verify(exactly = 1) { authorService.searchByIds(any()) }
            verify(exactly = 1) { bookRepository.findByAuthorId(any()) }
        }
    }
}
