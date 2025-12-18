package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningMessageAirnotifierAreNotificationPreferencesConfigured200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningMessageAirnotifierAreNotificationPreferencesConfiguredRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningMessageAirnotifierEnableDevice200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningMessageAirnotifierEnableDeviceRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningMessageAirnotifierGetUserDevices200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningMessageAirnotifierGetUserDevicesRequest

interface MessageAirnotifierApi {
    /**
     * POST message_airnotifier_are_notification_preferences_configured
     * Check if the users have notification preferences configured yet
     * Check if the users have notification preferences configured yet
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningMessageAirnotifierAreNotificationPreferencesConfiguredRequest 
     * @return [Call]<[ElearningMessageAirnotifierAreNotificationPreferencesConfigured200Response]>
     */
    @POST("message_airnotifier_are_notification_preferences_configured")
    fun messageAirnotifierAreNotificationPreferencesConfigured(@Body elearningMessageAirnotifierAreNotificationPreferencesConfiguredRequest: ElearningMessageAirnotifierAreNotificationPreferencesConfiguredRequest): Call<ElearningMessageAirnotifierAreNotificationPreferencesConfigured200Response>

    /**
     * POST message_airnotifier_enable_device
     * Enables or disables a registered user device so it can receive Push notifications
     * Enables or disables a registered user device so it can receive Push notifications
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningMessageAirnotifierEnableDeviceRequest 
     * @return [Call]<[ElearningMessageAirnotifierEnableDevice200Response]>
     */
    @POST("message_airnotifier_enable_device")
    fun messageAirnotifierEnableDevice(@Body elearningMessageAirnotifierEnableDeviceRequest: ElearningMessageAirnotifierEnableDeviceRequest): Call<ElearningMessageAirnotifierEnableDevice200Response>

    /**
     * POST message_airnotifier_get_user_devices
     * Return the list of mobile devices that are registered in Moodle for the given user
     * Return the list of mobile devices that are registered in Moodle for the given user
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningMessageAirnotifierGetUserDevicesRequest 
     * @return [Call]<[ElearningMessageAirnotifierGetUserDevices200Response]>
     */
    @POST("message_airnotifier_get_user_devices")
    fun messageAirnotifierGetUserDevices(@Body elearningMessageAirnotifierGetUserDevicesRequest: ElearningMessageAirnotifierGetUserDevicesRequest): Call<ElearningMessageAirnotifierGetUserDevices200Response>

    /**
     * POST message_airnotifier_is_system_configured
     * Check whether the airnotifier settings have been configured
     * Check whether the airnotifier settings have been configured
     * Responses:
     *  - 200: 0 if the system is not configured, 1 otherwise
     *  - 400: Invalid parameter value detected
     *
     * @return [Call]<[kotlin.Any]>
     */
    @POST("message_airnotifier_is_system_configured")
    fun messageAirnotifierIsSystemConfigured(): Call<kotlin.Any>

}
