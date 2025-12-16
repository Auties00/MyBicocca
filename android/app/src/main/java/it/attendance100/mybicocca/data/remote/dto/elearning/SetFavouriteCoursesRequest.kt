package it.attendance100.mybicocca.data.remote.dto.elearning

import com.google.gson.annotations.SerializedName

data class SetFavouriteCoursesRequest(
    @SerializedName("courses") val courses: List<SetFavouriteCoursesCourse>
)

data class SetFavouriteCoursesCourse(
    @SerializedName("id") val id: Int,
    @SerializedName("favourite") val favourite: Boolean
)
