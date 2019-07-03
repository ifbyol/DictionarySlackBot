package com.slackbot.bot.dictionarybot.eventhandler

interface EventHandler {

    fun handleEvent(event: String): String
}