package it.attendance100.mybicocca.data.remote.dto.elearning

import com.google.gson.annotations.SerializedName

data class ForumDiscussion(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("groupid") val groupId: Int? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("usermodified") val userModified: Int? = null,
    @SerializedName("timestart") val timeStart: Int? = null,
    @SerializedName("timeend") val timeEnd: Int? = null,
    @SerializedName("discussion") val discussion: Int? = null,
    @SerializedName("parent") val parent: Int? = null,
    @SerializedName("userid") val userId: Int? = null,
    @SerializedName("created") val created: Int? = null,
    @SerializedName("modified") val modified: Int? = null,
    @SerializedName("mailed") val mailed: Int? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("messageformat") val messageFormat: Int? = null,
    @SerializedName("messagetrust") val messageTrust: Int? = null,
    @SerializedName("attachment") val attachment: String? = null, // Note: Spec defines this as string (often "0" or "1")
    @SerializedName("attachments") val attachments: List<PostAttachment>? = null,
    @SerializedName("totalscore") val totalScore: Int? = null,
    @SerializedName("mailnow") val mailNow: Int? = null,
    @SerializedName("userfullname") val userFullName: String? = null,
    @SerializedName("usermodifiedfullname") val userModifiedFullName: String? = null,
    @SerializedName("userpictureurl") val userPictureUrl: String? = null,
    @SerializedName("usermodifiedpictureurl") val userModifiedPictureUrl: String? = null,
    @SerializedName("numreplies") val numReplies: Int? = null,
    @SerializedName("numunread") val numUnread: Int? = null,
    @SerializedName("pinned") val pinned: Boolean? = null,
    @SerializedName("locked") val locked: Boolean? = null,
    @SerializedName("starred") val starred: Boolean? = null,
    @SerializedName("canreply") val canReply: Boolean? = null,
    @SerializedName("canlock") val canLock: Boolean? = null,
    @SerializedName("canfavourite") val canFavourite: Boolean? = null
)