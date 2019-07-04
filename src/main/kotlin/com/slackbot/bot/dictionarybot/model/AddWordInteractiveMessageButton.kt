package com.slackbot.bot.dictionarybot.model

data class AddWordInteractiveMessageButton(
        val text: String,
        val attachments: Array<Attachment>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddWordInteractiveMessageButton

        if (text != other.text) return false
        if (!attachments.contentEquals(other.attachments)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + attachments.contentHashCode()
        return result
    }
}