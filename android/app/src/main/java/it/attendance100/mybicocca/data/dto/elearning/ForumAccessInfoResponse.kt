package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ForumAccessInfoResponse(
    @SerializedName("canaddinstance") val canAddInstance: Boolean? = null,
    @SerializedName("canviewdiscussion") val canViewDiscussion: Boolean? = null,
    @SerializedName("canviewhiddentimedposts") val canViewHiddenTimedPosts: Boolean? = null,
    @SerializedName("canstartnewdiscussion") val canStartNewDiscussion: Boolean? = null,
    @SerializedName("canreplypost") val canReplyPost: Boolean? = null,
    @SerializedName("canaddnews") val canAddNews: Boolean? = null,
    @SerializedName("canreplynews") val canReplyNews: Boolean? = null,
    @SerializedName("canviewrating") val canViewRating: Boolean? = null,
    @SerializedName("canviewanyrating") val canViewAnyRating: Boolean? = null,
    @SerializedName("canviewallratings") val canViewAllRatings: Boolean? = null,
    @SerializedName("canrate") val canRate: Boolean? = null,
    @SerializedName("canpostprivatereply") val canPostPrivateReply: Boolean? = null,
    @SerializedName("canreadprivatereplies") val canReadPrivateReplies: Boolean? = null,
    @SerializedName("canmod") val canMod: Boolean? = null,
    @SerializedName("canexportdiscussion") val canExportDiscussion: Boolean? = null,
    @SerializedName("canexportforum") val canExportForum: Boolean? = null,
    @SerializedName("canexportownpost") val canExportOwnPost: Boolean? = null,
    @SerializedName("cansplitdiscussions") val canSplitDiscussions: Boolean? = null,
    @SerializedName("canmanagesubscriptions") val canManageSubscriptions: Boolean? = null,
    @SerializedName("canmovefromanyforum") val canMoveFromAnyForum: Boolean? = null,
    @SerializedName("canmovetotopic") val canMoveToTopic: Boolean? = null,
    @SerializedName("canpindiscussions") val canPinDiscussions: Boolean? = null,
    @SerializedName("cangrade") val canGrade: Boolean? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
