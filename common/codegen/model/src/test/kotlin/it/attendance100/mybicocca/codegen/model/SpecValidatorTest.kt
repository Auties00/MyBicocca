package it.attendance100.mybicocca.codegen.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpecValidatorTest {
    private val record = RecordType("DemoItem", listOf(Field("name", "nome", TypeRef.STRING, nullable = false, default = DefaultValue.Zero)))

    private fun spec(
        records: List<RecordType> = listOf(record),
        operations: List<Operation> = emptyList(),
        enums: List<EnumType> = emptyList(),
    ) = ApiSpec("demo", "Demo", listOf(ApiModule("Items", "/", records, operations)), enums)

    private fun operation(path: String, vararg parameters: Parameter) =
        Operation("get", HttpMethod.GET, path, parameters.toList(), body = null, response = ResponseBody.None)

    @Test
    fun `accepts a consistent spec`() {
        assertTrue(SpecValidator.validate(spec()).isEmpty())
    }

    @Test
    fun `reports unknown type references`() {
        val broken = record.copy(fields = listOf(Field("owner", "owner", TypeRef.Named("Missing"), nullable = false, default = null)))
        assertEquals(listOf("Field 'DemoItem.owner' references unknown type 'Missing'"), SpecValidator.validate(spec(listOf(broken))))
    }

    @Test
    fun `reports defaults that do not fit the field`() {
        val broken = record.copy(
            fields = listOf(
                Field("a", "a", TypeRef.STRING, nullable = false, default = DefaultValue.Null),
                Field("b", "b", TypeRef.STRING, nullable = false, default = DefaultValue.EmptyList),
                Field("c", "c", TypeRef.Primitive(PrimitiveKind.LOCAL_DATE), nullable = false, default = DefaultValue.Zero),
            )
        )
        assertEquals(3, SpecValidator.validate(spec(listOf(broken))).size)
    }

    @Test
    fun `reports temporal codecs on non-temporal fields`() {
        val broken = record.copy(
            fields = listOf(Field("a", "a", TypeRef.STRING, nullable = true, default = DefaultValue.Null, codec = Codec.Temporal("dd/MM/yyyy", true)))
        )
        assertEquals(listOf("Field 'DemoItem.a' uses a temporal codec but isn't a date or time"), SpecValidator.validate(spec(listOf(broken))))
    }

    @Test
    fun `reports path placeholders without a matching parameter and the reverse`() {
        val problems = SpecValidator.validate(
            spec(operations = listOf(operation("/items/{id}", Parameter("other", "other", ParameterLocation.PATH, TypeRef.STRING, required = true))))
        )
        assertEquals(
            listOf(
                "Operation 'Items.get' has no path parameter for '{id}'",
                "Operation 'Items.get' declares path parameter 'other' missing from its path",
            ),
            problems,
        )
    }

    @Test
    fun `reports operations sending both a JSON body and form fields`() {
        val operation = Operation(
            "post", HttpMethod.POST, "/a",
            listOf(Parameter("flag", "flag", ParameterLocation.FORM, TypeRef.STRING, required = true)),
            body = RequestBody.Json(TypeRef.STRING), response = ResponseBody.None,
        )
        assertEquals(listOf("Operation 'Items.post' sends both a JSON body and form fields"), SpecValidator.validate(spec(operations = listOf(operation))))
    }

    @Test
    fun `reports scrapes yielding another kind than the field needs`() {
        val scraped = RecordType(
            "DemoPage",
            listOf(
                Field("title", "title", TypeRef.STRING, nullable = true, default = DefaultValue.Null, scrape = listOf(ScrapeStep.Select("h1"))),
                Field("open", "open", TypeRef.Primitive(PrimitiveKind.BOOLEAN), nullable = false, default = null, scrape = listOf(ScrapeStep.Text())),
                Field("note", "note", TypeRef.STRING, nullable = false, default = null),
            ),
            scrape = RecordScrape(),
        )
        assertEquals(
            listOf(
                "Field 'DemoPage.title': the scrape yields ELEMENT but the type needs TEXT",
                "Field 'DemoPage.open': the scrape yields TEXT but the type needs FLAG",
                "Field 'DemoPage.note' has neither a scrape nor a default",
            ),
            SpecValidator.validate(spec(listOf(record, scraped))),
        )
    }

    @Test
    fun `reports scrape steps reading the wrong kind and invalid patterns`() {
        val scraped = RecordType(
            "DemoPage",
            listOf(
                Field(
                    "id", "id", TypeRef.STRING, nullable = true, default = DefaultValue.Null,
                    scrape = listOf(ScrapeStep.SelectAll("a"), ScrapeStep.Text(), ScrapeStep.Regex("(")),
                ),
            ),
            scrape = RecordScrape(),
        )
        assertEquals(
            listOf("Field 'DemoPage.id': Text reads ELEMENT, not ELEMENTS", "Field 'DemoPage.id' has the invalid pattern '('"),
            SpecValidator.validate(spec(listOf(record, scraped))),
        )
    }

    @Test
    fun `reports repeated parameters that are not lists`() {
        val operation = operation("/a", Parameter("day", "day[]", ParameterLocation.QUERY, TypeRef.STRING, required = true, encoding = ParameterEncoding.REPEATED))
        assertEquals(
            listOf("Operation 'Items.get' parameter 'day' is repeated but isn't a list of plain values"),
            SpecValidator.validate(spec(operations = listOf(operation))),
        )
    }

    @Test
    fun `reports duplicate names`() {
        val enum = EnumType("DemoItem", listOf(EnumValue("A", "a"), EnumValue("A", "b")))
        val problems = SpecValidator.validate(spec(operations = listOf(operation("/a"), operation("/b")), enums = listOf(enum)))
        assertEquals(
            setOf(
                "Type 'DemoItem' is declared more than once",
                "Enum 'DemoItem' declares value name 'A' more than once",
                "Module 'Items' declares operation 'get' more than once",
            ),
            problems.toSet(),
        )
    }
}
