package it.attendance100.mybicocca.data.remote.elearning.dto

import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Request to retrieve forums for specified courses.
 *
 * @property courseIds The list of course identifiers to retrieve forums for.
 */
@Serializable
class ElearningGetForumsRequest(
    private val courseIds: List<Int>
) : ElearningRequest<ElearningGetForumsResponse> {
    override val functionName = "mod_forum_get_forums_by_courses"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        courseIds.forEachIndexed { index, id ->
            formData.append("courseids[$index]", id.toString())
        }
    }
}

/**
 * Response containing forums for the requested courses.
 *
 * @property items The list of forums found.
 */
@Serializable
data class ElearningGetForumsResponse(
    override val items: List<ElearningForum>
) : ElearningListResponse<ElearningForum> {
    /**
     * Alias for [items] providing a more descriptive name.
     */
    val forums: List<ElearningForum> get() = items
}

/**
 * Request to retrieve discussions from a forum.
 *
 * @property forumId The unique identifier of the forum.
 * @property sortOrder The order to sort discussions by.
 * @property page The page number to retrieve (0-indexed).
 * @property perPage The number of discussions per page.
 * @property groupId The group identifier to filter discussions.
 */
@Serializable
class ElearningGetForumDiscussionsRequest(
    private val forumId: Int,
    private val sortOrder: ElearningDiscussionSortOrder,
    private val page: Int,
    private val perPage: Int,
    private val groupId: Int
) : ElearningRequest<ElearningGetForumDiscussionsResponse> {
    override val functionName = "mod_forum_get_forum_discussions"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("forumid", forumId.toString())
        formData.append("sortorder", sortOrder.id.toString())
        formData.append("page", page.toString())
        formData.append("perpage", perPage.toString())
        formData.append("groupid", groupId.toString())
    }
}

/**
 * Response containing discussions from a forum.
 *
 * @property discussions The list of discussions.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetForumDiscussionsResponse(
    @SerialName("discussions")
    val discussions: List<ElearningForumDiscussion> = emptyList(),
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to retrieve posts from a discussion.
 *
 * @property discussionId The unique identifier of the discussion.
 * @property includeInlineAttachments Whether to include inline attachments in posts.
 */
@Serializable
class ElearningGetForumDiscussionPostsRequest(
    private val discussionId: Int,
    private val includeInlineAttachments: Boolean
) : ElearningRequest<ElearningGetForumDiscussionPostsResponse> {
    override val functionName = "mod_forum_get_discussion_posts"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("discussionid", discussionId.toString())
        if (includeInlineAttachments) formData.append("includeinlineattachments", "1")
    }
}

/**
 * Response containing posts from a discussion.
 *
 * @property posts The list of posts in the discussion.
 * @property forumId The identifier of the forum containing the discussion.
 * @property courseId The identifier of the course containing the forum.
 * @property ratingInfo Information about ratings for the posts.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningGetForumDiscussionPostsResponse(
    @SerialName("posts")
    val posts: List<ElearningForumPost> = emptyList(),
    @SerialName("forumid")
    val forumId: Int? = null,
    @SerialName("courseid")
    val courseId: Int? = null,
    @SerialName("ratinginfo")
    val ratingInfo: ElearningRatingInfo? = null,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to create a new discussion in a forum.
 *
 * @property forumId The unique identifier of the forum.
 * @property subject The subject of the new discussion.
 * @property message The message content of the first post.
 * @property groupId Optional group identifier for group-specific discussions.
 * @property options Optional list of additional options.
 */
@Serializable
class ElearningAddForumDiscussionRequest(
    private val forumId: Int,
    private val subject: String,
    private val message: String,
    private val groupId: Int? = null,
    private val options: List<ElearningDiscussionOption>? = null
) : ElearningRequest<ElearningAddForumDiscussionResponse> {
    override val functionName = "mod_forum_add_discussion"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("forumid", forumId.toString())
        formData.append("subject", subject)
        formData.append("message", message)
        groupId?.let { formData.append("groupid", it.toString()) }
        options?.forEachIndexed { index, option ->
            formData.append("options[$index][name]", option.name)
            formData.append("options[$index][value]", option.value)
        }
    }
}

