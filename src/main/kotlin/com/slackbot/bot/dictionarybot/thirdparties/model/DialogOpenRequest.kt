package com.slackbot.bot.dictionarybot.thirdparties.model

import com.google.gson.annotations.SerializedName

data class DialogOpenRequest(
        private val dialog: AddWordDialog,
        @SerializedName("trigger_id") private val triggerId: String
)