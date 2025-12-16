package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param conversations
 * @param unreadMessages
 */


data class Conversations(

    @SerializedName("conversations")
    val conversations: List<Any>? = null,

    @SerializedName("unread_messages")
    val unreadMessages: Int? = null

)

