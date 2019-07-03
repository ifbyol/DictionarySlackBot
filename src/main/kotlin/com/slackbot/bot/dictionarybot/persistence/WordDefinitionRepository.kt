package com.slackbot.bot.dictionarybot.persistence

import org.springframework.data.repository.CrudRepository
import javax.transaction.Transactional

interface WordDefinitionRepository: CrudRepository<WordDefinitionEntity, Long> {

    fun findByWord(word: String): WordDefinitionEntity?

    @Transactional
    fun deleteByWord(word: String)

    fun existsByWord(word: String): Boolean
}