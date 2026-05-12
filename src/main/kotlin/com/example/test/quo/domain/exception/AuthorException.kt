package com.example.test.quo.domain.exception

sealed class AuthorException(
    message: String,
    cause: Throwable? = null
): RuntimeException(message, cause) {
    class Register(message: String, cause: Throwable) : AuthorException(message, cause)
    class Update(message: String, cause: Throwable) : AuthorException(message, cause)
    class TargetNotExist(message: String) : AuthorException(message)
    class Search(message: String, cause: Throwable) : AuthorException(message, cause)
}