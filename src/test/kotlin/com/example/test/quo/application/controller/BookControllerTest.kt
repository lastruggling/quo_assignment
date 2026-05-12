package com.example.test.quo.application.controller

import com.example.test.quo.application.dto.BookRegisterRequest
import com.example.test.quo.application.dto.BookUpdateRequest
import com.example.test.quo.domain.model.Book
import com.example.test.quo.domain.model.PublicationStatus
import com.example.test.quo.domain.service.BookService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
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
import java.util.UUID

@ExtendWith(SpringExtension::class)
@WebMvcTest(BookController::class)
class BookControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var bookService: BookService

    @Test
    @DisplayName("正常登録時、201 Created返す")
    fun registerBook_success() {
        val req = BookRegisterRequest(
            title = "Test Book",
            price = 20000L,
            authorIds = listOf(UUID.randomUUID()),
            status = "PUBLISHED"
        )
        val domain = mockk<Book>(relaxed = true)

        every { bookService.registerBook(any(), any(), any(), any()) } returns domain

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
    }

    @Test
    @DisplayName("PriceがNegative、Authorが空の場合400 Bad Request返す")
    fun registerBook_fail_validation() {
        val req = BookRegisterRequest(
            title = "Invalid Book",
            price = -100L, // Validation Fail
            authorIds = emptyList(), // Validation Fail
            status = "UNPUBLISHED"
        )

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("更新成功時、200 OK返す")
    fun updateBook_success() {
        val bookId = UUID.randomUUID()
        val req = BookUpdateRequest(
            title = "Changed Title"
        )
        val domain = mockk<Book>(relaxed = true)

        every { bookService.updateBook(eq(bookId), any(), any(), any(), any()) } returns domain

        mockMvc.perform(
            patch("/books/$bookId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
    }
}