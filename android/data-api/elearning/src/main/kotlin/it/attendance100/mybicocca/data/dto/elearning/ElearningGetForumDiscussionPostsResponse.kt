package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val warnings: List<ElearningWarning> = emptyList()
) : ElearningResponse

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

@Serializable
data class ElearningRatingScale(
    @SerialName("id")
    val id: Int,
    @SerialName("courseid")
    val courseId: Int? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("max")
    val max: Int? = null,
    @SerialName("isnumeric")
    val isNumeric: Boolean? = null,
    @SerialName("items")
    val items: List<ElearningScaleItem>? = null
)

@Serializable
data class ElearningScaleItem(
    @SerialName("value")
    val value: Int,
    @SerialName("name")
    val name: String? = null
)

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
    val aggregateStr: String? = null,
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
