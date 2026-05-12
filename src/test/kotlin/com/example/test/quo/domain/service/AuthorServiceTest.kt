package com.example.test.quo.domain.service

import com.example.test.quo.domain.exception.AuthorException
import com.example.test.quo.domain.model.Author
import com.example.test.quo.domain.repository.AuthorRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockKExtension::class)
class AuthorServiceTest {
    @MockK
    lateinit var authorRepository: AuthorRepository

    @InjectMockKs
    lateinit var sut: AuthorService

    @Nested
    @DisplayName("正常系")
    inner class Regular {
        @Test
        @DisplayName("Authorの登録成功時、登録されたAuthorを返すこと")
        fun register_success() {
            val testName = "Test Author"
            val testBirthDate = LocalDate.of(1990, 1, 1)

            justRun { authorRepository.insert(any()) }

            val actual = sut.register(testName, testBirthDate)

            assertAll({
                assertEquals(testName, actual.name)
                assertEquals(testBirthDate, actual.birthDate)
            })

            verify(exactly = 1) { authorRepository.insert(actual) }
        }

        @Test
        @DisplayName("Authorの更新成功時、更新されたAuthorを返すこと")
        fun update_success() {
            val targetAuthor = Author(
                id = UUID.randomUUID(),
                name = "Origin Author",
                birthDate = LocalDate.of(1990, 1, 1),
            )

            val testName = "Test Author"
            val testBirthDate = LocalDate.of(1991, 1, 1)

            every { authorRepository.findByIdForUpdate(any()) } returns targetAuthor
            justRun { authorRepository.update(any()) }

            val actual = sut.updateAuthor(UUID.randomUUID(), testName, testBirthDate)

            assertAll({
                assertEquals(testName, actual.name)
                assertEquals(testBirthDate, actual.birthDate)
            })

            verify(exactly = 1) { authorRepository.update(actual) }
        }

        @Test
        @DisplayName("Authorの名前で検索成功時、List<Author>を返すこと")
        fun search_by_name_success() {
            val authorList = listOf(
                Author(
                    id = UUID.randomUUID(),
                    name = "First Author",
                    birthDate = LocalDate.of(1990, 1, 1),
                ),
                Author(
                    id = UUID.randomUUID(),
                    name = "Second Author",
                    birthDate = LocalDate.of(1890, 1, 1),
                )
            )

            every { authorRepository.findByName(any()) } returns authorList

            val actual = sut.searchByName("Author")

            assertEquals(authorList, actual)
            verify(exactly = 1) { authorRepository.findByName(any()) }
        }

        @Test
        @DisplayName("AuthorのIDで検索成功時、Authorを返すこと")
        fun search_by_id_success() {
            val authorList = listOf(
                Author(
                    id = UUID.randomUUID(),
                    name = "First Author",
                    birthDate = LocalDate.of(1990, 1, 1),
                ),
                Author(
                    id = UUID.randomUUID(),
                    name = "Second Author",
                    birthDate = LocalDate.of(1890, 1, 1),
                )
            )

            every { authorRepository.findByIds(any()) } returns authorList

            val actual = sut.searchByIds(listOf(UUID.randomUUID(), UUID.randomUUID()))

            assertEquals(authorList, actual)
            verify(exactly = 1) { authorRepository.findByIds(any()) }
        }
    }

    @Nested
    @DisplayName("異常系")
    inner class Irregular {
        @Test
        @DisplayName("Authorの登録失敗時、AuthorException.Registerをthrowすること")
        fun register_failure() {
            val testName = "Test Author"
            val testBirthDate = LocalDate.of(1990, 1, 1)

            every { authorRepository.insert(any()) } throws RuntimeException("DB Error")

            assertThrows<AuthorException.Register> {
                sut.register(testName, testBirthDate)
            }
        }

        @Test
        @DisplayName("Authorの更新時、存在しないIDにより失敗すると、AuthorException.TargetNotExistをthrowすること")
        fun update_failure_notfound() {
            val testName = "Test Author"
            val testBirthDate = LocalDate.of(1991, 1, 1)

            every { authorRepository.findByIdForUpdate(any()) } returns null
            justRun { authorRepository.update(any()) }

            assertThrows<AuthorException.TargetNotExist> {
                sut.updateAuthor(UUID.randomUUID(), testName, testBirthDate)
            }
            verify(exactly = 1) { authorRepository.findByIdForUpdate(any()) }
            verify(exactly = 0) { authorRepository.update(any()) }
        }

        @Test
        @DisplayName("Authorの更新時、DB更新失敗すると、AuthorException.Updateをthrowすること")
        fun update_failure_repository() {
            val targetAuthor = Author(
                id = UUID.randomUUID(),
                name = "Origin Author",
                birthDate = LocalDate.of(1990, 1, 1),
            )
            val testName = "Test Author"
            val testBirthDate = LocalDate.of(1991, 1, 1)

            every { authorRepository.findByIdForUpdate(any()) } returns targetAuthor
            every { authorRepository.update(any()) } throws RuntimeException("DB Error")

            assertThrows<AuthorException.Update> {
                sut.updateAuthor(UUID.randomUUID(), testName, testBirthDate)
            }
            verify(exactly = 1) { authorRepository.findByIdForUpdate(any()) }
            verify(exactly = 1) { authorRepository.update(any()) }
        }

        @Test
        @DisplayName("Authorの名前で検索失敗時、AuthorException.Searchをthrowすること")
        fun search_by_name_failure() {
            every { authorRepository.findByName(any()) } throws RuntimeException("DB Error")

            assertThrows<AuthorException.Search> {
                sut.searchByName("Author")
            }
            verify(exactly = 1) { authorRepository.findByName(any()) }
        }

        @Test
        @DisplayName("AuthorのIDで検索失敗時、AuthorException.Searchをthrowすること")
        fun search_by_id_failure() {
            every { authorRepository.findByIds(any()) } throws RuntimeException("DB Error")

            assertThrows<AuthorException.Search> {
                sut.searchByIds(listOf(UUID.randomUUID(), UUID.randomUUID()))
            }
            verify(exactly = 1) { authorRepository.findByIds(any()) }
        }
    }
}
