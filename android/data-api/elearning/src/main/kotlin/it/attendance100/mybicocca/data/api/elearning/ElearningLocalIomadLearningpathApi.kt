package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathActivateRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathAddcoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathAddusersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathCopypathRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathGetcoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathGetprospectivecoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathGetprospectiveusersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathGetusersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathOrdercoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathRemovecoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningLocalIomadLearningpathRemoveusersRequest

interface LocalIomadLearningpathApi {
    /**
     * POST local_iomad_learningpath_activate
     * Activates / deactivates learning path
     * Activates / deactivates learning path
     * Responses:
     *  - 200: True if active state set correctly
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathActivateRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_activate")
    fun localIomadLearningpathActivate(@Body elearningLocalIomadLearningpathActivateRequest: ElearningLocalIomadLearningpathActivateRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_addcourses
     * Add courses to learning path
     * Add courses to learning path
     * Responses:
     *  - 200: True if courses added correctly
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathAddcoursesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_addcourses")
    fun localIomadLearningpathAddcourses(@Body elearningLocalIomadLearningpathAddcoursesRequest: ElearningLocalIomadLearningpathAddcoursesRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_addusers
     * Add users to learning path
     * Add users to learning path
     * Responses:
     *  - 200: True if users added correctly
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathAddusersRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_addusers")
    fun localIomadLearningpathAddusers(@Body elearningLocalIomadLearningpathAddusersRequest: ElearningLocalIomadLearningpathAddusersRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_copypath
     * Copy a learning path
     * Copy a learning path
     * Responses:
     *  - 200: True if path copied correctly
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathCopypathRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_copypath")
    fun localIomadLearningpathCopypath(@Body elearningLocalIomadLearningpathCopypathRequest: ElearningLocalIomadLearningpathCopypathRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_deletepath
     * Completely delete a learning path
     * Completely delete a learning path
     * Responses:
     *  - 200: True if courses added correctly
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathCopypathRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_deletepath")
    fun localIomadLearningpathDeletepath(@Body elearningLocalIomadLearningpathCopypathRequest: ElearningLocalIomadLearningpathCopypathRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_getcourses
     * Read list of courses for given learning
     * Read list of courses for given learning
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathGetcoursesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_getcourses")
    fun localIomadLearningpathGetcourses(@Body elearningLocalIomadLearningpathGetcoursesRequest: ElearningLocalIomadLearningpathGetcoursesRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_getprospectivecourses
     * Read set of filtered courses for given company
     * Read set of filtered courses for given company
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathGetprospectivecoursesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_getprospectivecourses")
    fun localIomadLearningpathGetprospectivecourses(@Body elearningLocalIomadLearningpathGetprospectivecoursesRequest: ElearningLocalIomadLearningpathGetprospectivecoursesRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_getprospectiveusers
     * Get set of filtered users for given company
     * Get set of filtered users for given company
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathGetprospectiveusersRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_getprospectiveusers")
    fun localIomadLearningpathGetprospectiveusers(@Body elearningLocalIomadLearningpathGetprospectiveusersRequest: ElearningLocalIomadLearningpathGetprospectiveusersRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_getusers
     * Get users assigned to path
     * Get users assigned to path
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathGetusersRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_getusers")
    fun localIomadLearningpathGetusers(@Body elearningLocalIomadLearningpathGetusersRequest: ElearningLocalIomadLearningpathGetusersRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_ordercourses
     * Set sequence of courses in learning path
     * Set sequence of courses in learning path
     * Responses:
     *  - 200: True if courses ordered correctly
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathOrdercoursesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_ordercourses")
    fun localIomadLearningpathOrdercourses(@Body elearningLocalIomadLearningpathOrdercoursesRequest: ElearningLocalIomadLearningpathOrdercoursesRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_removecourses
     * Remove courses from learning path
     * Remove courses from learning path
     * Responses:
     *  - 200: True if courses removed correctly
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathRemovecoursesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_removecourses")
    fun localIomadLearningpathRemovecourses(@Body elearningLocalIomadLearningpathRemovecoursesRequest: ElearningLocalIomadLearningpathRemovecoursesRequest): Call<kotlin.Any>

    /**
     * POST local_iomad_learningpath_removeusers
     * Remove users from learning path
     * Remove users from learning path
     * Responses:
     *  - 200: True if users removed correctly
     *  - 400: Invalid parameter value detected
     *
     * @param elearningLocalIomadLearningpathRemoveusersRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("local_iomad_learningpath_removeusers")
    fun localIomadLearningpathRemoveusers(@Body elearningLocalIomadLearningpathRemoveusersRequest: ElearningLocalIomadLearningpathRemoveusersRequest): Call<kotlin.Any>

}