/**
 * Response after creating a new discussion.
 *
 * @property discussionId The unique identifier of the created discussion.
 * @property warnings Any warnings generated during the request.
 */
@Serializable
data class ElearningAddForumDiscussionResponse(
    @SerialName("discussionid")
    val discussionId: Int,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Request to add a reply post to a discussion.
 *
 * @property postId The identifier of the post to reply to.
 * @property subject The subject of the reply.
 * @property message The message content of the reply.
 * @property options Optional list of additional options.
 */
@Serializable
class ElearningAddForumPostRequest(
    private val postId: Int,
    private val subject: String,
    private val message: String,
    private val options: List<ElearningDiscussionOption>? = null
) : ElearningRequest<ElearningAddForumPostResponse> {
    override val functionName = "mod_forum_add_discussion_post"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("postid", postId.toString())
        formData.append("subject", subject)
        formData.append("message", message)
        options?.forEachIndexed { index, option ->
            formData.append("options[$index][name]", option.name)
            formData.append("options[$index][value]", option.value)
        }
    }
}

/**
 * Response after adding a post to a discussion.
 *
 * @property postId The unique identifier of the created post.
 * @property warnings Any warnings generated during the request.
 * @property post The created post details.
 * @property messages Any messages returned by the operation.
 */
@Serializable
data class ElearningAddForumPostResponse(
    @SerialName("postid")
    val postId: Int,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList(),
    @SerialName("post")
    val post: ElearningForumPost? = null,
    @SerialName("messages")
    val messages: List<ElearningForumMessage>? = null
) : ElearningResponse

/**
 * Represents a forum activity in a course.
 *
 * @property id The unique identifier of the forum.
 * @property courseId The identifier of the course containing this forum.
 * @property type The type of forum (e.g., "general", "single", "qanda", "blog", "news").
 * @property name The name of the forum.
 * @property introduction The introduction text for the forum.
 * @property introductionFormat The format of the introduction.
 * @property introductionFiles Files attached to the introduction.
 * @property dueDateTimestamp Unix timestamp of the forum due date.
 * @property cutOffDateTimestamp Unix timestamp after which posts are not accepted.
 * @property assessed Whether posts are assessed (graded).
 * @property assessTimeStartTimestamp Unix timestamp when assessment period starts.
 * @property assessTimeFinishTimestamp Unix timestamp when assessment period ends.
 * @property scale The scale used for assessment.
 * @property maxBytes Maximum file size for attachments in bytes.
 * @property maxAttachments Maximum number of attachments allowed.
 * @property forceSubscribe Subscription mode (0 = optional, 1 = forced, 2 = auto, 3 = disabled).
 * @property trackingType Read tracking mode.
 * @property rssType RSS feed type.
 * @property rssArticles Number of articles in RSS feed.
 * @property modifiedTimestamp Unix timestamp when the forum was last modified.
 * @property warnAfterPosts Number of posts after which to warn the user.
 * @property blockAfterPosts Number of posts after which to block the user.
 * @property blockPeriodSeconds Period in seconds for the blocking threshold.
 * @property completionDiscussions Discussions required for completion.
 * @property completionReplies Replies required for completion.
 * @property completionPosts Posts required for completion.
 * @property courseModuleId The course module identifier.
 * @property numberOfDiscussions Total number of discussions in the forum.
 * @property canCreateDiscussions Whether the user can create new discussions.
 * @property lockDiscussionAfterTimestamp Unix timestamp after which discussions are locked.
 * @property isTracked Whether the user has tracking enabled for this forum.
 */
