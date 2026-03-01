package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PrerequisiteConstraint
import it.attendance100.mybicocca.data.dto.esse3.Esse3PrerequisitesRegulation
import it.attendance100.mybicocca.data.dto.esse3.Esse3PrerequisitesRegulationWithConstraints
import kotlinx.serialization.json.Json

class Esse3RulePropertiesApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/regprop-service-v1") {

    suspend fun getPropaedeuticityRegulations(
        proposalRuleState: String? = null,
        facultyId: Long? = null,
        facultyCode: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        courseTypeCode: String? = null,
        cohort: Long? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null
    ): List<Esse3PrerequisitesRegulation> {
        return executeJsonGetList<Esse3PrerequisitesRegulation>("/regprop", setOf(Esse3PermissionLevel.ANY)) {
            proposalRuleState?.let { parameter("statoRegprop", it) }
            facultyId?.let { parameter("facId", it) }
            facultyCode?.let { parameter("facCod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            cohort?.let { parameter("coorte", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getPropaedeuticityRegulationWithDetails(
        proposalRuleId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): Esse3PrerequisitesRegulationWithConstraints {
        return executeJsonGet<Esse3PrerequisitesRegulationWithConstraints>("/regprop/${proposalRuleId}", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getRegulationConstraints(
        proposalRuleId: Long,
        proposalRuleWinnerId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3PrerequisiteConstraint> {
        return executeJsonGetList<Esse3PrerequisiteConstraint>("/regprop/${proposalRuleId}/${proposalRuleWinnerId}", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }
}
