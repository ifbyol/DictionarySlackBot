package com.slackbot.bot.dictionarybot.model

import com.google.gson.annotations.SerializedName
import java.util.Arrays

open class BaseSlackEvent(
        val token: String,
        @SerializedName("team_id") val teamId: String,
        @SerializedName("api_app_id") val apiAppId: String,
        val type: String,
        @SerializedName("event_id") val eventId: String,
        @SerializedName("event_time") val eventTime: Long,
        @SerializedName("authed_users") val authedUsers: Array<String>
): SlackEvent {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseSlackEvent

        if (token != other.token) return false
        if (teamId != other.teamId) return false
        if (apiAppId != other.apiAppId) return false
        if (type != other.type) return false
        if (eventId != other.eventId) return false
        if (eventTime != other.eventTime) return false
        if (!Arrays.equals(authedUsers, other.authedUsers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = token.hashCode()
        result = 31 * result + teamId.hashCode()
        result = 31 * result + apiAppId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + eventId.hashCode()
        result = 31 * result + eventTime.hashCode()
        result = 31 * result + Arrays.hashCode(authedUsers)
        return result
    }
}