package it.attendance100.mybicocca.codegen.reader.openapi

import it.attendance100.mybicocca.codegen.core.ReaderContext
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.ParameterEncoding
import it.attendance100.mybicocca.codegen.model.RecordScrape
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.ScrapeStep
import it.attendance100.mybicocca.codegen.model.SpecValidator
import it.attendance100.mybicocca.codegen.model.TypeRef
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The extensions for pages: fixed form fields, repeated parameters, script variables and scraping. */
class OpenApiPagesTest {
    private val spec: ApiSpec = OpenApiReader().read(
        ReaderContext(Path("src/test/resources/pages"), JsonObject(mapOf("spec" to JsonPrimitive("pages.yaml"))), mayUpdateSpecFiles = false) {}
    )
    private val operations = spec.modules.first().operations.associateBy { it.name }

    @Test
    fun `reads a valid spec`() {
        assertTrue(SpecValidator.validate(spec).isEmpty(), SpecValidator.validate(spec).toString())
    }

    @Test
    fun `reads fixed query parameters and a script variable response`() {
        val operation = operations.getValue("getBuildings")
        assertEquals(mapOf("sw" to "rooms_"), operation.query)
        assertEquals(ResponseBody.ScriptVariable("elenco_sedi", TypeRef.ListOf(TypeRef.Named("PagesBuilding"))), operation.response)
    }

    @Test
    fun `reads fixed form fields, repeated parameters and a scraped response`() {
        val operation = operations.getValue("getRooms")
        assertEquals(mapOf("view" to "vetrina_aule", "list" to ""), operation.form)
        assertEquals(
            listOf("rooms" to ParameterEncoding.REPEATED, "buildings" to ParameterEncoding.REPEATED, "language" to ParameterEncoding.PLAIN),
            operation.parameters.map { it.name to it.encoding },
        )
        assertEquals("it", operation.parameters.single { it.name == "language" }.default?.value)
        assertEquals(
            ResponseBody.Html(TypeRef.ListOf(TypeRef.Named("PagesRoom")), listOf(ScrapeStep.SelectAll(".attendance-section"))),
            operation.response,
        )
    }

    @Test
    fun `reads scraped records and their fields' pipelines`() {
        val room = spec.modules.first().records.single { it.name == "PagesRoom" }
        assertEquals(RecordScrape(missing = listOf("Dato mancante")), room.scrape)
        val scrapes = room.fields.associate { it.name to it.scrape }
        assertEquals(listOf(ScrapeStep.Select(".attendance-course-name"), ScrapeStep.OwnText), scrapes["name"])
        assertEquals(
            listOf(
                ScrapeStep.SelectAll(".custom-color-bold"),
                ScrapeStep.Where(listOf(ScrapeStep.Text(), ScrapeStep.Equals("Capacità"))),
                ScrapeStep.First,
                ScrapeStep.LastSibling,
                ScrapeStep.Text(),
                ScrapeStep.RemoveSuffix(" posti"),
            ),
            scrapes["capacity"],
        )
        assertEquals(
            listOf(
                ScrapeStep.FirstOf(
                    listOf(
                        listOf(ScrapeStep.Select(".attendance-course-name span"), ScrapeStep.Text(), ScrapeStep.Unwrap("[", "]")),
                        listOf(ScrapeStep.Attribute("data-code")),
                    ),
                ),
            ),
            scrapes["code"],
        )
    }

    @Test
    fun `reads the codecs of PHP backends`() {
        val lesson = spec.modules.first().records.single { it.name == "PagesLesson" }
        assertEquals(
            listOf(
                Codec.EpochSeconds,
                Codec.Joined(","),
                Codec.ObjectValues,
                Codec.EmbeddedHtml(listOf(ScrapeStep.Select("iframe"), ScrapeStep.Attribute("src"))),
            ),
            lesson.fields.map { it.codec },
        )
    }

    @Test
    fun `creates the modules records name even without operations`() {
        val templates = spec.modules.single { it.name == "Templates" }
        assertTrue(templates.operations.isEmpty())
        assertEquals(listOf("PagesTemplate"), templates.records.map { it.name })
    }
}
