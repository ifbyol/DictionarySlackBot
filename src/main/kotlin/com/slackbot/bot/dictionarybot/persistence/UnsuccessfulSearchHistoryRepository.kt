package com.slackbot.bot.dictionarybot.persistence

import org.springframework.data.repository.CrudRepository
import javax.transaction.Transactional

interface UnsuccessfulSearchHistoryRepository: CrudRepository<UnsuccessfulSearchHistory, Long> {

    fun findAllByWord(word: String): Array<UnsuccessfulSearchHistory>

    @Transactional
    fun deleteByWord(word: String)

    fun existsByWordAndUserId(word: String, userId: String): Boolean
}