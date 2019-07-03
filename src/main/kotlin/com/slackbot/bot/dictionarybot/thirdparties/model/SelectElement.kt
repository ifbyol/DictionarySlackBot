package com.slackbot.bot.dictionarybot.thirdparties.model

import com.google.gson.annotations.SerializedName

data class SelectElement(
        private val label: String,
        private val name: String,
        private val type: String,
        @SerializedName("data_source") private val dataSource: String,
        @SerializedName("min_query_length") private val minQueryLength: Int,
        private val placeholder: String
): DialogElement