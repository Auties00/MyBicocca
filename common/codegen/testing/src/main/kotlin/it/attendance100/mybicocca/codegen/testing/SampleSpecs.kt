package it.attendance100.mybicocca.codegen.testing

import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthParameter
import it.attendance100.mybicocca.codegen.model.AuthRequirement
import it.attendance100.mybicocca.codegen.model.AuthScheme
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.EnumValue
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.HttpMethod
import it.attendance100.mybicocca.codegen.model.Operation
import it.attendance100.mybicocca.codegen.model.Parameter
import it.attendance100.mybicocca.codegen.model.ParameterEncoding
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordEncoding
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.RequestBody
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.Server
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * A small API that exercises every feature of the model, so that each target's golden output
 * shows how it handles each one.
 */
object SampleSpecs {
    private val STRING = TypeRef.STRING
    private val INT32 = TypeRef.Primitive(PrimitiveKind.INT32)
    private val INT64 = TypeRef.Primitive(PrimitiveKind.INT64)
    private val BOOLEAN = TypeRef.Primitive(PrimitiveKind.BOOLEAN)
    private val LOCAL_DATE = TypeRef.Primitive(PrimitiveKind.LOCAL_DATE)
    private val LOCAL_DATE_TIME = TypeRef.Primitive(PrimitiveKind.LOCAL_DATE_TIME)
    private val LOCAL_TIME = TypeRef.Primitive(PrimitiveKind.LOCAL_TIME)

    val status = EnumType(
        name = "DemoStatus",
        values = listOf(
            EnumValue("Active", "A", "The item is in use."),
            EnumValue("Closed", "C"),
        ),
        open = true,
        acceptsWrappedValue = true,
        description = "Lifecycle of an item.",
    )

    val kind = EnumType(
        name = "DemoKind",
        values = listOf(EnumValue("Book", "book"), EnumValue("Unknown", "unknown")),
        open = true,
    )

    val priority = EnumType(
        name = "DemoPriority",
        values = listOf(EnumValue("Low", "low"), EnumValue("High", "high")),
        open = false,
    )

    val level = EnumType(
        name = "DemoLevel",
        values = listOf(EnumValue("Basic", "1"), EnumValue("Expert", "2")),
        open = true,
        integer = true,
    )

    val owner = RecordType(
        name = "DemoOwner",
        fields = listOf(Field("id", "ownerId", INT64, nullable = false, default = null)),
    )

    val item = RecordType(
        name = "DemoItem",
        description = "An item of the catalog.",
        fields = listOf(
            Field("name", "nome", STRING, nullable = false, default = DefaultValue.Zero, description = "Display name."),
            Field("count", "num", INT32, nullable = true, default = DefaultValue.Null),
            Field("active", "attivo", BOOLEAN, nullable = false, default = DefaultValue.Zero),
            Field("tags", "tag", TypeRef.ListOf(STRING), nullable = false, default = DefaultValue.EmptyList),
            Field(
                "startDate", "dataInizio", LOCAL_DATE, nullable = true, default = DefaultValue.Null,
                codec = Codec.Temporal("dd/MM/yyyy", blankAsNull = true),
            ),
            Field("updatedAt", "aggiornato", LOCAL_DATE_TIME, nullable = true, default = DefaultValue.Null),
            Field("status", "stato", TypeRef.Named(status.name), nullable = true, default = DefaultValue.Null),
            Field("owner", "proprietario", TypeRef.Named(owner.name), nullable = false, default = null),
            Field("previousOwners", "storico", TypeRef.ListOf(TypeRef.Named(owner.name)), nullable = false, default = DefaultValue.EmptyList),
            Field("extra", "extra", TypeRef.Primitive(PrimitiveKind.JSON_OBJECT), nullable = true, default = DefaultValue.Null),
            Field(
                "opensAt", "apertura", TypeRef.Primitive(PrimitiveKind.LOCAL_TIME), nullable = true, default = DefaultValue.Null,
                codec = Codec.Temporal("HH:mm", blankAsNull = false),
            ),
            Field("payload", "payload", TypeRef.Primitive(PrimitiveKind.JSON_VALUE), nullable = true, default = DefaultValue.Null),
            Field("kind", "tipo", TypeRef.Named(kind.name), nullable = false, default = DefaultValue.Literal("book")),
            Field("rating", "voto", TypeRef.Primitive(PrimitiveKind.FLOAT64), nullable = false, default = DefaultValue.Literal("2.5")),
            Field("default", "default", STRING, nullable = true, default = DefaultValue.Null, description = "A field named like a keyword."),
        ),
    )

