package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChoiceRegulation
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChoiceRegulationWindow
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChoiceRegulationWithCounts
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChoiceRegulationWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChoiceRegulationWithSchemas
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChoiceRuleWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3ChoiceRuleWithSchemas
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PlanSchema
import it.attendance100.mybicocca.data.dto.esse3.Esse3PlanSchemaWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3PlansCountsFilters
import it.attendance100.mybicocca.data.dto.esse3.Esse3PrerequisitesRegulationWithConstraints
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudentsStatistics
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlanHeaderPerActivity
import it.attendance100.mybicocca.data.dto.esse3.Esse3StudyPlanHeaderPerRule
import it.attendance100.mybicocca.data.dto.esse3.Esse3UpdateChoiceRegulation
import kotlinx.serialization.json.Json

class Esse3ChoiceRulesApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/regsce-service-v1") {

    suspend fun getChoiceRegulations(
        choiceRegulationState: List<String>? = null,
        facultyId: Long? = null,
        facultyCode: String? = null,
        courseOfStudyId: Long? = null,
        courseOfStudyCode: String? = null,
        academicYearOrderId: Long? = null,
        academicYearRevisionId: Long? = null,
        courseTypeCode: String? = null,
        cohort: Long? = null,
        fromModificationDate: String? = null,
        fields: String? = null,
        start: Int? = null,
        limit: Int? = null,
        optionalFields: String? = null,
        order: String? = null
    ): List<Esse3ChoiceRegulation> {
        return executeJsonGetList<Esse3ChoiceRegulation>("/regsce", setOf(Esse3PermissionLevel.ANY)) {
            choiceRegulationState?.let { parameter("statoRegsce", it) }
            facultyId?.let { parameter("facId", it) }
            facultyCode?.let { parameter("facCod", it) }
            courseOfStudyId?.let { parameter("cdsId", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            academicYearOrderId?.let { parameter("aaOrdId", it) }
            academicYearRevisionId?.let { parameter("aaRevisioneId", it) }
            courseTypeCode?.let { parameter("tipoCorsoCod", it) }
            cohort?.let { parameter("coorte", it) }
            fromModificationDate?.let { parameter("daDataMod", it) }
            fields?.let { parameter("fields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getChoiceRegulationWithSchemas(
        choiceRegulationId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null,
        filter: String? = null
    ): Esse3ChoiceRegulationWithSchemas {
        return executeJsonGet<Esse3ChoiceRegulationWithSchemas>("/regsce/${choiceRegulationId}", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun patchChoiceRegulation(
        choiceRegulationId: Long,
        body: Esse3UpdateChoiceRegulation,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null,
        filter: String? = null
    ): Esse3ChoiceRegulationWithSchemas {
        return executeJsonPatch<Esse3ChoiceRegulationWithSchemas>("/regsce/${choiceRegulationId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getChoiceRegulationWindows(
        choiceRegulationId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): List<Esse3ChoiceRegulationWindow> {
        return executeJsonGetList<Esse3ChoiceRegulationWindow>("/regsce/${choiceRegulationId}/finestre", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getChoiceRegulationWindow(
        choiceRegulationId: Long,
        finalRegulationChoiceId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ChoiceRegulationWindow {
        return executeJsonGet<Esse3ChoiceRegulationWindow>("/regsce/${choiceRegulationId}/finestre/${finalRegulationChoiceId}", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun putLinkedPlansCount(
        choiceRegulationId: Long,
        body: Esse3PlansCountsFilters,
        optionalFields: String? = null
    ): Esse3ChoiceRegulationWithCounts {
        return executeJsonPut<Esse3ChoiceRegulationWithCounts>("/regsce/${choiceRegulationId}/piani-collegati/conteggi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getPlansStatsByChoiceRegulationId(
        choiceRegulationId: Long
    ): Esse3StudentsStatistics {
        return executeJsonGet<Esse3StudentsStatistics>("/regsce/${choiceRegulationId}/piani/stats", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getChoiceRules(
        choiceRegulationId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null,
        filter: String? = null
    ): List<Esse3ChoiceRuleWithSchemas> {
        return executeJsonGetList<Esse3ChoiceRuleWithSchemas>("/regsce/${choiceRegulationId}/regole", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getChoiceRuleWithDetails(
        choiceRegulationId: Long,
        choiceId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3ChoiceRuleWithDetails {
        return executeJsonGet<Esse3ChoiceRuleWithDetails>("/regsce/${choiceRegulationId}/regole/${choiceId}", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getLinkedPlansByTeachingActivityChoiceId(
        choiceRegulationId: Long,
        choiceId: Long,
        teachingActivityChoiceId: Long,
        planStates: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3StudyPlanHeaderPerActivity> {
        return executeJsonGetList<Esse3StudyPlanHeaderPerActivity>("/regsce/${choiceRegulationId}/regole/${choiceId}/AD/${teachingActivityChoiceId}/piani", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            planStates?.let { parameter("statiPiano", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getLinkedPlansByChoiceId(
        choiceRegulationId: Long,
        choiceId: Long,
        planStates: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        start: Int? = null,
        limit: Int? = null,
        order: String? = null
    ): List<Esse3StudyPlanHeaderPerRule> {
        return executeJsonGetList<Esse3StudyPlanHeaderPerRule>("/regsce/${choiceRegulationId}/regole/${choiceId}/piani", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            planStates?.let { parameter("statiPiano", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getPlansStatsByChoiceId(
        choiceRegulationId: Long,
        choiceId: Long
    ): Esse3StudentsStatistics {
        return executeJsonGet<Esse3StudentsStatistics>("/regsce/${choiceRegulationId}/regole/${choiceId}/piani/stats", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getStudyPlanSchemas(
        choiceRegulationId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        order: String? = null,
        filter: String? = null
    ): List<Esse3PlanSchema> {
        return executeJsonGetList<Esse3PlanSchema>("/regsce/${choiceRegulationId}/schemi", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            order?.let { parameter("order", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getStudyPlanSchemaWithDetails(
        choiceRegulationId: Long,
        schemaId: Long,
        fields: String? = null,
        optionalFields: String? = null
    ): Esse3PlanSchemaWithDetails {
        return executeJsonGet<Esse3PlanSchemaWithDetails>("/regsce/${choiceRegulationId}/schemi/${schemaId}", setOf(Esse3PermissionLevel.ANY)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
        }
    }

    suspend fun getPlansStatsBySchemaId(
        choiceRegulationId: Long,
        schemaId: Long
    ): Esse3StudentsStatistics {
        return executeJsonGet<Esse3StudentsStatistics>("/regsce/${choiceRegulationId}/schemi/${schemaId}/piani/stats", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getChoiceRegulationWithDetails(
        choiceRegulationId: Long,
        order: String? = null,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): Esse3ChoiceRegulationWithDetails {
        return executeJsonGet<Esse3ChoiceRegulationWithDetails>("/regsceFull/${choiceRegulationId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getPropaedeuticityRegulationByChoiceRegulation(
        choiceRegulationId: Long,
        fields: String? = null,
        optionalFields: String? = null,
        filter: String? = null
    ): Esse3PrerequisitesRegulationWithConstraints {
        return executeJsonGet<Esse3PrerequisitesRegulationWithConstraints>("/regsceFull/${choiceRegulationId}/regprop", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            fields?.let { parameter("fields", it) }
            optionalFields?.let { parameter("optionalFields", it) }
            filter?.let { parameter("filter", it) }
        }
    }
}
