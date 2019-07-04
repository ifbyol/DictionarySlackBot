package com.slackbot.bot.dictionarybot.model

data class Action(
        val name: String,
        val text: String,
        val type: String,
        val value: String
)