package com.example.test.quo.application.controller

import com.example.test.quo.application.dto.ErrorResponse
import com.example.test.quo.domain.exception.AuthorException
import com.example.test.quo.domain.exception.BookException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(AuthorException::class)
    fun handleAuthorException(e: AuthorException): ResponseEntity<ErrorResponse> {
        return when(e) {
            is AuthorException.Register,
            is AuthorException.Update,
            is AuthorException.Search -> {
                ResponseEntity(ErrorResponse(e.message), HttpStatus.INTERNAL_SERVER_ERROR)
            }
            is AuthorException.TargetNotExist -> {
                ResponseEntity(ErrorResponse(e.message), HttpStatus.NOT_FOUND)
            }
        }
    }

    @ExceptionHandler(BookException::class)
    fun handleBookException(e: BookException): ResponseEntity<ErrorResponse> {
        return when(e) {
            is BookException.AuthorNotFound,
            is BookException.Register,
            is BookException.BookNotFound,
            is BookException.Update -> {
                ResponseEntity(ErrorResponse(e.message), HttpStatus.INTERNAL_SERVER_ERROR)
            }
            is BookException.BookNotExists -> {
                ResponseEntity(ErrorResponse(e.message), HttpStatus.NOT_FOUND)
            }
            is BookException.AuthorNotExists,
            is BookException.PublicationStatus -> {
                ResponseEntity(ErrorResponse(e.message), HttpStatus.BAD_REQUEST)
            }
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorMessage = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        return ResponseEntity(
            ErrorResponse(errorMessage),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleException(e: RuntimeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("Unexpected exception."),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
