package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import it.attendance100.mybicocca.data.dto.esse3.Esse3Competition
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionAdmissionInsert
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionEnrolled
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionEnrolledDetail
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionRanking
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionRankingList
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionTests
import it.attendance100.mybicocca.data.dto.esse3.Esse3CompetitionWithDetails
import it.attendance100.mybicocca.data.dto.esse3.Esse3ImportResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.dto.esse3.Esse3PersonRanking
import it.attendance100.mybicocca.data.dto.esse3.Esse3RankingImport
import it.attendance100.mybicocca.data.dto.esse3.Esse3RankingListImport
import kotlinx.serialization.json.Json

class Esse3CompetitionsApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/concorsi-service-v2") {

    suspend fun getCompetitions(
        academicYearId: Long? = null,
        filter: String? = null
    ): List<Esse3Competition> {
        return executeJsonGetList<Esse3Competition>("/concorsi", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            academicYearId?.let { parameter("aaId", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getCompetitionRanking(
        academicYearId: Long,
        testId: Long,
        personId: Long? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3CompetitionRankingList> {
        return executeJsonGetList<Esse3CompetitionRankingList>("/concorsi/graduatoria/${academicYearId}/${testId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            personId?.let { parameter("persId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getCompetitionRankingDetail(
        academicYearId: Long,
        testId: Long,
        testDetailId: Long,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3CompetitionRankingList> {
        return executeJsonGetList<Esse3CompetitionRankingList>("/concorsi/graduatoria/${academicYearId}/${testId}/${testDetailId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getPersonRanking(
        personId: Long,
        positionId: Long
    ): List<Esse3CompetitionRankingList> {
        return executeJsonGetList<Esse3CompetitionRankingList>("/concorsi/graduatoria/${personId}/${positionId}/persona", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }

    suspend fun getCompetition(
        academicYearId: Long,
        testId: Long,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3CompetitionWithDetails> {
        return executeJsonGetList<Esse3CompetitionWithDetails>("/concorsi/${academicYearId}/${testId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun putCompetitionRanking(
        academicYearId: Long,
        testId: Long,
        competitionTestsId: Long,
        body: List<Esse3RankingImport>,
        fileTypology: String
    ): List<Esse3ImportResponse> {
        return executeJsonPutList<Esse3ImportResponse>("/concorsi/${academicYearId}/${testId}/classifica/${competitionTestsId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("tipologiaFile", fileTypology)
        }
    }

    suspend fun putCompetitionRankingFile(
        academicYearId: Long,
        testId: Long,
        competitionTestsId: Long,
        body: kotlinx.serialization.json.JsonObject,
        fileTypology: String
    ): List<Esse3ImportResponse> {
        return executeJsonPutList<Esse3ImportResponse>("/concorsi/${academicYearId}/${testId}/classifica/${competitionTestsId}/file", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("tipologiaFile", fileTypology)
        }
    }

    suspend fun getCompetitionRanking(
        academicYearId: Long,
        testId: Long,
        personId: Long? = null,
        order: String? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3CompetitionRanking> {
        return executeJsonGetList<Esse3CompetitionRanking>("/concorsi/${academicYearId}/${testId}/classificaConcorso", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            personId?.let { parameter("persId", it) }
            order?.let { parameter("order", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun putCompetitionRankingsFile(
        academicYearId: Long,
        testId: Long,
        body: kotlinx.serialization.json.JsonObject
    ): List<Esse3ImportResponse> {
        return executeJsonPutList<Esse3ImportResponse>("/concorsi/${academicYearId}/${testId}/graduatorie/file", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun putRankingTestDetail(
        academicYearId: Long,
        testId: Long,
        testDetailId: Long,
        body: List<Esse3RankingListImport>
    ): List<Esse3ImportResponse> {
        return executeJsonPutList<Esse3ImportResponse>("/concorsi/${academicYearId}/${testId}/graduatorie/${testDetailId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun putRankingTestDetailFile(
        academicYearId: Long,
        testId: Long,
        testDetailId: Long,
        body: kotlinx.serialization.json.JsonObject
    ): List<Esse3ImportResponse> {
        return executeJsonPutList<Esse3ImportResponse>("/concorsi/${academicYearId}/${testId}/graduatorie/${testDetailId}/file", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getCompetitionEnrolled(
        academicYearId: Long,
        testId: Long,
        personId: Long? = null,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3CompetitionEnrolled> {
        return executeJsonGetList<Esse3CompetitionEnrolled>("/concorsi/${academicYearId}/${testId}/iscrizioni", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            personId?.let { parameter("persId", it) }
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun putCompetitionEnrollment(
        academicYearId: Long,
        testId: Long,
        personId: Long,
        body: Esse3CompetitionAdmissionInsert
    ): List<Esse3CompetitionEnrolledDetail> {
        return executeJsonPutList<Esse3CompetitionEnrolledDetail>("/concorsi/${academicYearId}/${testId}/iscrizioni/${personId}", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun getCompetitionTests(
        academicYearId: Long,
        testId: Long,
        start: Int? = null,
        limit: Int? = null
    ): List<Esse3CompetitionTests> {
        return executeJsonGetList<Esse3CompetitionTests>("/concorsi/${academicYearId}/${testId}/proveConcorso", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            start?.let { parameter("start", it) }
            limit?.let { parameter("limit", it) }
        }
    }

    suspend fun getPersonRanking(
        personId: Long,
        positionId: Long,
        order: String? = null
    ): List<Esse3PersonRanking> {
        return executeJsonGetList<Esse3PersonRanking>("/concorsi/${personId}/${positionId}/classifica", setOf(Esse3PermissionLevel.TECHNICAL_USER)) {
            order?.let { parameter("order", it) }
        }
    }

    suspend fun getCompetitionEnrolledDetail(
        personId: Long,
        positionId: Long
    ): List<Esse3CompetitionEnrolledDetail> {
        return executeJsonGetList<Esse3CompetitionEnrolledDetail>("/concorsi/${personId}/${positionId}/dettaglioIscritto", setOf(Esse3PermissionLevel.TECHNICAL_USER))
    }
}
