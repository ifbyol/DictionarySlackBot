package com.slackbot.bot.dictionarybot.thirdparties.model

data class TextAreaElement(
        private val label: String,
        private val name: String,
        private val type: String,
        private val placeholder: String
): DialogElement
