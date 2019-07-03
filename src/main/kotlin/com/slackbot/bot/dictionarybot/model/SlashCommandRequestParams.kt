package com.slackbot.bot.dictionarybot.model

data class SlashCommandRequestParams(
        private val token: String,
        private val team_id: String?,
        private val team_domain: String?,
        private val enterprise_id: String?,
        private val enterprise_name: String?,
        private val channel_id: String?,
        private val channel_name: String?,
        private val user_id: String?,
        private val user_name: String?,
        private val command: String?,
        val text: String?,
        val response_url: String,
        val trigger_id: String
)