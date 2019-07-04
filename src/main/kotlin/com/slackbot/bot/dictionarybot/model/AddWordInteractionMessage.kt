package com.slackbot.bot.dictionarybot.model

import com.google.gson.annotations.SerializedName

data class AddWordInteractionMessage(
        private val type: String,
        private val token: String,
        @SerializedName("action_ts") private val actionTs: String,
        private val team: Team,
        val user: User,
        private val channel: Channel,
        val submission: AddWordSubmission,
        @SerializedName("callback_id") private val callbackId: String,
        @SerializedName("response_url") val responseUrl: String,
        private val state: String
)