    val empty = RecordType(name = "DemoEmpty", fields = emptyList())

    val slot = RecordType(
        name = "DemoSlot",
        description = "A time range, written as `09:00-09:30`.",
        encoding = RecordEncoding.Delimited("-"),
        fields = listOf(
            Field("start", "start", LOCAL_TIME, nullable = false, default = null, codec = Codec.Temporal("HH:mm", blankAsNull = false)),
            Field("end", "end", LOCAL_TIME, nullable = false, default = null, codec = Codec.Temporal("HH:mm", blankAsNull = false)),
        ),
    )

    val settings = RecordType(
        name = "DemoSettings",
        fields = listOf(
            Field("enabled", "abilitato", BOOLEAN, nullable = false, default = DefaultValue.Zero),
            Field("limit", "limite", INT32, nullable = false, default = DefaultValue.Zero),
        ),
    )

    val calendar = RecordType(
        name = "DemoCalendar",
        fields = listOf(
            Field("days", "giorni", TypeRef.MapOf(LOCAL_DATE, TypeRef.ListOf(TypeRef.Named(slot.name))), nullable = false, default = DefaultValue.EmptyMap),
            Field(
                "seats", "posti", TypeRef.MapOf(LOCAL_DATE, TypeRef.MapOf(TypeRef.Named(slot.name), INT32)),
                nullable = false, default = DefaultValue.EmptyMap,
            ),
            Field("labels", "etichette", TypeRef.MapOf(STRING, STRING), nullable = false, default = DefaultValue.EmptyMap),
            Field("level", "livello", TypeRef.Named(level.name), nullable = true, default = DefaultValue.Null),
            Field("note", "nota", STRING, nullable = true, default = DefaultValue.Null, codec = Codec.BlankAsNull(listOf("0"))),
            Field("settings", "impostazioni", TypeRef.Named(settings.name), nullable = true, default = DefaultValue.Null, codec = Codec.FoldedObject),
            Field("policy", "regole", TypeRef.Named(settings.name), nullable = true, default = DefaultValue.Null, codec = Codec.EmbeddedJson),
        ),
    )

    private val items = ApiModule(
        name = "Items",
        basePath = "/items-service-v1",
        records = listOf(item, owner, empty),
        operations = listOf(
            Operation(
                name = "getItems",
                method = HttpMethod.GET,
                path = "/owners/{ownerId}/items",
                parameters = listOf(
                    Parameter("ownerId", "ownerId", ParameterLocation.PATH, INT64, required = true, description = "Owner of the items."),
                    Parameter(
                        "status", "stato", ParameterLocation.QUERY, TypeRef.Named(status.name), required = false,
                        valueDescriptions = mapOf("A" to "Only active items."),
                    ),
                    Parameter("ids", "ids", ParameterLocation.QUERY, TypeRef.ListOf(INT64), required = false),
                    Parameter("limit", "limit", ParameterLocation.QUERY, INT32, required = true),
                ),
                body = null,
                response = ResponseBody.Json(TypeRef.ListOf(TypeRef.Named(item.name))),
                roles = setOf("STUDENT", "TECHNICAL_USER"),
                summary = "Lists the items of an owner.",
            ),
            Operation(
                name = "createItem",
                method = HttpMethod.POST,
                path = "/items",
                parameters = listOf(Parameter("notify", "notifica", ParameterLocation.QUERY, BOOLEAN, required = false)),
                body = RequestBody.Json(TypeRef.Named(item.name), "The item to create."),
                response = ResponseBody.Json(TypeRef.Named(item.name)),
            ),
            Operation(
                name = "renameItem",
                method = HttpMethod.PUT,
                path = "/items/{itemId}/name",
                parameters = listOf(
                    Parameter("itemId", "itemId", ParameterLocation.PATH, STRING, required = true),
                    Parameter("name", "nome", ParameterLocation.FORM, STRING, required = true),
                    Parameter("count", "num", ParameterLocation.FORM, INT32, required = false),
                    Parameter("status", "stato", ParameterLocation.FORM, TypeRef.Named(status.name), required = false),
                ),
                body = null,
                response = ResponseBody.None,
            ),
            Operation(
                name = "deleteItem",
                method = HttpMethod.DELETE,
                path = "/items/{itemId}",
                parameters = listOf(Parameter("itemId", "itemId", ParameterLocation.PATH, STRING, required = true)),
                body = null,
                response = ResponseBody.None,
            ),
            Operation(
                name = "getItemPdf",
                method = HttpMethod.GET,
                path = "/items/{itemId}/pdf",
                parameters = listOf(
                    Parameter("itemId", "itemId", ParameterLocation.PATH, STRING, required = true),
                    Parameter("priority", "priority", ParameterLocation.QUERY, TypeRef.Named(priority.name), required = true),
                ),
                body = null,
                response = ResponseBody.Binary,
            ),
        ),
    )

