package com.slackbot.bot.dictionarybot.model

import com.google.gson.annotations.SerializedName
import com.slackbot.bot.dictionarybot.eventhandler.AddWordHandler

data class AddWordSubmission(
        @SerializedName(AddWordHandler.NEW_WORD_NAME) val word: String,
        @SerializedName(AddWordHandler.WORD_DEFINITION_NAME) val definition: String,
        @SerializedName(AddWordHandler.WORD_EXAMPLE_NAME) val example: String
)