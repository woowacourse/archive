package io.github.woowacourse.archive.conversation

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import java.time.LocalDateTime.now

data class ConversationDto(
    val id: Long,
    val message: String,
    val userId: String,
    val conversationTime: LocalDateTime,
    val files: List<String>
) {
    constructor(conversation: Conversation) :
        this(
            conversation.id,
            conversation.message,
            conversation.userId,
            conversation.conversationTime,
            conversation.files.map { it.url }
        )

    companion object {
        fun listOf(conversations: List<Conversation>): List<ConversationDto> {
            return conversations.map { ConversationDto(it) }
        }
    }
}

data class ConversationDetailDto(
    val id: Long,
    val message: String,
    val userId: String,
    val conversationTime: LocalDateTime,
    val files: List<String>,
    val replies: List<ReplyDto>
) {
    constructor(conversation: Conversation) :
        this(
            conversation.id,
            conversation.message,
            conversation.userId,
            conversation.conversationTime,
            conversation.files.map { it.url },
            ReplyDto.listOf(conversation.replies)
        )
}

data class ReplyDto(
    val message: String,
    val userId: String,
    val replyTime: LocalDateTime,
    val files: List<String>,
    val id: Long
) {
    constructor(reply: Reply) :
        this(
            reply.message,
            reply.userId,
            reply.replyTime,
            reply.files.map { it.url },
            reply.id
        )

    companion object {
        fun listOf(replies: List<Reply>): List<ReplyDto> {
            return replies.map { ReplyDto(it) }
        }
    }
}

data class ConversationRequestDto(
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val conversationTime: LocalDateTime = now(),
    val message: String = "",
    val size: Int
)
