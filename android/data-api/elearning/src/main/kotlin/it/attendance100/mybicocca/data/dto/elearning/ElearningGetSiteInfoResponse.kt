package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetSiteInfoResponse(
    @SerialName("advancedfeatures")
    val advancedFeatures: List<AdvancedFeature>,
    @SerialName("downloadfiles")
    val downloadFiles: Int,
    @SerialName("firstname")
    val firstName: String,
    @SerialName("fullname")
    val fullName: String,
    @SerialName("functions")
    val functions: List<Function>,
    @SerialName("lang")
    val lang: String,
    @SerialName("lastname")
    val lastName: String,
    @SerialName("limitconcurrentlogins")
    val limitConcurrentLogins: Int,
    @SerialName("mobilecssurl")
    val mobileCssUrl: String,
    @SerialName("policyagreed")
    val policyAgreed: Int,
    @SerialName("release")
    val release: String,
    @SerialName("sitecalendartype")
    val siteCalendarType: String,
    @SerialName("siteid")
    val siteId: Int,
    @SerialName("sitename")
    val siteName: String,
    @SerialName("siteurl")
    val siteUrl: String,
    @SerialName("theme")
    val theme: String,
    @SerialName("uploadfiles")
    val uploadFiles: Int,
    @SerialName("usercalendartype")
    val userCalendarType: String,
    @SerialName("usercanmanageownfiles")
    val userCanManageOwnFiles: Boolean,
    @SerialName("userhomepage")
    val userHomePage: Int,
    @SerialName("userid")
    val userId: Int,
    @SerialName("userissiteadmin")
    val userIsSiteAdmin: Boolean,
    @SerialName("usermaxuploadfilesize")
    val userMaxUploadFileSize: Int,
    @SerialName("username")
    val username: String,
    @SerialName("userpictureurl")
    val userPictureUrl: String,
    @SerialName("userprivateaccesskey")
    val userPrivateAccessKey: String,
    @SerialName("userquota")
    val userQuota: Int,
    @SerialName("version")
    val version: String
) : ElearningResponse {

    @Serializable
    data class AdvancedFeature(
        @SerialName("name")
        val name: String,
        @SerialName("value")
        val value: Int
    )

    @Serializable
    data class Function(
        @SerialName("name")
        val name: String,
        @SerialName("version")
        val version: String
    )
}