@Serializable
data class ElearningForum(
    @SerialName("id")
    val id: Int,
    @SerialName("course")
    val courseId: Int,
    @SerialName("type")
    val type: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("intro")
    val introduction: String? = null,
    @SerialName("introformat")
    val introductionFormat: Int? = null,
    @SerialName("introfiles")
    val introductionFiles: List<ElearningFile>? = null,
    @SerialName("duedate")
    val dueDateTimestamp: Long? = null,
    @SerialName("cutoffdate")
    val cutOffDateTimestamp: Long? = null,
    @SerialName("assessed")
    val assessed: Int? = null,
    @SerialName("assesstimestart")
    val assessTimeStartTimestamp: Long? = null,
    @SerialName("assesstimefinish")
    val assessTimeFinishTimestamp: Long? = null,
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
    val modifiedTimestamp: Long? = null,
    @SerialName("warnafter")
    val warnAfterPosts: Int? = null,
    @SerialName("blockafter")
    val blockAfterPosts: Int? = null,
    @SerialName("blockperiod")
    val blockPeriodSeconds: Int? = null,
    @SerialName("completiondiscussions")
    val completionDiscussions: Int? = null,
    @SerialName("completionreplies")
    val completionReplies: Int? = null,
    @SerialName("completionposts")
    val completionPosts: Int? = null,
    @SerialName("cmid")
    val courseModuleId: Int? = null,
    @SerialName("numdiscussions")
    val numberOfDiscussions: Int? = null,
    @SerialName("cancreatediscussions")
    val canCreateDiscussions: Boolean? = null,
    @SerialName("lockdiscussionafter")
    val lockDiscussionAfterTimestamp: Long? = null,
    @SerialName("istracked")
    val isTracked: Boolean? = null
)

/**
 * Represents a discussion within a forum.
 *
 * @property id The unique identifier of the discussion.
 * @property name The name/title of the discussion.
 * @property groupId The group identifier if this is a group discussion.
 * @property modifiedTimestamp Unix timestamp when the discussion was last modified.
 * @property userModifiedId The identifier of the user who last modified the discussion.
 * @property startTimestamp Unix timestamp when the discussion becomes visible.
 * @property endTimestamp Unix timestamp when the discussion is no longer visible.
 * @property discussionId The discussion identifier (may differ from id in some contexts).
 * @property parentPostId The identifier of the parent post.
 * @property authorUserId The identifier of the user who created the discussion.
 * @property createdTimestamp Unix timestamp when the discussion was created.
 * @property modifiedAtTimestamp Unix timestamp when the discussion was modified.
 * @property mailed Whether the discussion has been mailed.
 * @property subject The subject of the first post.
 * @property message The message content of the first post.
 * @property messageFormat The format of the message.
 * @property messageTrust The trust level of the message.
 * @property hasAttachment Whether the discussion has attachments.
 * @property totalScore The total score from ratings.
 * @property mailNow Whether to mail immediately.
 * @property authorFullName The full name of the author.
 * @property modifierFullName The full name of the last modifier.
 * @property authorPictureUrl URL to the author's profile picture.
 * @property modifierPictureUrl URL to the modifier's profile picture.
 * @property numberOfReplies The number of replies to the discussion.
 * @property numberOfUnread The number of unread posts.
 * @property pinned Whether the discussion is pinned.
 * @property locked Whether the discussion is locked.
 * @property starred Whether the discussion is starred by the user.
 * @property canReply Whether the user can reply to the discussion.
 * @property canLock Whether the user can lock the discussion.
 * @property canFavourite Whether the user can favourite the discussion.
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
    val modifiedTimestamp: Long? = null,
    @SerialName("usermodified")
    val userModifiedId: Int? = null,
    @SerialName("timestart")
    val startTimestamp: Long? = null,
    @SerialName("timeend")
    val endTimestamp: Long? = null,
    @SerialName("discussion")
    val discussionId: Int? = null,
    @SerialName("parent")
    val parentPostId: Int? = null,
    @SerialName("userid")
    val authorUserId: Int? = null,
    @SerialName("created")
    val createdTimestamp: Long? = null,
    @SerialName("modified")
    val modifiedAtTimestamp: Long? = null,
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
    val hasAttachment: Boolean? = null,
    @SerialName("totalscore")
    val totalScore: Int? = null,
    @SerialName("mailnow")
    val mailNow: Int? = null,
    @SerialName("userfullname")
    val authorFullName: String? = null,
    @SerialName("usermodifiedfullname")
    val modifierFullName: String? = null,
    @SerialName("userpictureurl")
    val authorPictureUrl: String? = null,
    @SerialName("usermodifiedpictureurl")
    val modifierPictureUrl: String? = null,
    @SerialName("numreplies")
    val numberOfReplies: Int? = null,
    @SerialName("numunread")
    val numberOfUnread: Int? = null,
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
 * Represents a post within a forum discussion.
 *
 * @property id The unique identifier of the post.
 * @property discussionId The identifier of the discussion containing this post.
 * @property parentId The identifier of the parent post (for replies).
 * @property hasParent Whether this post has a parent (is a reply).
 * @property createdTimestamp Unix timestamp when the post was created.
 * @property modifiedTimestamp Unix timestamp when the post was last modified.
 * @property subject The subject of the post.
 * @property replySubject The suggested subject for replies.
 * @property message The message content of the post.
 * @property messageFormat The format of the message.
 * @property author Information about the post author.
 * @property attachments Files attached to the post.
 * @property messageInlineFiles Inline files embedded in the message.
 * @property hasWordCount Whether word count is available.
 * @property wordCount The number of words in the message.
 * @property characterCount The number of characters in the message.
 * @property capabilities The user's capabilities for this post.
 * @property unread Whether the post is unread by the current user.
 * @property isDeleted Whether the post has been deleted.
 * @property isPrivateReply Whether this is a private reply.
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
    val createdTimestamp: Long? = null,
    @SerialName("timemodified")
    val modifiedTimestamp: Long? = null,
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
    val characterCount: Int? = null,
    @SerialName("capabilities")
    val capabilities: ElearningForumPostCapabilities? = null,
    @SerialName("unread")
    val unread: Boolean? = null,
    @SerialName("isdeleted")
    val isDeleted: Boolean? = null,
    @SerialName("isprivatereply")
    val isPrivateReply: Boolean? = null
)

/**
 * Information about the author of a forum post.
 *
 * @property id The unique identifier of the author.
 * @property fullName The full name of the author.
 * @property isDeleted Whether the author's account has been deleted.
 * @property groups The groups the author belongs to.
 * @property urls URLs related to the author.
 */
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