    private val search = ApiModule(
        name = "Search",
        basePath = "",
        records = emptyList(),
        operations = listOf(
            Operation(
                name = "searchItems",
                method = HttpMethod.GET,
                path = "/search",
                parameters = listOf(
                    Parameter("query", "q", ParameterLocation.QUERY, STRING, required = true),
                    Parameter("page", "page", ParameterLocation.QUERY, INT32, required = false, default = DefaultValue.Literal("0")),
                    Parameter(
                        "timeZone", "X-TIMEZONE", ParameterLocation.HEADER, STRING, required = false,
                        default = DefaultValue.Literal("Europe/Rome"), description = "Zone the results are localised to.",
                    ),
                ),
                body = null,
                response = ResponseBody.Json(TypeRef.ListOf(TypeRef.Named(item.name)), envelope = listOf("data", "items")),
                server = "Search",
                headers = mapOf("x-client" to "sdk"),
                auth = AuthRequirement(listOf("Token", "Key"), required = false),
            ),
            Operation(
                name = "touch",
                method = HttpMethod.POST,
                path = "/touch",
                parameters = emptyList(),
                body = RequestBody.EmptyObject,
                response = ResponseBody.Json(TypeRef.Primitive(PrimitiveKind.JSON_VALUE)),
                server = "Main",
                auth = AuthRequirement(listOf("Key"), required = true),
            ),
        ),
    )

    private val calendarModule = ApiModule(
        name = "Calendar",
        basePath = "",
        records = listOf(calendar, slot, settings),
        operations = listOf(
            Operation(
                name = "getCalendar",
                method = HttpMethod.GET,
                path = "/regions/{region}/calendar/{day}",
                parameters = listOf(
                    Parameter("day", "day", ParameterLocation.PATH, LOCAL_DATE, required = true),
                    Parameter(
                        "filters", "filters", ParameterLocation.COOKIE, TypeRef.Named(settings.name), required = false,
                        default = DefaultValue.Literal("{}"), encoding = ParameterEncoding.JSON, description = "Sticky filters.",
                    ),
                    Parameter("level", "level", ParameterLocation.QUERY, TypeRef.Named(level.name), required = false),
                ),
                body = null,
                response = ResponseBody.Json(TypeRef.Named(calendar.name)),
                server = "Main",
                headers = mapOf("x-region" to "region-{region}"),
            ),
            Operation(
                name = "saveLabels",
                method = HttpMethod.POST,
                path = "/labels",
                parameters = emptyList(),
                body = RequestBody.Json(TypeRef.MapOf(STRING, STRING)),
                response = ResponseBody.Json(TypeRef.MapOf(STRING, TypeRef.Named(level.name))),
                server = "Main",
            ),
        ),
    )

    private val typesOnly = ApiModule(
        name = "Shared",
        basePath = "/",
        records = emptyList(),
        operations = listOf(
            Operation(
                name = "ping",
                method = HttpMethod.GET,
                path = "/ping",
                parameters = emptyList(),
                body = null,
                response = ResponseBody.Json(STRING),
            ),
        ),
    )

    val demo = ApiSpec(
        id = "demo",
        prefix = "Demo",
        modules = listOf(items, search, calendarModule, typesOnly),
        enums = listOf(kind, level, priority, status),
        servers = listOf(Server("Main", "https://api.example.com/v1"), Server("Search", "https://search.example.com")),
        authSchemes = listOf(
            AuthScheme("Token", listOf(AuthParameter("token")), mapOf("Authorization" to "Bearer {token}"), "A user's bearer token."),
            AuthScheme(
                "Key",
                listOf(AuthParameter("apiKey"), AuthParameter("secret")),
                mapOf("x-api-key" to "{apiKey}", "x-signature" to "{apiKey}:{secret}"),
            ),
        ),
        parameters = listOf(
            Parameter("tenant", "X-TENANT", ParameterLocation.HEADER, STRING, required = true, description = "Tenant every call acts for."),
            Parameter("region", "region", ParameterLocation.PATH, STRING, required = true),
        ),
    )
}
