package it.attendance100.mybicocca.data.remote.dto.elearning

import com.google.gson.annotations.SerializedName

data class DiscussionPost(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("replysubject") val replySubject: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("messageformat") val messageFormat: Int? = null,
    @SerializedName("author") val author: PostAuthor? = null,
    @SerializedName("discussionid") val discussionId: Int? = null,
    @SerializedName("hasparent") val hasParent: Boolean? = null,
    @SerializedName("parentid") val parentId: Int? = null,
    @SerializedName("timecreated") val timeCreated: Int? = null,
    @SerializedName("timemodified") val timeModified: Int? = null,
    @SerializedName("unread") val unread: Boolean? = null,
    @SerializedName("isdeleted") val isDeleted: Boolean? = null,
    @SerializedName("isprivatereply") val isPrivateReply: Boolean? = null,
    @SerializedName("haswordcount") val hasWordCount: Boolean? = null,
    @SerializedName("wordcount") val wordCount: Int? = null,
    @SerializedName("charcount") val charCount: Int? = null,
    @SerializedName("capabilities") val capabilities: PostCapabilities? = null,
    @SerializedName("urls") val urls: PostUrls? = null,
    @SerializedName("attachments") val attachments: List<PostAttachment>? = null,
    @SerializedName("messageinlinefiles") val messageInlineFiles: List<PostAttachment>? = null,
    @SerializedName("tags") val tags: List<PostTag>? = null,
    @SerializedName("html") val html: PostHtml? = null
)

