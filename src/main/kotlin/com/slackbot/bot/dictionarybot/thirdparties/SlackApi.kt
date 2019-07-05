package com.slackbot.bot.dictionarybot.thirdparties

import com.google.gson.Gson
import com.slackbot.bot.dictionarybot.model.TextResponse
import com.slackbot.bot.dictionarybot.thirdparties.model.AddDialogResponse
import com.slackbot.bot.dictionarybot.thirdparties.model.DialogOpenRequest
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SlackApi @Autowired constructor(
        private val gson: Gson,
        private val logger: Logger = LoggerFactory.getLogger(SlackApi::class.java)
) {

    fun openDialog(request: DialogOpenRequest): AddDialogResponse {
        val json = "application/json; charset=utf-8".toMediaType()
        val body = gson.toJson(request).toRequestBody(json)

        return gson.fromJson(sendRequest(body, "$BASE_API_URL/$DIALOG_OPEN_METHOD"), AddDialogResponse::class.java)
    }

    fun sendResponse(requestBody: TextResponse, responseUrl: String): Boolean {
        val json = "application/json; charset=utf-8".toMediaType()
        val body = gson.toJson(requestBody).toRequestBody(json)

        return try {
            sendRequest(body, responseUrl)
            true
        } catch (e: RuntimeException) {
            logger.error("There was en error sending the response", e)
            false
        }
    }

    fun publishMessage(message: String, url: String): Boolean {
        val json = "application/json; charset=utf-8".toMediaType()
        val body = message.toRequestBody(json)

        return try {
            sendRequest(body, url)
            true
        } catch (e: RuntimeException) {
            logger.error("There was en error publishing a message", e)
            false
        }
    }

    fun sendDirectMessage(message: String): Boolean {
        val json = "application/json; charset=utf-8".toMediaType()
        val body = message.toRequestBody(json)

        return try {
            sendRequest(body, "$BASE_API_URL/$POST_MESSAGE_METHOD")
            true
        } catch (e: RuntimeException) {
            logger.error("There was en error publishing a message", e)
            false
        }
    }

    private fun sendRequest(requestBody: RequestBody, url: String): String? {
        val client = OkHttpClient()
        val token = System.getenv(TOKEN_ENV_VARIABLE)
        val finalRequest = Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, "Bearer $token")
                .url(url)
                .post(requestBody)
                .build()

        val response = client.newCall(finalRequest).execute()
        if (!response.isSuccessful)
            throw RuntimeException("")

        val result = response.body?.string()

        response.close()

        return result
    }

    companion object {
        const val BASE_API_URL = "https://slack.com/api"
        const val DIALOG_OPEN_METHOD = "dialog.open"
        const val POST_MESSAGE_METHOD = "chat.postMessage"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val TOKEN_ENV_VARIABLE = "BOT_TOKEN"
    }
}