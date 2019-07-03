package com.slackbot.bot.dictionarybot.eventhandler

import com.google.gson.Gson
import com.slackbot.bot.dictionarybot.model.VerificationEvent
import com.slackbot.bot.dictionarybot.model.VerificationResponse
import org.springframework.stereotype.Component

@Component
class VerificationHandler(
        private val gson: Gson
): EventHandler {
    override fun handleEvent(event: String): String {
        val receivedEvent = gson.fromJson(event, VerificationEvent::class.java)

        return gson.toJson(VerificationResponse(receivedEvent.challenge))
    }
}