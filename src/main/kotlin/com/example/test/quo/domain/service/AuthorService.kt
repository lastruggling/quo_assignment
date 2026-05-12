package com.example.test.quo.domain.service

import com.example.test.quo.domain.exception.AuthorException
import com.example.test.quo.domain.model.Author
import com.example.test.quo.domain.repository.AuthorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class AuthorService(
    private val authorRepository: AuthorRepository
) {
    /**
     * 著者を登録する
     *
     * @param name 氏名
     * @param birthDate 誕生日
     * @return 登録成功した著者のドメインモデル
     * @throws AuthorException.Register 著者登録失敗のカスタム例外
     */
    fun register(name: String, birthDate: LocalDate): Author {
        val newAuthor = Author(
            id = UUID.randomUUID(),
            name = name,
            birthDate = birthDate,
        )

        runCatching {
            authorRepository.insert(newAuthor)
        }.onFailure { th ->
            throw AuthorException.Register("Failed to register author.", th)
        }

        return newAuthor
    }

    /**
     * 著者を更新する
     *
     * @param id 更新対象著者のID（UUID）
     * @param name 新しい（更新後）氏名
     * @param birthDate 新しい（更新後）誕生日
     * @return 新しい（更新後）著者のドメインモデル
     * @throws AuthorException.TargetNotExist 更新対象著者検索失敗のカスタム例外
     * @throws AuthorException.Update 更新検索失敗のカスタム例外
     */
    @Transactional
    fun updateAuthor(id: UUID, name: String?, birthDate: LocalDate?): Author {
        val targetAuthor = authorRepository.findByIdForUpdate(id)
            ?: throw AuthorException.TargetNotExist("Failed to find target author.")

        val updatedAuthor = Author(
            id = id,
            name = name ?: targetAuthor.name,
            birthDate = birthDate ?: targetAuthor.birthDate,
        )

        runCatching {
            authorRepository.update(updatedAuthor)
        }.onFailure { th ->
            throw AuthorException.Update("Failed to update author.", th)
        }

        return updatedAuthor
    }

    /**
     * 名前で著者検索
     *
     * @param name 氏名
     * @return 登録成功した著者のドメインモデルのList
     * @throws AuthorException.Search 著者検索失敗のカスタム例外
     */
    fun searchByName(name: String): List<Author> {
        return runCatching {
            authorRepository.findByName(name)
        }.onFailure { th ->
            throw AuthorException.Search("Failed to search author by name.", th)
        }.getOrThrow()
    }

    /**
     * IDから著者を検索する
     *
     * @param ids 著者ID（UUID）のList
     * @return 検索結果の著者List
     * @throws AuthorException.Search 著者検索失敗のカスタム例外
     */
    fun searchByIds(ids: List<UUID>): List<Author> {
        return runCatching {
            authorRepository.findByIds(ids)
        }.onFailure { th ->
            throw AuthorException.Search("Failed to search authors by ID.", th)
        }.getOrThrow()
    }
}
