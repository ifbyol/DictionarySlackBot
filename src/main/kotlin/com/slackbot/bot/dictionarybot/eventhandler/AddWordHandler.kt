package com.slackbot.bot.dictionarybot.eventhandler

import com.slackbot.bot.dictionarybot.model.SlashCommandRequestParams
import com.slackbot.bot.dictionarybot.model.AddWordSubmission
import com.slackbot.bot.dictionarybot.model.TextResponse
import com.slackbot.bot.dictionarybot.persistence.WordDefinitionEntity
import com.slackbot.bot.dictionarybot.persistence.WordDefinitionRepository
import com.slackbot.bot.dictionarybot.thirdparties.SlackApi
import com.slackbot.bot.dictionarybot.thirdparties.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class AddWordHandler @Autowired constructor(
        private val api: SlackApi,
        private val logger: Logger = LoggerFactory.getLogger(AddWordHandler::class.java),
        private val wordDefinitionRepository: WordDefinitionRepository
) {

    fun addWord(request: SlashCommandRequestParams): TextResponse {
        GlobalScope.launch {
            openDialog(request)
        }

        return TextResponse("Your request is being processing")
    }

    fun lookForDefinition(request: SlashCommandRequestParams): TextResponse {
        GlobalScope.launch {
            searchDefinition(request)
        }

        return TextResponse("Searching...")
    }

    fun deleteDefinition(request: SlashCommandRequestParams): TextResponse {
        GlobalScope.launch {
            removeDefinition(request)
        }

        return TextResponse("Deleting...")
    }

    fun handleSubmission(submission: AddWordSubmission, responseUrl: String): String {
        GlobalScope.launch {
            handleAddWordSubmission(submission, responseUrl)
        }

        return ""
    }

    private fun getResponse(word: String, definition: String, example: String?) = "*$word* is " +
            "`$definition`. An example of use of the word in a sentence is: `$example`"

    private suspend fun openDialog(request: SlashCommandRequestParams) {
        val dialogOpenRequest = DialogOpenRequestBuilder()
                .build(request.trigger_id)

        logger.info("DialogOpenRequest: $dialogOpenRequest")

        val response = api.openDialog(request = dialogOpenRequest)

        if (!response.ok)
            logger.error("Failed to open dialog: ${response.error}")
    }

    private suspend fun searchDefinition(request: SlashCommandRequestParams) {
        val text = request.text?.toLowerCase()

        if (text == null) {
            logger.info("Cannot find a definition of a null word")
            api.sendResponse(
                    TextResponse("There was an error searching for the definition"),
                    request.response_url
            )
        } else {
            val definition = wordDefinitionRepository.findByWord(text)

            if (definition == null) {
                logger.info("Definition not found for word: `$text`")
                api.sendResponse(
                        TextResponse("Definition not found for word: `$text`"),
                        request.response_url
                )
            } else {
                api.sendResponse(
                        TextResponse(
                                getResponse(
                                        definition = definition.definition,
                                        word = definition.word,
                                        example = definition.example
                                )
                        ),
                        request.response_url
                )
            }
        }
    }

    @Transactional
    private suspend fun removeDefinition(request: SlashCommandRequestParams) {
        val text = request.text?.toLowerCase()

        if (text == null) {
            logger.info("Cannot find a definition of a null word")
            api.sendResponse(
                    TextResponse("There was an error searching for the definition"),
                    request.response_url
            )
        } else {
            wordDefinitionRepository.deleteByWord(text)
            logger.info("Definition for word `$text` was successfully deleted")

            api.sendResponse(
                    TextResponse("Definition for word `$text` was successfully deleted"),
                    request.response_url
            )
        }
    }

    private suspend fun handleAddWordSubmission(submission: AddWordSubmission, responseUrl: String) {
        logger.info("Submission received: $submission")

        logger.info("Inserting definition for ${submission.word} into the database")

        val word = submission.word.toLowerCase()
        val wordEntity = getOrCreateDefinition(submission, word)

        wordDefinitionRepository.save(wordEntity)
        logger.info("Insertion finished")

        api.sendResponse(
                TextResponse("Definition saved correctly!"),
                responseUrl
        )
    }

    private fun getOrCreateDefinition(submission: AddWordSubmission, word: String) =
        if (wordDefinitionRepository.existsByWord(word)) {
            val wordEntity = wordDefinitionRepository.findByWord(word)!!
            wordEntity.definition = submission.definition
            wordEntity.example = submission.example

            wordEntity
        } else {
            WordDefinitionEntity(
                    word = word,
                    definition = submission.definition,
                    example = submission.example
            )
        }

    companion object {
        const val DIALOG_TITLE = "Add a word"

        const val NEW_WORD_LABEL = "Word"
        const val NEW_WORD_NAME = "NewWord"
        const val NEW_WORD_TYPE = "text"
        const val NEW_WORD_PLACEHOLDER = "KPI"

        const val WORD_DEFINITION_LABEL = "Word definition"
        const val WORD_DEFINITION_NAME = "WordDefinition"
        const val WORD_DEFINITION_TYPE = "textarea"
        const val WORD_DEFINITION_PLACEHOLDER = "Key Performance Indicator"

        const val WORD_EXAMPLE_LABEL = "Example of use"
        const val WORD_EXAMPLE_NAME = "WordExample"
        const val WORD_EXAMPLE_TYPE = "textarea"
        const val WORD_EXAMPLE_PLACEHOLDER = "Your main KPI will be to increase our sales a 20%"
    }
}

private class DialogOpenRequestBuilder {

    fun build(triggerId: String): DialogOpenRequest = DialogOpenRequest(
            triggerId = triggerId,
            dialog = AddWordDialog(
                    title = AddWordHandler.DIALOG_TITLE,
                    callbackId = "1",
                    notifyOnCancel = true,
                    elements = arrayOf(
                            TextElement(
                                    label = AddWordHandler.NEW_WORD_LABEL,
                                    name = AddWordHandler.NEW_WORD_NAME,
                                    type = AddWordHandler.NEW_WORD_TYPE,
                                    placeholder = AddWordHandler.NEW_WORD_PLACEHOLDER
                            ),
                            TextAreaElement(
                                    label = AddWordHandler.WORD_DEFINITION_LABEL,
                                    name = AddWordHandler.WORD_DEFINITION_NAME,
                                    type = AddWordHandler.WORD_DEFINITION_TYPE,
                                    placeholder = AddWordHandler.WORD_DEFINITION_PLACEHOLDER
                            ),
                            TextAreaElement(
                                    label = AddWordHandler.WORD_EXAMPLE_LABEL,
                                    name = AddWordHandler.WORD_EXAMPLE_NAME,
                                    type = AddWordHandler.WORD_EXAMPLE_TYPE,
                                    placeholder = AddWordHandler.WORD_EXAMPLE_PLACEHOLDER
                            )
                    )
            )
    )
}