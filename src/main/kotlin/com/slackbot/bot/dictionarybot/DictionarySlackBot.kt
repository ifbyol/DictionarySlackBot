package com.slackbot.bot.dictionarybot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DictionarySlackBot

fun main(args: Array<String>) {
	runApplication<DictionarySlackBot>(*args)
}
