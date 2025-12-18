package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFolderGetFoldersByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModFolderViewFolderRequest

interface ModFolderApi {
    /**
     * POST mod_folder_get_folders_by_courses
     * Returns a list of folders in a provided list of courses, if no list is provided all folders that                             the user can view will be returned. Please note that this WS is not returning the folder contents.
     * Returns a list of folders in a provided list of courses, if no list is provided all folders that                             the user can view will be returned. Please note that this WS is not returning the folder contents.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest 
     * @return [Call]<[ElearningModFolderGetFoldersByCourses200Response]>
     */
    @POST("mod_folder_get_folders_by_courses")
    fun modFolderGetFoldersByCourses(@Body elearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest: ElearningModBigbluebuttonbnGetBigbluebuttonbnsByCoursesRequest): Call<ElearningModFolderGetFoldersByCourses200Response>

    /**
     * POST mod_folder_view_folder
     * Simulate the view.php web interface folder: trigger events, completion, etc...
     * Simulate the view.php web interface folder: trigger events, completion, etc...
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModFolderViewFolderRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_folder_view_folder")
    fun modFolderViewFolder(@Body elearningModFolderViewFolderRequest: ElearningModFolderViewFolderRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
