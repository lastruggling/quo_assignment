package com.example.test.quo.application.controller

import com.example.test.quo.application.dto.AuthorRegisterRequest
import com.example.test.quo.domain.model.Author
import com.example.test.quo.domain.model.Book
import com.example.test.quo.domain.service.AuthorService
import com.example.test.quo.domain.service.BookService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.UUID

@ExtendWith(SpringExtension::class)
@WebMvcTest(AuthorController::class)
class AuthorControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var authorService: AuthorService

    @MockkBean
    lateinit var bookService: BookService

    @Test
    @DisplayName("正常登録時、201 Created返す")
    fun registerAuthor_success() {
        val req = AuthorRegisterRequest("Test Author", LocalDate.of(1990, 1, 1))
        val domain = Author(UUID.randomUUID(), req.name, req.birthDate)

        every { authorService.register(any(), any()) } returns domain

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value(req.name))
    }

    @Test
    @DisplayName("BirthDateが未来の場合、400 Bad Request返す")
    fun registerAuthor_fail_validation() {
        val futureDate = LocalDate.now().plusDays(1)
        val req = AuthorRegisterRequest("Future Man", futureDate)

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Authorに紐づいたBook取得成功時、200 OK返すこと")
    fun getAuthorWithBookList_success() {
        val authorId = UUID.randomUUID()
        val author = Author(authorId, "Author", LocalDate.of(1980, 5, 5))
        val books = listOf(mockk<Book>(relaxed = true))

        every { bookService.searchByAuthorId(authorId) } returns (author to books)

        mockMvc.perform(get("/authors/$authorId"))
            .andExpect(status().isOk)
    }
}