package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class Esse3Api(httpClientConfig: HttpClientConfig<*>.() -> Unit = {}) : AutoCloseable {

    private val json = Json {
        coerceInputValues = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    private val client = HttpClient {
        httpClientConfig()

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        followRedirects = true
    }

    val attachments = Esse3AttachmentsApi(client, json)
    val auth = Esse3AuthApi(client, json)
    val logging = Esse3LoggingApi(client, json)
    val personalData = Esse3PersonalDataApi(client, json)
    val careers = Esse3CareersApi(client, json)
    val teachers = Esse3TeachersApi(client, json)
    val badgeImport = Esse3BadgeImportApi(client, json)
    val countries = Esse3CountriesApi(client, json)
    val internships = Esse3InternshipsApi(client, json)
    val questionnaires = Esse3QuestionnairesApi(client, json)
    val competitions = Esse3CompetitionsApi(client, json)
    val tuitionFees = Esse3TuitionFeesApi(client, json)
    val structure = Esse3StructureApi(client, json)
    val choiceRules = Esse3ChoiceRulesApi(client, json)
    val logistics = Esse3LogisticsApi(client, json)
    val offer = Esse3OfferApi(client, json)
    val ruleProperties = Esse3RulePropertiesApi(client, json)
    val teacherReporting = Esse3TeacherReportingApi(client, json)
    val calesa = Esse3CalesaApi(client, json)
    val careerUpdate = Esse3CareerUpdateApi(client, json)
    val transcript = Esse3TranscriptApi(client, json)
    val plans = Esse3PlansApi(client, json)
    val records = Esse3RecordsApi(client, json)
    val degreeAward = Esse3DegreeAwardApi(client, json)
    val communications = Esse3CommunicationsApi(client, json)
    val badge = Esse3BadgeApi(client, json)
    val services = Esse3ServicesApi(client, json)
    val users = Esse3UsersApi(client, json)
    val appointmentsCalendar = Esse3AppointmentsCalendarApi(client, json)

    override fun close() {
        client.close()
    }
}
