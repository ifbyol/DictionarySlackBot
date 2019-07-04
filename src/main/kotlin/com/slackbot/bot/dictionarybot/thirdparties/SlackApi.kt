package com.slackbot.bot.dictionarybot.thirdparties

import com.google.gson.Gson
import com.slackbot.bot.dictionarybot.model.TextResponse
import com.slackbot.bot.dictionarybot.thirdparties.model.AddDialogResponse
import com.slackbot.bot.dictionarybot.thirdparties.model.DialogOpenRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SlackApi @Autowired constructor(
        private val gson: Gson
) {

    fun openDialog(request: DialogOpenRequest): AddDialogResponse {
        val client = OkHttpClient()
        val json = "application/json; charset=utf-8".toMediaType()
        val body = gson.toJson(request).toRequestBody(json)
        val token = System.getenv(TOKEN_ENV_VARIABLE)
        val finalRequest = Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, "Bearer $token")
                .url("$BASE_API_URL/$DIALOG_OPEN_METHOD")
                .post(body)
                .build()

        val response = client.newCall(finalRequest).execute()

        response.close()
        if (response.isSuccessful) return gson.fromJson(response.body?.string(), AddDialogResponse::class.java)

        throw RuntimeException("Error opening a dialog in slack: ${response.body}")
    }

    fun sendResponse(requestBody: TextResponse, responseUrl: String): Boolean {
        val client = OkHttpClient()
        val json = "application/json; charset=utf-8".toMediaType()
        val body = gson.toJson(requestBody).toRequestBody(json)
        val token = System.getenv(TOKEN_ENV_VARIABLE)
        val finalRequest = Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, "Bearer $token")
                .url(responseUrl)
                .post(body)
                .build()

        val response = client.newCall(finalRequest).execute()
        response.close()

        return response.isSuccessful
    }

    fun publishMessage(message: String, url: String): Boolean {
        val client = OkHttpClient()
        val json = "application/json; charset=utf-8".toMediaType()
        val body = message.toRequestBody(json)
        val token = System.getenv(TOKEN_ENV_VARIABLE)
        val finalRequest = Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, "Bearer $token")
                .url(url)
                .post(body)
                .build()

        val response = client.newCall(finalRequest).execute()
        response.close()

        return response.isSuccessful
    }

    fun sendDirectMessage(message: String): Boolean {
        val client = OkHttpClient()
        val json = "application/json; charset=utf-8".toMediaType()
        val body = message.toRequestBody(json)
        val token = System.getenv(TOKEN_ENV_VARIABLE)
        val finalRequest = Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, "Bearer $token")
                .url("$BASE_API_URL/$POST_MESSAGE_METHOD")
                .post(body)
                .build()

        val response = client.newCall(finalRequest).execute()
        response.close()

        return response.isSuccessful
    }

    companion object {
        const val BASE_API_URL = "https://slack.com/api"
        const val DIALOG_OPEN_METHOD = "dialog.open"
        const val POST_MESSAGE_METHOD = "chat.postMessage"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val TOKEN_ENV_VARIABLE = "BOT_TOKEN"
    }
}