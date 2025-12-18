package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockRecentlyaccesseditemsGetRecentItemsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse

interface BlockRecentlyaccesseditemsApi {
    /**
     * POST block_recentlyaccesseditems_get_recent_items
     * List of items a user has accessed most recently.
     * List of items a user has accessed most recently.
     * Responses:
     *  - 200: The most recently accessed activities/resources by the logged user
     *  - 400: Invalid parameter value detected
     *
     * @param elearningBlockRecentlyaccesseditemsGetRecentItemsRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("block_recentlyaccesseditems_get_recent_items")
    fun blockRecentlyaccesseditemsGetRecentItems(@Body elearningBlockRecentlyaccesseditemsGetRecentItemsRequest: ElearningBlockRecentlyaccesseditemsGetRecentItemsRequest): Call<kotlin.Any>

}
