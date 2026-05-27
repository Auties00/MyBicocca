package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.message.ConversationEntity
import it.attendance100.mybicocca.data.local.elearning.message.ConversationMemberEntity
import it.attendance100.mybicocca.data.local.elearning.message.MessageEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningConversation
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningConversationMember
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningMessageItem
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.message.Conversation
import it.attendance100.mybicocca.domain.model.elearning.message.ConversationId
import it.attendance100.mybicocca.domain.model.elearning.message.ConversationMember
import it.attendance100.mybicocca.domain.model.elearning.message.ConversationType
import it.attendance100.mybicocca.domain.model.elearning.message.Message
import it.attendance100.mybicocca.domain.model.elearning.message.MessageId
import java.time.Instant

internal data class ConversationProjection(
    val conversation: ConversationEntity,
    val members: List<ConversationMemberEntity>,
    val messages: List<MessageEntity>,
)

internal fun ElearningConversation.toProjection(accountId: AccountId): ConversationProjection {
    val members = (members ?: emptyList()).map { it.toEntity(accountId, id) }
    val msgs = (messages ?: emptyList()).map { it.toEntity(accountId, id) }
    val lastMessage = msgs.maxByOrNull { it.sentAtMs ?: Long.MIN_VALUE }
    val sortAt = lastMessage?.sentAtMs ?: System.currentTimeMillis()
    val convo = ConversationEntity(
        accountId = accountId.value,
        conversationId = id,
        typeCode = type ?: 0,
        name = name,
        subName = subname,
        imageUrl = imageUrl,
        unreadCount = unreadCount ?: 0,
        isMuted = isMuted == true,
        isFavourite = isFavourite == true,
        lastMessageId = lastMessage?.messageId,
        lastMessageText = lastMessage?.text,
        lastMessageSenderId = lastMessage?.senderUserId,
        lastMessageAtMs = lastMessage?.sentAtMs,
        sortAtMs = sortAt,
    )
    return ConversationProjection(convo, members, msgs)
}

internal fun ElearningConversationMember.toEntity(accountId: AccountId, conversationId: Int): ConversationMemberEntity =
    ConversationMemberEntity(
        accountId = accountId.value,
        conversationId = conversationId,
        userId = id,
        fullName = fullName ?: "—",
        profileImageUrl = profileImageUrl,
        isOnline = isOnline == true,
        isBlocked = isBlocked == true,
    )

internal fun ConversationMemberEntity.toDomain(): ConversationMember =
    ConversationMember(
        userId = userId,
        fullName = fullName,
        profileImageUrl = profileImageUrl,
        isOnline = isOnline,
        isBlocked = isBlocked,
    )

internal fun ElearningMessageItem.toEntity(accountId: AccountId, conversationId: Int): MessageEntity =
    MessageEntity(
        accountId = accountId.value,
        messageId = id,
        conversationId = conversationId,
        senderUserId = userIdFrom ?: 0,
        text = text.orEmpty(),
        sentAtMs = timeCreated.toMillisOrNullSec(),
        isRead = false,
    )

internal fun MessageEntity.toDomain(): Message =
    Message(
        id = MessageId(messageId),
        conversationId = ConversationId(conversationId),
        senderUserId = senderUserId,
        text = text,
        sentAt = sentAtMs?.let(Instant::ofEpochMilli),
        isRead = isRead,
    )

internal fun ConversationEntity.toDomain(
    members: List<ConversationMember>,
    lastMessage: Message?,
): Conversation =
    Conversation(
        id = ConversationId(conversationId),
        type = ConversationType.fromCode(typeCode),
        name = name,
        subName = subName,
        imageUrl = imageUrl,
        unreadCount = unreadCount,
        isMuted = isMuted,
        isFavourite = isFavourite,
        members = members,
        lastMessage = lastMessage,
    )

private fun Long?.toMillisOrNullSec(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }
