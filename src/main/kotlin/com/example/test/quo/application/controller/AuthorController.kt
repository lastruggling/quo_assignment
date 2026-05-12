package com.example.test.quo.application.controller

import com.example.test.quo.application.dto.AuthorRegisterRequest
import com.example.test.quo.application.dto.AuthorResponse
import com.example.test.quo.application.dto.AuthorUpdateRequest
import com.example.test.quo.application.dto.AuthorWithBookResponse
import com.example.test.quo.domain.service.AuthorService
import com.example.test.quo.domain.service.BookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class AuthorController (
    private val authorService: AuthorService,
    private val bookService: BookService
) {
    /**
     * 新しい著者を登録する
     *
     * @param req 著者登録時必要な情報（氏名、生年月日）を含むRequest Object
     * @return 登録された著者の詳細情報
     */
    @PostMapping("/authors")
    fun registerAuthor(
        @RequestBody
        @Valid
        req: AuthorRegisterRequest
    ): ResponseEntity<AuthorResponse> {
        return authorService.register(
            name = req.name,
            birthDate = req.birthDate
        ).let { author ->
            AuthorResponse.from(author)
        }.let { response ->
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        }
    }

    /**
     * 著者情報を更新する
     *
     * @param id 更新対象の著者のID（UUID）
     * @param req 更新する値を含んだRequest Object
     * @return 更新後の著者情報
     */
    @PatchMapping("/authors/{id}")
    fun updateAuthor(
        @PathVariable
        id: UUID,
        @RequestBody
        @Valid
        req: AuthorUpdateRequest
    ): ResponseEntity<AuthorResponse> {
        return authorService.updateAuthor(
            id = id,
            name = req.name,
            birthDate = req.birthDate
        ).let { author ->
            AuthorResponse.from(author)
        }.let { response ->
            ResponseEntity.ok(response)
        }
    }

    /**
     * 著者を名前で検索する
     *
     * @param name 検索対象の著者名
     * @return 検索語が名前に含まれた著者リスト
     */
    @GetMapping("/authors/search")
    fun searchAuthorsByName(@RequestParam name: String): ResponseEntity<List<AuthorResponse>> {
        return authorService.searchByName(
            name = name
        ).map { author ->
            AuthorResponse.from(author)
        }.let { response ->
            ResponseEntity.status(HttpStatus.OK).body(response)
        }
    }

    /**
     * 著者の詳細情報を参照する
     *
     * @param id 参照対象著者のID（UUID）
     * @return 参照対象著者の情報
     */
    @GetMapping("/authors/{id}")
    fun getAuthorWithBookList(@PathVariable id: UUID): ResponseEntity<AuthorWithBookResponse> {
        return bookService.searchByAuthorId(id)
            .let { (author, books) ->
                AuthorWithBookResponse.from(author, books)
            }.let { response ->
                ResponseEntity.ok(response)
            }
    }
}
