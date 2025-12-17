package it.attendance100.mybicocca.data.dto.elearning

import com.google.gson.annotations.SerializedName

enum class UserSearchFieldEnum {
    @SerializedName("id") ID,
    @SerializedName("idnumber") IDNUMBER,
    @SerializedName("username") USERNAME,
    @SerializedName("email") EMAIL
}