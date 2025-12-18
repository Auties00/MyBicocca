package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolTemplatelibraryListTemplatesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningToolTemplatelibraryLoadCanonicalTemplateRequest

interface ToolTemplatelibraryApi {
    /**
     * POST tool_templatelibrary_list_templates
     * List/search templates by component.
     * List/search templates by component.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolTemplatelibraryListTemplatesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tool_templatelibrary_list_templates")
    fun toolTemplatelibraryListTemplates(@Body elearningToolTemplatelibraryListTemplatesRequest: ElearningToolTemplatelibraryListTemplatesRequest): Call<kotlin.Any>

    /**
     * POST tool_templatelibrary_load_canonical_template
     * Load a canonical template by name (not the theme overidden one).
     * Load a canonical template by name (not the theme overidden one).
     * Responses:
     *  - 200: template
     *  - 400: Invalid parameter value detected
     *
     * @param elearningToolTemplatelibraryLoadCanonicalTemplateRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("tool_templatelibrary_load_canonical_template")
    fun toolTemplatelibraryLoadCanonicalTemplate(@Body elearningToolTemplatelibraryLoadCanonicalTemplateRequest: ElearningToolTemplatelibraryLoadCanonicalTemplateRequest): Call<kotlin.Any>

}
