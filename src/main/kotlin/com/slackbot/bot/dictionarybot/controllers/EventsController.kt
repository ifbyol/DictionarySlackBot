package com.slackbot.bot.dictionarybot.controllers

import com.google.gson.Gson
import com.slackbot.bot.dictionarybot.eventhandler.AddWordHandler
import com.slackbot.bot.dictionarybot.eventhandler.VerificationHandler
import com.slackbot.bot.dictionarybot.model.AddWordInteractionMessage
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EventsController @Autowired constructor(
        private val verificationHandler: VerificationHandler,
        private val addWordHandler: AddWordHandler,
        private val logger: Logger = LoggerFactory.getLogger(EventsController::class.java),
        private val gson: Gson
){

    @PostMapping(path = ["/event"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun onEvent(@RequestBody event: String): ResponseEntity<String> {
        logger.info("Event received $event")
        val json = JSONObject(event)

        return when(json.getString(TYPE_ATTRIBUTE)) {
            URL_VERIFICATION_TYPE -> buildSuccessResponse(verificationHandler.handleEvent(event))
            EVENT_CALLBACK_TYPE -> buildSuccessResponse("")
            else -> {
                logger.error("Invalid 'type' parameter: $event")
                buildBadRequestResponse("Invalid 'type' parameter: $event")
            }
        }
    }

    @PostMapping(path = ["/interaction"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun interaction(@RequestParam("payload") event: String?): ResponseEntity<String> {
        logger.info("Event received $event")
        val json = JSONObject(event)

        return when(json.getString(TYPE_ATTRIBUTE)) {
            INTERACTION_EVENT_DIALOG_SUBMISSION_TYPE -> buildSuccessResponse(
                    handleDialogSubmissionEvent(gson.fromJson(event, AddWordInteractionMessage::class.java))
            )
            INTERACTION_EVENT_DIALOG_CANCELLED_TYPE -> buildSuccessResponse("")
            INTERACTIVE_MESSAGE_TYPE -> buildSuccessResponse(addWordHandler.handleAddWordInteractiveMessage(
                    json.getString(TRIGGER_ID_ATTRIBUTE),
                    json.getString(CALLBACK_ID))
            )
            else -> buildBadRequestResponse("")
        }
    }

    private fun handleDialogSubmissionEvent(event: AddWordInteractionMessage) =
            addWordHandler.handleSubmission(
                    event.submission,
                    event.responseUrl,
                    event.user
            )

    private fun buildSuccessResponse(response: String) = ResponseEntity.ok(response)

    private fun buildBadRequestResponse(response: String) = ResponseEntity.badRequest().body(response)

    companion object {
        const val TYPE_ATTRIBUTE = "type"
        const val TRIGGER_ID_ATTRIBUTE = "trigger_id"
        const val CALLBACK_ID = "callback_id"
        const val URL_VERIFICATION_TYPE = "url_verification"
        const val EVENT_CALLBACK_TYPE = "event_callback"
        const val INTERACTION_EVENT_DIALOG_SUBMISSION_TYPE = "dialog_submission"
        const val INTERACTION_EVENT_DIALOG_CANCELLED_TYPE = "dialog_cancellation"
        const val INTERACTIVE_MESSAGE_TYPE = "interactive_message"
    }
}