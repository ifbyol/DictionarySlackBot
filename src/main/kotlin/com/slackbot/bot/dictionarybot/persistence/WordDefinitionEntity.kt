package com.slackbot.bot.dictionarybot.persistence

import javax.persistence.*

@Entity
@Table(name = "word_definitions")
class WordDefinitionEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        val word: String,
        var definition: String,
        var example: String?
) {
    constructor(): this(null, "", "", null)
}