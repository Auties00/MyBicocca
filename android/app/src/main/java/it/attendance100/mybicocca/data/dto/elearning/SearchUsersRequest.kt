package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

data class SearchUsersRequest(
    @SerializedName("courseid") val courseid: Int,
    @SerializedName("search") val search: String,
    @SerializedName("searchanywhere") val searchanywhere: Boolean,
    @SerializedName("page") val page: Int,
    @SerializedName("perpage") val perpage: Int
)
