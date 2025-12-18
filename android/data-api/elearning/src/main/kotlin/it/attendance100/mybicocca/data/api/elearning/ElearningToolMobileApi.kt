package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileCallExternalFunctions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileCallExternalFunctionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileGetAutologinKey200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileGetAutologinKeyRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileGetConfig200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileGetConfigRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileGetContent200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileGetContentRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileGetPluginsSupportingMobile200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileGetPublicConfig200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileGetTokensForQrLogin200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileGetTokensForQrLoginRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileValidateSubscriptionKey200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolMobileValidateSubscriptionKeyRequest

interface ToolMobileApi {
    /**
     * POST tool_mobile_call_external_functions
     * Call multiple external functions and return all responses.
     * Call multiple external functions and return all responses.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolMobileCallExternalFunctionsRequest 
     * @return [Call]<[ElearningToolMobileCallExternalFunctions200Response]>
     */
    @POST("tool_mobile_call_external_functions")
    fun toolMobileCallExternalFunctions(@Body elearningToolMobileCallExternalFunctionsRequest: ElearningToolMobileCallExternalFunctionsRequest): Call<ElearningToolMobileCallExternalFunctions200Response>

    /**
     * POST tool_mobile_get_autologin_key
     * Creates an auto-login key for the current user.                             Is created only in https sites and is restricted by time, ip address and only works if the request                             comes from the Moodle mobile or desktop app.
     * Creates an auto-login key for the current user.                             Is created only in https sites and is restricted by time, ip address and only works if the request                             comes from the Moodle mobile or desktop app.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolMobileGetAutologinKeyRequest 
     * @return [Call]<[ElearningToolMobileGetAutologinKey200Response]>
     */
    @POST("tool_mobile_get_autologin_key")
    fun toolMobileGetAutologinKey(@Body elearningToolMobileGetAutologinKeyRequest: ElearningToolMobileGetAutologinKeyRequest): Call<ElearningToolMobileGetAutologinKey200Response>

    /**
     * POST tool_mobile_get_config
     * Returns a list of the site configurations, filtering by section.
     * Returns a list of the site configurations, filtering by section.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolMobileGetConfigRequest 
     * @return [Call]<[ElearningToolMobileGetConfig200Response]>
     */
    @POST("tool_mobile_get_config")
    fun toolMobileGetConfig(@Body elearningToolMobileGetConfigRequest: ElearningToolMobileGetConfigRequest): Call<ElearningToolMobileGetConfig200Response>

    /**
     * POST tool_mobile_get_content
     * Returns a piece of content to be displayed in the Mobile app.
     * Returns a piece of content to be displayed in the Mobile app.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolMobileGetContentRequest 
     * @return [Call]<[ElearningToolMobileGetContent200Response]>
     */
    @POST("tool_mobile_get_content")
    fun toolMobileGetContent(@Body elearningToolMobileGetContentRequest: ElearningToolMobileGetContentRequest): Call<ElearningToolMobileGetContent200Response>

    /**
     * POST tool_mobile_get_plugins_supporting_mobile
     * Returns a list of Moodle plugins supporting the mobile app.
     * Returns a list of Moodle plugins supporting the mobile app.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @return [Call]<[ElearningToolMobileGetPluginsSupportingMobile200Response]>
     */
    @POST("tool_mobile_get_plugins_supporting_mobile")
    fun toolMobileGetPluginsSupportingMobile(): Call<ElearningToolMobileGetPluginsSupportingMobile200Response>

    /**
     * POST tool_mobile_get_public_config
     * Returns a list of the site public settings, those not requiring authentication.
     * Returns a list of the site public settings, those not requiring authentication.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @return [Call]<[ElearningToolMobileGetPublicConfig200Response]>
     */
    @POST("tool_mobile_get_public_config")
    fun toolMobileGetPublicConfig(): Call<ElearningToolMobileGetPublicConfig200Response>

    /**
     * POST tool_mobile_get_tokens_for_qr_login
     * Returns a WebService token (and private token) for QR login.
     * Returns a WebService token (and private token) for QR login.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolMobileGetTokensForQrLoginRequest 
     * @return [Call]<[ElearningToolMobileGetTokensForQrLogin200Response]>
     */
    @POST("tool_mobile_get_tokens_for_qr_login")
    fun toolMobileGetTokensForQrLogin(@Body elearningToolMobileGetTokensForQrLoginRequest: ElearningToolMobileGetTokensForQrLoginRequest): Call<ElearningToolMobileGetTokensForQrLogin200Response>

    /**
     * POST tool_mobile_validate_subscription_key
     * Check if the given site subscription key is valid.
     * Check if the given site subscription key is valid.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolMobileValidateSubscriptionKeyRequest 
     * @return [Call]<[ElearningToolMobileValidateSubscriptionKey200Response]>
     */
    @POST("tool_mobile_validate_subscription_key")
    fun toolMobileValidateSubscriptionKey(@Body elearningToolMobileValidateSubscriptionKeyRequest: ElearningToolMobileValidateSubscriptionKeyRequest): Call<ElearningToolMobileValidateSubscriptionKey200Response>

}
