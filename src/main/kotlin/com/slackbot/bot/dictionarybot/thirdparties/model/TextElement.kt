package com.slackbot.bot.dictionarybot.thirdparties.model

data class TextElement(
        private val label: String,
        private val name: String,
        private val type: String,
        private val placeholder: String,
        private val value: String?
): DialogElement