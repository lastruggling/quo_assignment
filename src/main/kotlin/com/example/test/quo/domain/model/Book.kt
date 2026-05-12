package com.example.test.quo.domain.model

import java.math.BigDecimal
import java.util.UUID

data class Book(
    val id: UUID,
    val title: String,
    val price: BigDecimal,
    val authors: List<Author>,
    val publicationStatus: PublicationStatus
)
