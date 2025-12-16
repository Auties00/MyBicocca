package it.attendance100.mybicocca.data.remote.dto.bicoccapp


import com.google.gson.annotations.SerializedName

/**
 *
 *
 * @param matricId
 * @param selected
 * @param oldCode
 * @param matricCode
 * @param studentId
 */


data class UserProfileCareersInner(

    @SerializedName("matricId")
    val matricId: Int? = null,

    @SerializedName("selected")
    val selected: Boolean? = null,

    @SerializedName("oldCode")
    val oldCode: Boolean? = null,

    @SerializedName("matricCode")
    val matricCode: String? = null,

    @SerializedName("studentId")
    val studentId: Int? = null

)

