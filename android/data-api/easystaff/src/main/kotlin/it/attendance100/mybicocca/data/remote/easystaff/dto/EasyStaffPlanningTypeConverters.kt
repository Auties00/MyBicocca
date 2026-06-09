package it.attendance100.mybicocca.data.remote.easystaff.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Decodes booleans the Portale Planning backend encodes inconsistently: native booleans,
 * numeric `0`/`1`, and string `"0"`/`"1"`/`"true"`/`"false"` are all accepted.
 *
 * Values are always serialized back as native booleans.
 */
object TolerantBooleanSerializer : KSerializer<Boolean> {
    override val descriptor = PrimitiveSerialDescriptor("TolerantBoolean", PrimitiveKind.BOOLEAN)

    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeBoolean(value)

    override fun deserialize(decoder: Decoder): Boolean {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeBoolean()
        val primitive = jsonDecoder.decodeJsonElement() as? JsonPrimitive
            ?: return false
        return primitive.booleanOrNull
            ?: primitive.intOrNull?.let { it != 0 }
            ?: (primitive.content == "1" || primitive.content.equals("true", ignoreCase = true))
    }
}

/**
 * Decodes integers the Portale Planning backend encodes inconsistently: native numbers and
 * numeric strings (e.g. `"7"`) are both accepted. Unparsable values decode to zero.
 *
 * Values are always serialized back as native integers.
 */
object TolerantIntSerializer : KSerializer<Int> {
    override val descriptor = PrimitiveSerialDescriptor("TolerantInt", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int) = encoder.encodeInt(value)

    override fun deserialize(decoder: Decoder): Int {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeInt()
        val primitive = jsonDecoder.decodeJsonElement() as? JsonPrimitive
            ?: return 0
        return primitive.intOrNull
            ?: primitive.content.toIntOrNull()
            ?: 0
    }
}

/**
 * Decodes any JSON primitive (string, number, boolean) to its string content, and anything
 * else (null, objects, arrays) to null. Used for fields whose primitive type is not guaranteed
 * by the backend, like reservation codes.
 */
object TolerantStringSerializer : KSerializer<String?> {
    override val descriptor = PrimitiveSerialDescriptor("TolerantString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "")
    }

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeString()
        val primitive = jsonDecoder.decodeJsonElement() as? JsonPrimitive
            ?: return null
        if (primitive is JsonNull) {
            return null
        }
        return primitive.content
    }
}

/**
 * Decodes strings where the backend uses `"0"` or an empty string as a disabled/absent marker
 * (e.g. the public login URL of a portal), mapping those markers to null.
 */
object ZeroAsNullStringSerializer : KSerializer<String?> {
    override val descriptor = PrimitiveSerialDescriptor("ZeroAsNullString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "0")
    }

    override fun deserialize(decoder: Decoder): String? {
        val value = decoder.decodeString()
        return value.takeUnless { it.isBlank() || it == "0" }
    }
}

/**
 * Serializes [LocalDateTime] using the `yyyy-MM-dd HH:mm` format the Portale Planning backend
 * uses for the first availability of a month schedule.
 */
object PlanningLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    override val descriptor = PrimitiveSerialDescriptor("PlanningLocalDateTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalDateTime = LocalDateTime.parse(decoder.decodeString(), formatter)
}

/**
 * Serializes [LocalTime] using the `HH:mm:ss` format the Portale Planning backend uses for
 * service booking deadlines.
 */
object LocalTimeSecondsSerializer : KSerializer<LocalTime> {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    override val descriptor = PrimitiveSerialDescriptor("LocalTimeSeconds", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalTime) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString(), formatter)
}

/**
 * Serializes [EasyStaffPlanningTimeRange] using the `HH:mm-HH:mm` format the Portale Planning
 * backend uses for availability windows and slot labels.
 */
object EasyStaffPlanningTimeRangeSerializer : KSerializer<EasyStaffPlanningTimeRange> {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    override val descriptor = PrimitiveSerialDescriptor("EasyStaffPlanningTimeRange", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EasyStaffPlanningTimeRange) {
        encoder.encodeString("${value.start.format(formatter)}-${value.end.format(formatter)}")
    }

    override fun deserialize(decoder: Decoder): EasyStaffPlanningTimeRange {
        val parts = decoder.decodeString().split("-", limit = 2)
        require(parts.size == 2) {
            "Invalid time range: ${parts.joinToString("-")}"
        }
        return EasyStaffPlanningTimeRange(
            start = LocalTime.parse(parts[0].trim(), formatter),
            end = LocalTime.parse(parts[1].trim(), formatter)
        )
    }
}

