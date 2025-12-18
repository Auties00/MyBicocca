package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreMessageGetUnreadConversationsCountRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningMessagePopupGetPopupNotifications200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningMessagePopupGetPopupNotificationsRequest

interface MessagePopupApi {
    /**
     * POST message_popup_get_popup_notifications
     * Retrieve a list of popup notifications for a user
     * Retrieve a list of popup notifications for a user
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningMessagePopupGetPopupNotificationsRequest 
     * @return [Call]<[ElearningMessagePopupGetPopupNotifications200Response]>
     */
    @POST("message_popup_get_popup_notifications")
    fun messagePopupGetPopupNotifications(@Body elearningMessagePopupGetPopupNotificationsRequest: ElearningMessagePopupGetPopupNotificationsRequest): Call<ElearningMessagePopupGetPopupNotifications200Response>

    /**
     * POST message_popup_get_unread_popup_notification_count
     * Retrieve the count of unread popup notifications for a given user
     * Retrieve the count of unread popup notifications for a given user
     * Responses:
     *  - 200: The count of unread popup notifications
     *  - 400: Invalid parameter value detected
     *
     * @param elearningCoreMessageGetUnreadConversationsCountRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("message_popup_get_unread_popup_notification_count")
    fun messagePopupGetUnreadPopupNotificationCount(@Body elearningCoreMessageGetUnreadConversationsCountRequest: ElearningCoreMessageGetUnreadConversationsCountRequest): Call<kotlin.Any>

}
