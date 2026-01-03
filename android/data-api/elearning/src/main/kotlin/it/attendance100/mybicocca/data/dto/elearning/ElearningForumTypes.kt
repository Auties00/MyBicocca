package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a forum.
 */
@Serializable
data class ElearningForum(
    @SerialName("id")
    val id: Int,
    @SerialName("course")
    val course: Int,
    @SerialName("type")
    val type: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("intro")
    val intro: String? = null,
    @SerialName("introformat")
    val introFormat: Int? = null,
    @SerialName("introfiles")
    val introFiles: List<ElearningFile>? = null,
    @SerialName("duedate")
    val dueDate: Long? = null,
    @SerialName("cutoffdate")
    val cutOffDate: Long? = null,
    @SerialName("assessed")
    val assessed: Int? = null,
    @SerialName("assesstimestart")
    val assessTimeStart: Long? = null,
    @SerialName("assesstimefinish")
    val assessTimeFinish: Long? = null,
    @SerialName("scale")
    val scale: Int? = null,
    @SerialName("maxbytes")
    val maxBytes: Int? = null,
    @SerialName("maxattachments")
    val maxAttachments: Int? = null,
    @SerialName("forcesubscribe")
    val forceSubscribe: Int? = null,
    @SerialName("trackingtype")
    val trackingType: Int? = null,
    @SerialName("rsstype")
    val rssType: Int? = null,
    @SerialName("rssarticles")
    val rssArticles: Int? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("warnafter")
    val warnAfter: Int? = null,
    @SerialName("blockafter")
    val blockAfter: Int? = null,
    @SerialName("blockperiod")
    val blockPeriod: Int? = null,
    @SerialName("completiondiscussions")
    val completionDiscussions: Int? = null,
    @SerialName("completionreplies")
    val completionReplies: Int? = null,
    @SerialName("completionposts")
    val completionPosts: Int? = null,
    @SerialName("cmid")
    val cmId: Int? = null,
    @SerialName("numdiscussions")
    val numDiscussions: Int? = null,
    @SerialName("cancreatediscussions")
    val canCreateDiscussions: Boolean? = null,
    @SerialName("lockdiscussionafter")
    val lockDiscussionAfter: Long? = null,
    @SerialName("istracked")
    val isTracked: Boolean? = null
)

/**
 * Represents a forum discussion.
 */
@Serializable
data class ElearningForumDiscussion(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("groupid")
    val groupId: Int? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("usermodified")
    val userModified: Int? = null,
    @SerialName("timestart")
    val timeStart: Long? = null,
    @SerialName("timeend")
    val timeEnd: Long? = null,
    @SerialName("discussion")
    val discussion: Int? = null,
    @SerialName("parent")
    val parent: Int? = null,
    @SerialName("userid")
    val userId: Int? = null,
    @SerialName("created")
    val created: Long? = null,
    @SerialName("modified")
    val modified: Long? = null,
    @SerialName("mailed")
    val mailed: Int? = null,
    @SerialName("subject")
    val subject: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageformat")
    val messageFormat: Int? = null,
    @SerialName("messagetrust")
    val messageTrust: Int? = null,
    @SerialName("attachment")
    val attachment: Boolean? = null,
    @SerialName("totalscore")
    val totalScore: Int? = null,
    @SerialName("mailnow")
    val mailNow: Int? = null,
    @SerialName("userfullname")
    val userFullName: String? = null,
    @SerialName("usermodifiedfullname")
    val userModifiedFullName: String? = null,
    @SerialName("userpictureurl")
    val userPictureUrl: String? = null,
    @SerialName("usermodifiedpictureurl")
    val userModifiedPictureUrl: String? = null,
    @SerialName("numreplies")
    val numReplies: Int? = null,
    @SerialName("numunread")
    val numUnread: Int? = null,
    @SerialName("pinned")
    val pinned: Boolean? = null,
    @SerialName("locked")
    val locked: Boolean? = null,
    @SerialName("starred")
    val starred: Boolean? = null,
    @SerialName("canreply")
    val canReply: Boolean? = null,
    @SerialName("canlock")
    val canLock: Boolean? = null,
    @SerialName("canfavourite")
    val canFavourite: Boolean? = null
)

/**
 * Represents a forum post.
 */
@Serializable
data class ElearningForumPost(
    @SerialName("id")
    val id: Int,
    @SerialName("discussionid")
    val discussionId: Int? = null,
    @SerialName("parentid")
    val parentId: Int? = null,
    @SerialName("hasparent")
    val hasParent: Boolean? = null,
    @SerialName("timecreated")
    val timeCreated: Long? = null,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("subject")
    val subject: String? = null,
    @SerialName("replysubject")
    val replySubject: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageformat")
    val messageFormat: Int? = null,
    @SerialName("author")
    val author: ElearningForumPostAuthor? = null,
    @SerialName("attachments")
    val attachments: List<ElearningFile>? = null,
    @SerialName("messageinlinefiles")
    val messageInlineFiles: List<ElearningFile>? = null,
    @SerialName("haswordcount")
    val hasWordCount: Boolean? = null,
    @SerialName("wordcount")
    val wordCount: Int? = null,
    @SerialName("charcount")
    val charCount: Int? = null,
    @SerialName("capabilities")
    val capabilities: ElearningForumPostCapabilities? = null,
    @SerialName("unread")
    val unread: Boolean? = null,
    @SerialName("isdeleted")
    val isDeleted: Boolean? = null,
    @SerialName("isprivatereply")
    val isPrivateReply: Boolean? = null
)

@Serializable
data class ElearningForumPostAuthor(
    @SerialName("id")
    val id: Int,
    @SerialName("fullname")
    val fullName: String? = null,
    @SerialName("isdeleted")
    val isDeleted: Boolean? = null,
    @SerialName("groups")
    val groups: List<ElearningUserGroup>? = null,
    @SerialName("urls")
    val urls: ElearningForumAuthorUrls? = null
)

@Serializable
data class ElearningForumAuthorUrls(
    @SerialName("profile")
    val profile: String? = null,
    @SerialName("profileimage")
    val profileImage: String? = null
)

@Serializable
data class ElearningForumPostCapabilities(
    @SerialName("view")
    val view: Boolean? = null,
    @SerialName("edit")
    val edit: Boolean? = null,
    @SerialName("delete")
    val delete: Boolean? = null,
    @SerialName("split")
    val split: Boolean? = null,
    @SerialName("reply")
    val reply: Boolean? = null,
    @SerialName("selfenrol")
    val selfEnrol: Boolean? = null,
    @SerialName("export")
    val export: Boolean? = null,
    @SerialName("controlreadstatus")
    val controlReadStatus: Boolean? = null,
    @SerialName("canreplyprivately")
    val canReplyPrivately: Boolean? = null
)