// PHP serializes empty associative arrays as [], so empty schedule maps arrive as JSON arrays
private fun emptyArrayAsObject(element: JsonElement): JsonElement =
    if (element is JsonArray && element.isEmpty()) JsonObject(emptyMap()) else element

/**
 * Decodes the day-to-windows maps of schedules and resource availabilities, tolerating the
 * empty-array shape PHP produces for empty maps.
 */
object DateToTimeRangesSerializer : JsonTransformingSerializer<Map<LocalDate, List<EasyStaffPlanningTimeRange>>>(
    MapSerializer(LocalDateSerializer, ListSerializer(EasyStaffPlanningTimeRange.serializer()))
) {
    override fun transformDeserialize(element: JsonElement): JsonElement = emptyArrayAsObject(element)
}

/**
 * Decodes the day-to-slots map of a day schedule, tolerating the empty-array shape PHP
 * produces for empty maps.
 */
object DateToSlotsSerializer : JsonTransformingSerializer<Map<LocalDate, Map<EasyStaffPlanningTimeRange, EasyStaffPlanningSlotAvailability>>>(
    MapSerializer(
        LocalDateSerializer,
        MapSerializer(EasyStaffPlanningTimeRange.serializer(), EasyStaffPlanningSlotAvailability.serializer())
    )
) {
    override fun transformDeserialize(element: JsonElement): JsonElement = emptyArrayAsObject(element)
}

/**
 * Decodes the booking suspension policy of services and areas, which the backend double-encodes
 * as a JSON document inside a JSON string. Empty strings and nulls decode to null.
 */
object SuspensionPolicySerializer : KSerializer<EasyStaffPlanningSuspensionPolicy?> {
    override val descriptor = PrimitiveSerialDescriptor("SuspensionPolicy", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EasyStaffPlanningSuspensionPolicy?) {
        encoder.encodeString(value?.toString() ?: "")
    }

    override fun deserialize(decoder: Decoder): EasyStaffPlanningSuspensionPolicy? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return null
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonObject -> jsonDecoder.json.decodeFromJsonElement(
                EasyStaffPlanningSuspensionPolicy.serializer(),
                element
            )

            is JsonPrimitive -> element.contentOrNull
                ?.takeUnless { it.isBlank() }
                ?.let { jsonDecoder.json.decodeFromString(EasyStaffPlanningSuspensionPolicy.serializer(), it) }

            else -> null
        }
    }
}

/**
 * Decodes the values of a form field, which can be null, a JSON object mapping submission
 * values to display labels (the options of a select), a JSON array of plain options, or a
 * single string (e.g. the privacy policy URL of the GDPR checkbox). Plain options and single
 * strings decode to options whose value and label coincide.
 *
 * Values are always serialized back as a value-to-label JSON object.
 */
object FieldValuesSerializer : KSerializer<List<EasyStaffPlanningFieldOption>?> {
    private val delegate = MapSerializer(String.serializer(), String.serializer())
    override val descriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: List<EasyStaffPlanningFieldOption>?) {
        delegate.serialize(encoder, value.orEmpty().associate { it.value to it.label })
    }

    override fun deserialize(decoder: Decoder): List<EasyStaffPlanningFieldOption>? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return null
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonObject -> element.entries.mapNotNull { (key, value) ->
                (value as? JsonPrimitive)?.contentOrNull
                    ?.let { EasyStaffPlanningFieldOption(value = key, label = it) }
            }

            is JsonArray -> element.mapNotNull { item ->
                (item as? JsonPrimitive)?.contentOrNull
                    ?.let { EasyStaffPlanningFieldOption(value = it, label = it) }
            }

            is JsonPrimitive -> element.contentOrNull
                ?.takeUnless { it.isBlank() }
                ?.let { listOf(EasyStaffPlanningFieldOption(value = it, label = it)) }

            else -> null
        }
    }
}

/**
 * Decodes the entry reference of a booking result, which the backend may report either as a
 * bare numeric identifier or as the full entry object.
 */
object EntryIdSerializer : KSerializer<Int?> {
    override val descriptor = PrimitiveSerialDescriptor("EntryId", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int?) {
        encoder.encodeInt(value ?: 0)
    }

    override fun deserialize(decoder: Decoder): Int? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeInt()
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.intOrNull
            is JsonObject -> (element["id"] as? JsonPrimitive)?.intOrNull
            else -> null
        }
    }
}

/**
 * Decodes a JSON object into a map of its primitive values, used for the user-editable fields
 * of a managed reservation whose value types are not guaranteed by the backend.
 */
