package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SearchCoursesRequest(
    @SerializedName("criterianame") val criteriaName: String,
    @SerializedName("criteriavalue") val criteriaValue: String,
    @SerializedName("page") val page: Int? = null,
    @SerializedName("perpage") val perPage: Int? = null,
    @SerializedName("limittoenrolled") val limitToEnrolled: Boolean? = null
)