/**
 * URLs related to a forum post author.
 *
 * @property profile URL to the author's profile page.
 * @property profileImage URL to the author's profile image.
 */
@Serializable
data class ElearningForumAuthorUrls(
    @SerialName("profile")
    val profile: String? = null,
    @SerialName("profileimage")
    val profileImage: String? = null
)

/**
 * Capabilities the user has for a specific forum post.
 *
 * @property view Whether the user can view the post.
 * @property edit Whether the user can edit the post.
 * @property delete Whether the user can delete the post.
 * @property split Whether the user can split the post into a new discussion.
 * @property reply Whether the user can reply to the post.
 * @property selfEnrol Whether the user can self-enrol.
 * @property export Whether the user can export the post.
 * @property controlReadStatus Whether the user can control read status.
 * @property canReplyPrivately Whether the user can reply privately.
 */
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

/**
 * A name-value pair for discussion options.
 *
 * @property name The name of the option.
 * @property value The value of the option.
 */
@Serializable
data class ElearningDiscussionOption(
    val name: String,
    val value: String
)

/**
 * A message returned by forum operations.
 *
 * @property type The type of message.
 * @property message The message content.
 */
@Serializable
data class ElearningForumMessage(
    @SerialName("type")
    val type: String? = null,
    @SerialName("message")
    val message: String? = null
)

/**
 * Information about ratings for forum posts.
 *
 * @property contextId The context identifier for ratings.
 * @property component The component name for ratings.
 * @property ratingArea The area name for ratings.
 * @property canViewAll Whether the user can view all ratings.
 * @property canViewAny Whether the user can view any ratings.
 * @property scales The rating scales available.
 * @property ratings The list of ratings.
 */
@Serializable
data class ElearningRatingInfo(
    @SerialName("contextid")
    val contextId: Int? = null,
    @SerialName("component")
    val component: String? = null,
    @SerialName("ratingarea")
    val ratingArea: String? = null,
    @SerialName("canviewall")
    val canViewAll: Boolean? = null,
    @SerialName("canviewany")
    val canViewAny: Boolean? = null,
    @SerialName("scales")
    val scales: List<ElearningRatingScale>? = null,
    @SerialName("ratings")
    val ratings: List<ElearningRating>? = null
)

