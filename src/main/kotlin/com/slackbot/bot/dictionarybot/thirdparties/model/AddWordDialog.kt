package com.slackbot.bot.dictionarybot.thirdparties.model

import com.google.gson.annotations.SerializedName

class AddWordDialog(
        private val title: String,
        @SerializedName("callback_id") private val callbackId: String,
        private val elements: Array<DialogElement>,
        @SerializedName("notify_on_cancel") private val notifyOnCancel: Boolean
)