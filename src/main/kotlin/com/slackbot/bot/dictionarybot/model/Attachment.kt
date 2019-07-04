package com.slackbot.bot.dictionarybot.model

import com.google.gson.annotations.SerializedName

data class Attachment(
        val text: String,
        val fallback: String,
        @SerializedName("callback_id") val callbackId: String,
        @SerializedName("attachment_type") val attachmentType: String,
        val color: String,
        val actions: Array<Action>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Attachment

        if (text != other.text) return false
        if (fallback != other.fallback) return false
        if (callbackId != other.callbackId) return false
        if (attachmentType != other.attachmentType) return false
        if (color != other.color) return false
        if (!actions.contentEquals(other.actions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + fallback.hashCode()
        result = 31 * result + callbackId.hashCode()
        result = 31 * result + attachmentType.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + actions.contentHashCode()
        return result
    }

}