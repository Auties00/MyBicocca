package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockStarredcoursesGetStarredCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse

interface BlockStarredcoursesApi {
    /**
     * POST block_starredcourses_get_starred_courses
     * Get users starred courses.
     * Get users starred courses.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockStarredcoursesGetStarredCoursesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_starredcourses_get_starred_courses")
    fun blockStarredcoursesGetStarredCourses(@Body elearningBlockStarredcoursesGetStarredCoursesRequest: ElearningBlockStarredcoursesGetStarredCoursesRequest): Call<kotlin.Any>

}
