package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockAccessreviewGetModuleDataRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockAccessreviewGetSectionDataRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse

interface BlockAccessreviewApi {
    /**
     * POST block_accessreview_get_module_data
     * Gets error data for course modules.
     * Gets error data for course modules.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockAccessreviewGetModuleDataRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_accessreview_get_module_data")
    fun blockAccessreviewGetModuleData(@Body elearningBlockAccessreviewGetModuleDataRequest: ElearningBlockAccessreviewGetModuleDataRequest): Call<kotlin.Any>

    /**
     * POST block_accessreview_get_section_data
     * Gets error data for course sections.
     * Gets error data for course sections.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockAccessreviewGetSectionDataRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_accessreview_get_section_data")
    fun blockAccessreviewGetSectionData(@Body elearningBlockAccessreviewGetSectionDataRequest: ElearningBlockAccessreviewGetSectionDataRequest): Call<kotlin.Any>

}
