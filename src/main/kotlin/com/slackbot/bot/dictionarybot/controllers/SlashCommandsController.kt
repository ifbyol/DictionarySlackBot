package com.slackbot.bot.dictionarybot.controllers

import com.google.gson.Gson
import com.slackbot.bot.dictionarybot.eventhandler.AddWordHandler
import com.slackbot.bot.dictionarybot.model.SlashCommandRequestParams
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SlashCommandsController @Autowired constructor(
        private val addWordHandler: AddWordHandler,
        private val logger: Logger = LoggerFactory.getLogger(EventsController::class.java),
        private val gson: Gson
){

    @PostMapping(path = ["/add-word"], consumes = ["application/x-www-form-urlencoded;charset=UTF-8"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addWord(event: SlashCommandRequestParams): ResponseEntity<String> {
        logger.info("Event received $event")

        return buildSuccessResponse(
                gson.toJson(
                        addWordHandler.addWord(event)
                )
        )
    }

    @PostMapping(path = ["/wtf"], consumes = ["application/x-www-form-urlencoded;charset=UTF-8"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun wtfi(event: SlashCommandRequestParams): ResponseEntity<String> {
        logger.info("Event received $event")

        return buildSuccessResponse(
                gson.toJson(
                        addWordHandler.lookForDefinition(event)
                )
        )
    }

    @PostMapping(path = ["/delete-word"], consumes = ["application/x-www-form-urlencoded;charset=UTF-8"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteWord(event: SlashCommandRequestParams): ResponseEntity<String> {
        logger.info("Event received $event")

        return buildSuccessResponse(
                gson.toJson(
                        addWordHandler.deleteDefinition(event)
                )
        )
    }

    private fun buildSuccessResponse(response: String) = ResponseEntity.ok(response)
}