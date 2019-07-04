package com.slackbot.bot.dictionarybot.eventhandler

import com.google.gson.Gson
import com.slackbot.bot.dictionarybot.model.*
import com.slackbot.bot.dictionarybot.persistence.UnsuccessfulSearchHistory
import com.slackbot.bot.dictionarybot.persistence.UnsuccessfulSearchHistoryRepository
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
        private val wordDefinitionRepository: WordDefinitionRepository,
        private val unsuccessfullSearchHistoryRepository: UnsuccessfulSearchHistoryRepository,
        private val gson: Gson
) {

    fun addWord(request: SlashCommandRequestParams): TextResponse {
        GlobalScope.launch {
            openDialog(request.trigger_id)
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

    fun handleSubmission(submission: AddWordSubmission, responseUrl: String, creator: User): String {
        GlobalScope.launch {
            handleAddWordSubmission(submission, responseUrl, creator)
        }

        return ""
    }

    fun handleAddWordInteractiveMessage(triggerId: String, word: String): String {
        GlobalScope.launch {
            openDialog(triggerId, word)
        }

        return ""
    }

    private fun getResponse(word: String, definition: String, example: String?) = "*$word* is " +
            "`$definition`. An example of use of the word in a sentence is: `$example`"

    private suspend fun openDialog(triggerId: String, defaultWord: String? = null) {
        val dialogOpenRequest = DialogOpenRequestBuilder()
                .build(triggerId, defaultWord)

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
                        TextResponse("Definition not found for word: `$text`. Asking in channel #${System.getenv(BOT_CHANNEL_ENV_VARIABLE)}"),
                        request.response_url
                )

                addUnsuccessfulSearchHistory(text, request.user_name, request.user_id)
                postAddWordMessage(text)
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

    private fun addUnsuccessfulSearchHistory(text: String, username: String, userId: String) {
        unsuccessfullSearchHistoryRepository.save(
                UnsuccessfulSearchHistory(
                        word = text,
                        user = username,
                        userId = userId
                )
        )
    }

    private fun postAddWordMessage(word: String) {
        val requestBody = AddWordInteractionMessageBuilder()
                .build(word)

        api.publishMessage(
                gson.toJson(requestBody),
                System.getenv(BOT_CHANNEL_WEBHOOK_ENV_VARIABLE)
        )
    }

    private fun postNewDefinitionMessage(word: String, user: User) {
        val requestBody = NewDefinitionAddedMessageBuilder()
                .build(word, user)

        api.publishMessage(
                gson.toJson(requestBody),
                System.getenv(BOT_CHANNEL_WEBHOOK_ENV_VARIABLE)
        )
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

    private suspend fun handleAddWordSubmission(submission: AddWordSubmission, responseUrl: String, creator: User) {
        logger.info("Submission received: $submission")

        logger.info("Inserting definition for ${submission.word} into the database")

        val word = submission.word.toLowerCase()
        val wordEntity = getOrCreateDefinition(submission, word)

        wordDefinitionRepository.save(wordEntity)
        postNewDefinitionMessage(word, creator)
        notifyToInterestedUsers(word, creator)
        logger.info("Insertion finished")

        api.sendResponse(
                TextResponse("Definition saved correctly!"),
                responseUrl
        )
    }

    private fun notifyToInterestedUsers(word: String, creator: User) {
        val interestedUsers = unsuccessfullSearchHistoryRepository.findAllByWord(word)
                .map {
                    User(it.userId, it.user)
                }

        interestedUsers.forEach {
            val message = DirectMessageBuilder().build(word, creator, it.id)
            api.sendDirectMessage(gson.toJson(message))
            unsuccessfullSearchHistoryRepository.deleteByWord(word)
        }
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

        const val BOT_CHANNEL_ENV_VARIABLE = "BOT_CHANNEL"
        const val BOT_CHANNEL_WEBHOOK_ENV_VARIABLE = "BOT_CHANNEL_WEBHOOK_URL"

        const val ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_TEXT = "Someone has searched `%s` but I don't have in my knowledge base"
        const val ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_QUESTION = "Do you want to add a definition?"
        const val ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_FALLBACK = "Unable to save definition"
        const val ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_ATTACHMENT_TYPE = "default"
        const val ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_COLOR = "#3AA3E3"
        const val ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_TYPE = "button"

        const val NEW_WORD_INTERACTIVE_MESSAGE_TEXT = "Definition for `%s` was added by %s"
    }
}

private class DialogOpenRequestBuilder {

    fun build(triggerId: String, defaultWord: String?): DialogOpenRequest = DialogOpenRequest(
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
                                    placeholder = AddWordHandler.NEW_WORD_PLACEHOLDER,
                                    value = defaultWord
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

private class AddWordInteractionMessageBuilder {

    fun build(word: String) = AddWordInteractiveMessageButton(
            text = AddWordHandler.ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_TEXT.format(word),
            attachments = arrayOf(
                    Attachment(
                            text = AddWordHandler.ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_QUESTION,
                            fallback = AddWordHandler.ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_FALLBACK,
                            callbackId = word,
                            attachmentType = AddWordHandler.ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_ATTACHMENT_TYPE,
                            color = AddWordHandler.ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_COLOR,
                            actions = arrayOf(
                                    Action(
                                            name = AddWordHandler.NEW_WORD_LABEL,
                                            text = AddWordHandler.DIALOG_TITLE,
                                            type = AddWordHandler.ADD_WORD_INTERACTIVE_MESSAGE_BUTTON_TYPE,
                                            value = ""
                                    )
                            )
                    )
            )
    )
}

private class NewDefinitionAddedMessageBuilder {

    fun build(word: String, user: User) = AddWordInteractiveMessageButton(
            text = AddWordHandler.NEW_WORD_INTERACTIVE_MESSAGE_TEXT.format(word, user.name),
            attachments = arrayOf()
    )
}

private class DirectMessageBuilder {

    fun build(word: String, creator: User, interestedUserId: String) = DirectMessage(
            text = AddWordHandler.NEW_WORD_INTERACTIVE_MESSAGE_TEXT.format(word, creator.name),
            channel = interestedUserId
    )
}