package it.attendance100.mybicocca.data.remote.dto.elearning

import com.google.gson.annotations.SerializedName

enum class CourseSearchFieldEnum {
    @SerializedName("id") ID,
    @SerializedName("ids") IDS,
    @SerializedName("shortname") SHORTNAME,
    @SerializedName("idnumber") IDNUMBER,
    @SerializedName("category") CATEGORY,
    @SerializedName("sectionid") SECTIONID
}