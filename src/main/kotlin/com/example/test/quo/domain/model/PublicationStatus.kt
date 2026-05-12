package com.example.test.quo.domain.model

enum class PublicationStatus {
    PUBLISHED,
    UNPUBLISHED;

    companion object {
        /**
         * StringからPublicationStatusへ変換
         * Nullの場合、デフォルトでPUBLISHED変換
         *
         * @param value PublicationStatusの文字列
         * @return 変換したPublicationStatus
         */
        fun of(value: String?): PublicationStatus {
            if (value == null) return PUBLISHED
            return entries.find { it.name == value.uppercase() } ?: PUBLISHED
        }
    }
}
