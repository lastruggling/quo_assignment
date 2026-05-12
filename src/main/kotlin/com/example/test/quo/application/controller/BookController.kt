package com.example.test.quo.application.controller

import com.example.test.quo.application.dto.BookRegisterRequest
import com.example.test.quo.application.dto.BookResponse
import com.example.test.quo.application.dto.BookUpdateRequest
import com.example.test.quo.domain.model.PublicationStatus
import com.example.test.quo.domain.service.BookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class BookController(
    private var bookService: BookService
) {
    /**
     * 新しい書籍を登録する
     *
     * @param req 書籍登録時必要な情報（タイトル、価格など）を含むRequest Object
     * @return 登録された書籍の詳細情報
     */
    @PostMapping("/books")
    fun registerBook(
        @RequestBody
        @Valid
        req: BookRegisterRequest
    ): ResponseEntity<BookResponse> {
        return bookService.registerBook(
            title = req.title,
            price = req.price,
            authorIds = req.authorIds,
            publicationStatus = PublicationStatus.of(req.status),
        ).let { registeredBook ->
            ResponseEntity.status(HttpStatus.CREATED).body(
                BookResponse.from(registeredBook)
            )
        }
    }

    /**
     * 著者情報を更新する
     *
     * @param id 更新対象の書籍のID（UUID）
     * @param req 更新する値を含んだRequest Object
     * @return 更新後の著者情報
     */
    @PatchMapping("/books/{id}")
    fun updateBook(
        @PathVariable
        id: UUID,
        @RequestBody
        @Valid
        req: BookUpdateRequest
    ): ResponseEntity<BookResponse> {
        return bookService.updateBook(
            id = id,
            title = req.title,
            price = req.price,
            authorIds = req.authorIds,
            publicationStatus = PublicationStatus.of(req.status),
        ).let { book ->
            BookResponse.from(book)
        }.let { response ->
            ResponseEntity.ok(response)
        }
    }
}
