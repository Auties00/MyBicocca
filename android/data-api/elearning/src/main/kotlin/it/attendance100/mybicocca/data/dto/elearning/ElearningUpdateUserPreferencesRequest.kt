package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningUpdateUserPreferencesRequest(
    private val userId: Int? = null,
    private val preferences: List<ElearningUserPreference>
) : ElearningRequest<ElearningUpdateUserPreferencesResponse> {
    override val functionName = "core_user_update_user_preferences"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        userId?.let { formData.append("userid", it.toString()) }
        preferences.forEachIndexed { index, pref ->
            formData.append("preferences[$index][type]", pref.name)
            pref.value?.let { formData.append("preferences[$index][value]", it) }
        }
    }
}