/**
 * A rating scale used for forum posts.
 *
 * @property id The unique identifier of the scale.
 * @property courseId The course identifier.
 * @property name The name of the scale.
 * @property maximumValue The maximum value on the scale.
 * @property isNumeric Whether the scale uses numeric values.
 * @property items The items/options in the scale.
 */
@Serializable
data class ElearningRatingScale(
    @SerialName("id")
    val id: Int,
    @SerialName("courseid")
    val courseId: Int? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("max")
    val maximumValue: Int? = null,
    @SerialName("isnumeric")
    val isNumeric: Boolean? = null,
    @SerialName("items")
    val items: List<ElearningScaleItem>? = null
)

/**
 * An item in a rating scale.
 *
 * @property value The numeric value of the item.
 * @property name The display name of the item.
 */
@Serializable
data class ElearningScaleItem(
    @SerialName("value")
    val value: Int,
    @SerialName("name")
    val name: String? = null
)

/**
 * A rating on a forum post.
 *
 * @property itemId The identifier of the rated item.
 * @property scaleId The identifier of the scale used.
 * @property userId The identifier of the user who rated.
 * @property aggregate The aggregated rating value.
 * @property aggregateString The aggregated rating as a string.
 * @property aggregateLabel The label for the aggregate.
 * @property count The number of ratings.
 * @property rating The user's own rating.
 * @property canRate Whether the user can rate.
 * @property canViewAggregate Whether the user can view the aggregate.
 */
@Serializable
data class ElearningRating(
    @SerialName("itemid")
    val itemId: Int,
    @SerialName("scaleid")
    val scaleId: Int? = null,
    @SerialName("userid")
    val userId: Int? = null,
    @SerialName("aggregate")
    val aggregate: Double? = null,
    @SerialName("aggregatestr")
    val aggregateString: String? = null,
    @SerialName("aggregatelabel")
    val aggregateLabel: String? = null,
    @SerialName("count")
    val count: Int? = null,
    @SerialName("rating")
    val rating: Int? = null,
    @SerialName("canrate")
    val canRate: Boolean? = null,
    @SerialName("canviewaggregate")
    val canViewAggregate: Boolean? = null
)

/**
 * Sort order options for forum discussions.
 *
 * @property id The numeric identifier for the sort order.
 */
@Serializable(with = ElearningDiscussionSortOrderSerializer::class)
enum class ElearningDiscussionSortOrder(val id: Int) {
    /** Sort by last post date, newest first. */
    LAST_POST_DESCENDING(1),
    /** Sort by last post date, oldest first. */
    LAST_POST_ASCENDING(2),
    /** Sort by creation date, newest first. */
    CREATION_DATE_DESCENDING(3),
    /** Sort by creation date, oldest first. */
    CREATION_DATE_ASCENDING(4),
    /** Sort by reply count, most replies first. */
    REPLY_COUNT_DESCENDING(5),
    /** Sort by reply count, fewest replies first. */
    REPLY_COUNT_ASCENDING(6);

    companion object {
        /**
         * Finds a sort order by its numeric identifier.
         *
         * @param id The numeric identifier to search for.
         * @return The matching sort order, or null if not found.
         */
        fun fromId(id: Int): ElearningDiscussionSortOrder? = entries.find { it.id == id }
    }
}

/**
 * Serializer for [ElearningDiscussionSortOrder] that encodes/decodes as integers.
 */
object ElearningDiscussionSortOrderSerializer : KSerializer<ElearningDiscussionSortOrder> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ElearningDiscussionSortOrder", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ElearningDiscussionSortOrder) {
        encoder.encodeInt(value.id)
    }

    override fun deserialize(decoder: Decoder): ElearningDiscussionSortOrder {
        val id = decoder.decodeInt()
        return ElearningDiscussionSortOrder.fromId(id)
            ?: throw IllegalArgumentException("Unknown ElearningDiscussionSortOrder id: $id")
    }
}
