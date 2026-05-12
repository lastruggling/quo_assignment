package com.example.test.quo.domain.exception


sealed class BookException(
    message: String,
    cause: Throwable? = null
): RuntimeException(message, cause) {
    class Register(message: String, cause: Throwable) : BookException(message, cause)
    class Update(message: String, cause: Throwable) : BookException(message, cause)
    class AuthorNotFound(message: String, cause: Throwable?) : BookException(message, cause)
    class BookNotFound(message: String, cause: Throwable?) : BookException(message, cause)
    class BookNotExists(message: String) : BookException(message)
    class AuthorNotExists(message: String) : BookException(message)
    class PublicationStatus(message: String): BookException(message)
}