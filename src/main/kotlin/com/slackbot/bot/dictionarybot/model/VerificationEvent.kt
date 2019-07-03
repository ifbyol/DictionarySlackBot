package com.slackbot.bot.dictionarybot.model

class VerificationEvent(val token: String, val challenge: String, val type: String): SlackEvent