object StringValuesMapSerializer : KSerializer<Map<String, String?>> {
    private val delegate = MapSerializer(String.serializer(), TolerantStringSerializer)
    override val descriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Map<String, String?>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Map<String, String?> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return emptyMap()
        val element = jsonDecoder.decodeJsonElement() as? JsonObject
            ?: return emptyMap()
        return element.mapValues { (_, value) -> (value as? JsonPrimitive)?.contentOrNull }
    }
}

// Laravel reports each portal configuration group as an array of single-key objects;
// folding them into one object lets the groups decode as regular classes
private fun foldSettingsGroup(element: JsonElement): JsonElement = when (element) {
    is JsonArray -> JsonObject(
        element.filterIsInstance<JsonObject>()
            .flatMap { it.entries }
            .associate { it.key to it.value }
    )

    else -> element
}

/**
 * Decodes the form field labels of a reservation result, which the backend reports as an
 * array of single-key objects mapping each field code to its display label.
 */
object FieldLabelsSerializer : JsonTransformingSerializer<Map<String, String>>(
    MapSerializer(String.serializer(), String.serializer())
) {
    override fun transformDeserialize(element: JsonElement): JsonElement = foldSettingsGroup(element)
}

/**
 * Decodes the `general` group of a portal configuration from its array-of-single-key-objects
 * wire shape.
 */
object GeneralSettingsSerializer : JsonTransformingSerializer<EasyStaffPlanningGeneralSettings>(
    EasyStaffPlanningGeneralSettings.serializer()
) {
    override fun transformDeserialize(element: JsonElement): JsonElement = foldSettingsGroup(element)
}

/**
 * Decodes the `campi_tipologia_servizi` group of a portal configuration from its
 * array-of-single-key-objects wire shape.
 */
object ServiceCategorySettingsSerializer : JsonTransformingSerializer<EasyStaffPlanningServiceCategorySettings>(
    EasyStaffPlanningServiceCategorySettings.serializer()
) {
    override fun transformDeserialize(element: JsonElement): JsonElement = foldSettingsGroup(element)
}

/**
 * Decodes the `login_pubblico` group of a portal configuration from its
 * array-of-single-key-objects wire shape.
 */
object LoginSettingsSerializer : JsonTransformingSerializer<EasyStaffPlanningLoginSettings>(
    EasyStaffPlanningLoginSettings.serializer()
) {
    override fun transformDeserialize(element: JsonElement): JsonElement = foldSettingsGroup(element)
}

/**
 * Decodes the `griglia` group of a portal configuration from its array-of-single-key-objects
 * wire shape.
 */
object GridSettingsSerializer : JsonTransformingSerializer<EasyStaffPlanningGridSettings>(
    EasyStaffPlanningGridSettings.serializer()
) {
    override fun transformDeserialize(element: JsonElement): JsonElement = foldSettingsGroup(element)
}

/**
 * Decodes the `campi_portale` group of a portal configuration from its
 * array-of-single-key-objects wire shape.
 */
object PortalFieldsSettingsSerializer : JsonTransformingSerializer<EasyStaffPlanningPortalFieldsSettings>(
    EasyStaffPlanningPortalFieldsSettings.serializer()
) {
    override fun transformDeserialize(element: JsonElement): JsonElement = foldSettingsGroup(element)
}

/**
 * Decodes the `portale_pubblico` group of a portal configuration from its
 * array-of-single-key-objects wire shape.
 */
object PublicPortalSettingsSerializer : JsonTransformingSerializer<EasyStaffPlanningPublicPortalSettings>(
    EasyStaffPlanningPublicPortalSettings.serializer()
) {
    override fun transformDeserialize(element: JsonElement): JsonElement = foldSettingsGroup(element)
}

/**
 * Serializes [EasyStaffPlanningReservationType] from the backend label, mapping unknown labels
 * to [EasyStaffPlanningReservationType.Other].
 */
object EasyStaffPlanningReservationTypeSerializer : KSerializer<EasyStaffPlanningReservationType> {
    private const val LBL_CONFIRMED = "CONFERMATA"

    override val descriptor = PrimitiveSerialDescriptor("EasyStaffPlanningReservationType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EasyStaffPlanningReservationType) {
        val stringValue = when (value) {
            is EasyStaffPlanningReservationType.Confirmed -> LBL_CONFIRMED
            is EasyStaffPlanningReservationType.Other -> value.value
        }
        encoder.encodeString(stringValue)
    }

    override fun deserialize(decoder: Decoder): EasyStaffPlanningReservationType {
        return when (val stringValue = decoder.decodeString()) {
            LBL_CONFIRMED -> EasyStaffPlanningReservationType.Confirmed
            else -> EasyStaffPlanningReservationType.Other(stringValue)
        }
    }
}

/**
 * Serializes [EasyStaffPlanningLoginState] from the backend label, mapping unknown labels to
 * [EasyStaffPlanningLoginState.Other].
 */
