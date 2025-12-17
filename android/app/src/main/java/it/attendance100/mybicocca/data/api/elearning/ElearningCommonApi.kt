package it.attendance100.mybicocca.data.api.elearning

import it.attendance100.mybicocca.data.remote.dto.elearning.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * # Elearning Common API
 *
 * Handles common tools, configuration, and plugin support (Tool Mobile).
 *
 * ## Key Features
 *
 * - **Config:** Get site configuration and public settings.
 * - **Plugins:** Check mobile plugin support.
 * - **Functions:** Call external functions dynamically.
 * - **Content:** Get generic content (e.g., terms, help).
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Get site configuration
 * val config = commonApi.getConfig(
 *     GetConfigRequest()
 * )
 * ```
 */
interface ElearningCommonApi {

    /**
     * Returns the site configuration.
     *
     * @param request Configuration request.
     * @return Public configuration.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=tool_mobile_get_config")
    suspend fun getConfig(@Body request: GetConfigRequest): Response<ConfigResponse>

    /**
     * Returns a list of Moodle plugins supporting the mobile app.
     *
     * @param request Empty object (optional).
     * @return List of supported plugins.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=tool_mobile_get_plugins_supporting_mobile")
    suspend fun getPluginsSupportingMobile(@Body request: GetPluginsRequest? = null): Response<PluginsResponse>

    /**
     * Call multiple external functions in a single request.
     *
     * @param request List of function calls.
     * @return List of function responses.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=tool_mobile_call_external_functions")
    suspend fun callExternalFunctions(@Body request: CallExternalFunctionsRequest): Response<CallExternalFunctionsResponse>

    /**
     * Returns a piece of content to be displayed in the mobile app.
     *
     * @param request Component, method, and arguments.
     * @return Content data (HTML, JavaScript, etc.).
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=tool_mobile_get_content")
    suspend fun getContent(@Body request: GetContentRequest): Response<ContentResponse>
}
