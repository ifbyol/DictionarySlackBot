package com.slackbot.bot.dictionarybot.persistence

import javax.persistence.*

@Entity
@Table(name = "unsuccessful_search_history")
class UnsuccessfulSearchHistory(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        val user: String,
        val word: String,
        val userId: String
) {
    constructor(): this(null, "", "", "")
}