object EasyStaffPlanningLoginStateSerializer : KSerializer<EasyStaffPlanningLoginState> {
    private const val LBL_OPTIONAL = "facoltativo"
    private const val LBL_MANDATORY = "obbligatorio"

    override val descriptor = PrimitiveSerialDescriptor("EasyStaffPlanningLoginState", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EasyStaffPlanningLoginState) {
        val stringValue = when (value) {
            is EasyStaffPlanningLoginState.Optional -> LBL_OPTIONAL
            is EasyStaffPlanningLoginState.Mandatory -> LBL_MANDATORY
            is EasyStaffPlanningLoginState.Other -> value.value
        }
        encoder.encodeString(stringValue)
    }

    override fun deserialize(decoder: Decoder): EasyStaffPlanningLoginState {
        return when (val stringValue = decoder.decodeString()) {
            LBL_OPTIONAL -> EasyStaffPlanningLoginState.Optional
            LBL_MANDATORY -> EasyStaffPlanningLoginState.Mandatory
            else -> EasyStaffPlanningLoginState.Other(stringValue)
        }
    }
}

/**
 * Serializes [EasyStaffPlanningFieldType] from the backend label, mapping unknown labels to
 * [EasyStaffPlanningFieldType.Other].
 */
object EasyStaffPlanningFieldTypeSerializer : KSerializer<EasyStaffPlanningFieldType> {
    private const val LBL_EMAIL = "email"
    private const val LBL_TEXT = "text"
    private const val LBL_TEXT_AREA = "textarea"
    private const val LBL_PHONE = "tel"
    private const val LBL_CHECKBOX = "checkbox"
    private const val LBL_SELECT = "select"

    override val descriptor = PrimitiveSerialDescriptor("EasyStaffPlanningFieldType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EasyStaffPlanningFieldType) {
        val stringValue = when (value) {
            is EasyStaffPlanningFieldType.Email -> LBL_EMAIL
            is EasyStaffPlanningFieldType.Text -> LBL_TEXT
            is EasyStaffPlanningFieldType.TextArea -> LBL_TEXT_AREA
            is EasyStaffPlanningFieldType.Phone -> LBL_PHONE
            is EasyStaffPlanningFieldType.Checkbox -> LBL_CHECKBOX
            is EasyStaffPlanningFieldType.Select -> LBL_SELECT
            is EasyStaffPlanningFieldType.Other -> value.value
        }
        encoder.encodeString(stringValue)
    }

    override fun deserialize(decoder: Decoder): EasyStaffPlanningFieldType {
        return when (val stringValue = decoder.decodeString()) {
            LBL_EMAIL -> EasyStaffPlanningFieldType.Email
            LBL_TEXT -> EasyStaffPlanningFieldType.Text
            LBL_TEXT_AREA -> EasyStaffPlanningFieldType.TextArea
            LBL_PHONE -> EasyStaffPlanningFieldType.Phone
            LBL_CHECKBOX -> EasyStaffPlanningFieldType.Checkbox
            LBL_SELECT -> EasyStaffPlanningFieldType.Select
            else -> EasyStaffPlanningFieldType.Other(stringValue)
        }
    }
}

/**
 * Serializes [EasyStaffPlanningFieldPosition] from the backend numeric code, mapping unknown
 * codes to [EasyStaffPlanningFieldPosition.Other].
 */
object EasyStaffPlanningFieldPositionSerializer : KSerializer<EasyStaffPlanningFieldPosition> {
    private const val CODE_USER = 1
    private const val CODE_SERVICE = 2
    private const val CODE_GDPR = 100

    override val descriptor = PrimitiveSerialDescriptor("EasyStaffPlanningFieldPosition", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: EasyStaffPlanningFieldPosition) {
        val intValue = when (value) {
            is EasyStaffPlanningFieldPosition.User -> CODE_USER
            is EasyStaffPlanningFieldPosition.Service -> CODE_SERVICE
            is EasyStaffPlanningFieldPosition.Gdpr -> CODE_GDPR
            is EasyStaffPlanningFieldPosition.Other -> value.value
        }
        encoder.encodeInt(intValue)
    }

    override fun deserialize(decoder: Decoder): EasyStaffPlanningFieldPosition {
        return when (val intValue = decoder.decodeInt()) {
            CODE_USER -> EasyStaffPlanningFieldPosition.User
            CODE_SERVICE -> EasyStaffPlanningFieldPosition.Service
            CODE_GDPR -> EasyStaffPlanningFieldPosition.Gdpr
            else -> EasyStaffPlanningFieldPosition.Other(intValue)
        }
    }
}
