package com.slackbot.bot.dictionarybot.persistence

import org.springframework.data.repository.CrudRepository
import javax.transaction.Transactional

interface UnsuccessfulSearchHistoryRepository: CrudRepository<UnsuccessfulSearchHistory, Long> {

    fun findAllByWord(word: String): Array<UnsuccessfulSearchHistory>

    @Transactional
    fun deleteByWord(word: String)

    fun existsByWord(word: String): Boolean
}