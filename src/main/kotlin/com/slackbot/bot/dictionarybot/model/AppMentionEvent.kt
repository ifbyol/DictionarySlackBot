package com.slackbot.bot.dictionarybot.model

import com.google.gson.annotations.SerializedName

class AppMentionEvent(
        token: String,
        teamId: String,
        apiAppId: String,
        type: String,
        eventId: String,
        eventTime: Long,
        authedUsers: Array<String>,
        val event: Event
): BaseSlackEvent(
        token,
        teamId,
        apiAppId,
        type,
        eventId,
        eventTime,
        authedUsers
) {
    class Event(
            val type: String,
            val user: String,
            val text: String,
            val ts: String,
            val channel: String,
            @SerializedName("event_ts") val eventTs: String
    )
}
