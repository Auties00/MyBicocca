package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = Esse3AcknowledgmentOfReceipt.Serializer::class)
sealed interface Esse3AcknowledgmentOfReceipt {
    val value: String

    data object NotViewed : Esse3AcknowledgmentOfReceipt { override val value = "N" }
    data object Viewed : Esse3AcknowledgmentOfReceipt { override val value = "V" }
    data object Accepted : Esse3AcknowledgmentOfReceipt { override val value = "A" }
    data object Rejected : Esse3AcknowledgmentOfReceipt { override val value = "R" }
    data class Unknown(override val value: String) : Esse3AcknowledgmentOfReceipt

    object Serializer : KSerializer<Esse3AcknowledgmentOfReceipt> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3AcknowledgmentOfReceipt", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3AcknowledgmentOfReceipt {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "N" -> NotViewed
                "V" -> Viewed
                "A" -> Accepted
                "R" -> Rejected
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3AcknowledgmentOfReceipt) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3AverageTypeCode.Serializer::class)
sealed interface Esse3AverageTypeCode {
    val value: String

    data object Arithmetic : Esse3AverageTypeCode { override val value = "A" }
    data object Weighted : Esse3AverageTypeCode { override val value = "P" }
    data class Unknown(override val value: String) : Esse3AverageTypeCode

    object Serializer : KSerializer<Esse3AverageTypeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3AverageTypeCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3AverageTypeCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "A" -> Arithmetic
                "P" -> Weighted
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3AverageTypeCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3BadgeMatriculationStateFilter.Serializer::class)
sealed interface Esse3BadgeMatriculationStateFilter {
    val value: String

    data object A : Esse3BadgeMatriculationStateFilter { override val value = "A" }
    data object S : Esse3BadgeMatriculationStateFilter { override val value = "S" }
    data object I : Esse3BadgeMatriculationStateFilter { override val value = "I" }
    data class Unknown(override val value: String) : Esse3BadgeMatriculationStateFilter

    object Serializer : KSerializer<Esse3BadgeMatriculationStateFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3BadgeMatriculationStateFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3BadgeMatriculationStateFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "A" -> A
                "S" -> S
                "I" -> I
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3BadgeMatriculationStateFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3BadgeStudentStateFilter.Serializer::class)
sealed interface Esse3BadgeStudentStateFilter {
    val value: String

    data object A : Esse3BadgeStudentStateFilter { override val value = "A" }
    data object S : Esse3BadgeStudentStateFilter { override val value = "S" }
    data object X : Esse3BadgeStudentStateFilter { override val value = "X" }
    data object I : Esse3BadgeStudentStateFilter { override val value = "I" }
    data class Unknown(override val value: String) : Esse3BadgeStudentStateFilter

    object Serializer : KSerializer<Esse3BadgeStudentStateFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3BadgeStudentStateFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3BadgeStudentStateFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "A" -> A
                "S" -> S
                "X" -> X
                "I" -> I
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3BadgeStudentStateFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3BaseDefinition.Serializer::class)
sealed interface Esse3BaseDefinition {
    val value: String

    data object DegreeCourse : Esse3BaseDefinition { override val value = "CDS" }
    data object DegreeCourseRegulations : Esse3BaseDefinition { override val value = "CDSORD" }
    data class Unknown(override val value: String) : Esse3BaseDefinition

    object Serializer : KSerializer<Esse3BaseDefinition> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3BaseDefinition", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3BaseDefinition {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "CDS" -> DegreeCourse
                "CDSORD" -> DegreeCourseRegulations
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3BaseDefinition) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3BookableExamFilter.Serializer::class)
sealed interface Esse3BookableExamFilter {
    val value: String

    /** recupera tutti gli appelli che non hanno nessuna prenotazione e risultano prenotabili */
    data object AppelliPrenotabili : Esse3BookableExamFilter { override val value = "APPELLI_PRENOTABILI" }
    /** recupera tutti gli appelli che non hanno nessuna prenotazione e risultano prenotabili o futuri */
    data object AppelliPrenotabiliEFuturi : Esse3BookableExamFilter { override val value = "APPELLI_PRENOTABILI_E_FUTURI" }
    data class Unknown(override val value: String) : Esse3BookableExamFilter

    object Serializer : KSerializer<Esse3BookableExamFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3BookableExamFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3BookableExamFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "APPELLI_PRENOTABILI" -> AppelliPrenotabili
                "APPELLI_PRENOTABILI_E_FUTURI" -> AppelliPrenotabiliEFuturi
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3BookableExamFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3BookingFilter.Serializer::class)
sealed interface Esse3BookingFilter {
    val value: String

    /** prenotazioni visualizzate in bacheca esiti (appelli con pubblicazone, esiti pubblicati, data_ultimo_rif trascora, il verbale collegato non è stato generato) */
    data object BachecaEsiti : Esse3BookingFilter { override val value = "BACHECA_ESITI" }
    data class Unknown(override val value: String) : Esse3BookingFilter

    object Serializer : KSerializer<Esse3BookingFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3BookingFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3BookingFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "BACHECA_ESITI" -> BachecaEsiti
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3BookingFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3ChoiceRulesStateFilter.Serializer::class)
sealed interface Esse3ChoiceRulesStateFilter {
    val value: String

    data object A : Esse3ChoiceRulesStateFilter { override val value = "A" }
    data object B : Esse3ChoiceRulesStateFilter { override val value = "B" }
    data object X : Esse3ChoiceRulesStateFilter { override val value = "X" }
    data class Unknown(override val value: String) : Esse3ChoiceRulesStateFilter

    object Serializer : KSerializer<Esse3ChoiceRulesStateFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3ChoiceRulesStateFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3ChoiceRulesStateFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "A" -> A
                "B" -> B
                "X" -> X
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3ChoiceRulesStateFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3ChoiceType.Serializer::class)
sealed interface Esse3ChoiceType {
    val value: String

    data object Obligatory : Esse3ChoiceType { override val value = "O" }
    data object FreeChoice : Esse3ChoiceType { override val value = "F" }
    data object TableBased : Esse3ChoiceType { override val value = "T" }
    data object GroupChoice : Esse3ChoiceType { override val value = "G" }
    data object WebChoice : Esse3ChoiceType { override val value = "W" }
    data object Substitutive : Esse3ChoiceType { override val value = "S" }
    data object Optional : Esse3ChoiceType { override val value = "V" }
    data object Restricted : Esse3ChoiceType { override val value = "D" }
    data class Unknown(override val value: String) : Esse3ChoiceType

    object Serializer : KSerializer<Esse3ChoiceType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3ChoiceType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3ChoiceType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "O" -> Obligatory
                "F" -> FreeChoice
                "T" -> TableBased
                "G" -> GroupChoice
                "W" -> WebChoice
                "S" -> Substitutive
                "V" -> Optional
                "D" -> Restricted
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3ChoiceType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3ChoiceType2.Serializer::class)
sealed interface Esse3ChoiceType2 {
    val value: String

    data object Obligatory : Esse3ChoiceType2 { override val value = "O" }
    data object FreeChoice : Esse3ChoiceType2 { override val value = "F" }
    data object TableBased : Esse3ChoiceType2 { override val value = "T" }
    data object GroupChoice : Esse3ChoiceType2 { override val value = "G" }
    data object WebChoice : Esse3ChoiceType2 { override val value = "W" }
    data object Substitutive : Esse3ChoiceType2 { override val value = "S" }
    data object Optional : Esse3ChoiceType2 { override val value = "V" }
    data object Restricted : Esse3ChoiceType2 { override val value = "D" }
    data object ExtraCurricular : Esse3ChoiceType2 { override val value = "E" }
    data object Individual : Esse3ChoiceType2 { override val value = "I" }
    data object Requisite : Esse3ChoiceType2 { override val value = "R" }
    data class Unknown(override val value: String) : Esse3ChoiceType2

    object Serializer : KSerializer<Esse3ChoiceType2> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3ChoiceType2", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3ChoiceType2 {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "O" -> Obligatory
                "F" -> FreeChoice
                "T" -> TableBased
                "G" -> GroupChoice
                "W" -> WebChoice
                "S" -> Substitutive
                "V" -> Optional
                "D" -> Restricted
                "E" -> ExtraCurricular
                "I" -> Individual
                "R" -> Requisite
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3ChoiceType2) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3CompanyConventionStateFilter.Serializer::class)
sealed interface Esse3CompanyConventionStateFilter {
    val value: String

    data object P : Esse3CompanyConventionStateFilter { override val value = "P" }
    data object I : Esse3CompanyConventionStateFilter { override val value = "I" }
    data object X : Esse3CompanyConventionStateFilter { override val value = "X" }
    data object R : Esse3CompanyConventionStateFilter { override val value = "R" }
    data class Unknown(override val value: String) : Esse3CompanyConventionStateFilter

    object Serializer : KSerializer<Esse3CompanyConventionStateFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3CompanyConventionStateFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3CompanyConventionStateFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "P" -> P
                "I" -> I
                "X" -> X
                "R" -> R
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3CompanyConventionStateFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3CompanySiteTypeFilter.Serializer::class)
sealed interface Esse3CompanySiteTypeFilter {
    val value: String

    data object Leg : Esse3CompanySiteTypeFilter { override val value = "LEG" }
    data object Ope : Esse3CompanySiteTypeFilter { override val value = "OPE" }
    data object Per : Esse3CompanySiteTypeFilter { override val value = "PER" }
    data class Unknown(override val value: String) : Esse3CompanySiteTypeFilter

    object Serializer : KSerializer<Esse3CompanySiteTypeFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3CompanySiteTypeFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3CompanySiteTypeFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "LEG" -> Leg
                "OPE" -> Ope
                "PER" -> Per
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3CompanySiteTypeFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3CompensatoryMeasureFilter.Serializer::class)
sealed interface Esse3CompensatoryMeasureFilter {
    val value: String

    /** recupera solo le misure compensative valide per un determinato appello */
    data object Prenotazione : Esse3CompensatoryMeasureFilter { override val value = "PRENOTAZIONE" }
    data class Unknown(override val value: String) : Esse3CompensatoryMeasureFilter

    object Serializer : KSerializer<Esse3CompensatoryMeasureFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3CompensatoryMeasureFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3CompensatoryMeasureFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "PRENOTAZIONE" -> Prenotazione
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3CompensatoryMeasureFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3CompetitionRankingFileType.Serializer::class)
sealed interface Esse3CompetitionRankingFileType {
    val value: String

    data object Std : Esse3CompetitionRankingFileType { override val value = "STD" }
    data object MedCineca : Esse3CompetitionRankingFileType { override val value = "MED_CINECA" }
    data class Unknown(override val value: String) : Esse3CompetitionRankingFileType

    object Serializer : KSerializer<Esse3CompetitionRankingFileType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3CompetitionRankingFileType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3CompetitionRankingFileType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "STD" -> Std
                "MED_CINECA" -> MedCineca
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3CompetitionRankingFileType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3ComputerBasedTestAuthorization.Serializer::class)
sealed interface Esse3ComputerBasedTestAuthorization {
    val value: String

    data object None : Esse3ComputerBasedTestAuthorization { override val value = "N" }
    data object ComputerBasedTest : Esse3ComputerBasedTestAuthorization { override val value = "C" }
    data object Tablet : Esse3ComputerBasedTestAuthorization { override val value = "T" }
    data class Unknown(override val value: String) : Esse3ComputerBasedTestAuthorization

    object Serializer : KSerializer<Esse3ComputerBasedTestAuthorization> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3ComputerBasedTestAuthorization", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3ComputerBasedTestAuthorization {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "N" -> None
                "C" -> ComputerBasedTest
                "T" -> Tablet
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3ComputerBasedTestAuthorization) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3ConditionTypeCode.Serializer::class)
sealed interface Esse3ConditionTypeCode {
    val value: String

    data object YearOfCourse : Esse3ConditionTypeCode { override val value = "A" }
    data object RequiredCredits : Esse3ConditionTypeCode { override val value = "B" }
    data object PassedExams : Esse3ConditionTypeCode { override val value = "P" }
    data object EducationalRules : Esse3ConditionTypeCode { override val value = "R" }
    data object BlockingStatus : Esse3ConditionTypeCode { override val value = "S" }
    data class Unknown(override val value: String) : Esse3ConditionTypeCode

    object Serializer : KSerializer<Esse3ConditionTypeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3ConditionTypeCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3ConditionTypeCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "A" -> YearOfCourse
                "B" -> RequiredCredits
                "P" -> PassedExams
                "R" -> EducationalRules
                "S" -> BlockingStatus
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3ConditionTypeCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3CourseOfStudySearchFilter.Serializer::class)
sealed interface Esse3CourseOfStudySearchFilter {
    val value: String

    /** recupera solo se strutture con def_amm_flg e def_amm_old_flg = 1 */
    data object StruttureDefAmm : Esse3CourseOfStudySearchFilter { override val value = "STRUTTURE_DEF_AMM" }
    data class Unknown(override val value: String) : Esse3CourseOfStudySearchFilter

    object Serializer : KSerializer<Esse3CourseOfStudySearchFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3CourseOfStudySearchFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3CourseOfStudySearchFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "STRUTTURE_DEF_AMM" -> StruttureDefAmm
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3CourseOfStudySearchFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3ElementType.Serializer::class)
sealed interface Esse3ElementType {
    val value: String

    data object ActivityCourse : Esse3ElementType { override val value = "AC" }
    data object DidacticActivity : Esse3ElementType { override val value = "AD" }
    data object ScientificSector : Esse3ElementType { override val value = "SET" }
    data class Unknown(override val value: String) : Esse3ElementType

    object Serializer : KSerializer<Esse3ElementType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3ElementType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3ElementType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "AC" -> ActivityCourse
                "AD" -> DidacticActivity
                "SET" -> ScientificSector
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3ElementType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3EnrollmentTypeCode.Serializer::class)
sealed interface Esse3EnrollmentTypeCode {
    val value: String

    data object Written : Esse3EnrollmentTypeCode { override val value = "S" }
    data object Oral : Esse3EnrollmentTypeCode { override val value = "O" }
    data object WrittenAndOral : Esse3EnrollmentTypeCode { override val value = "SO" }
    data class Unknown(override val value: String) : Esse3EnrollmentTypeCode

    object Serializer : KSerializer<Esse3EnrollmentTypeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3EnrollmentTypeCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3EnrollmentTypeCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "S" -> Written
                "O" -> Oral
                "SO" -> WrittenAndOral
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3EnrollmentTypeCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3EnvironmentType.Serializer::class)
sealed interface Esse3EnvironmentType {
    val value: String

    data object Local : Esse3EnvironmentType { override val value = "LOCAL" }
    data object Certification : Esse3EnvironmentType { override val value = "CR" }
    data object Development1 : Esse3EnvironmentType { override val value = "DEV1" }
    data object Development2 : Esse3EnvironmentType { override val value = "DEV2" }
    data object PreProduction : Esse3EnvironmentType { override val value = "PREPROD" }
    data object Production : Esse3EnvironmentType { override val value = "PROD" }
    data object Unknown : Esse3EnvironmentType { override val value = "UNKNOWN" }

    object Serializer : KSerializer<Esse3EnvironmentType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3EnvironmentType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3EnvironmentType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "LOCAL" -> Local
                "CR" -> Certification
                "DEV1" -> Development1
                "DEV2" -> Development2
                "PREPROD" -> PreProduction
                "PROD" -> Production
                "UNKNOWN" -> Unknown
                else -> Unknown
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3EnvironmentType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3EvaluationModeCode.Serializer::class)
sealed interface Esse3EvaluationModeCode {
    val value: String

    data object GradeThirtieths : Esse3EvaluationModeCode { override val value = "V" }
    data object JudgmentPassFail : Esse3EvaluationModeCode { override val value = "G" }
    data object NotEvaluated : Esse3EvaluationModeCode { override val value = "N" }
    data class Unknown(override val value: String) : Esse3EvaluationModeCode

    object Serializer : KSerializer<Esse3EvaluationModeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3EvaluationModeCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3EvaluationModeCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "V" -> GradeThirtieths
                "G" -> JudgmentPassFail
                "N" -> NotEvaluated
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3EvaluationModeCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3ExamCallSearchFilter.Serializer::class)
sealed interface Esse3ExamCallSearchFilter {
    val value: String

    /** recupera tutti gli appelli che non hanno nessuna prenotazione e risultano prenotabili (vale solo per user = STUDENTE e TECNICO) */
    data object AppelliPrenotabili : Esse3ExamCallSearchFilter { override val value = "APPELLI_PRENOTABILI" }
    /** recupera tutti gli appelli che risultano integrati con moodle per la gestione dei QUIZ */
    data object AppelliMoodleQuiz : Esse3ExamCallSearchFilter { override val value = "APPELLI_MOODLE_QUIZ" }
    /** recupera tutti gli appelli che non hanno nessuna prenotazione e risultano prenotabili o futuri (vale solo per user = STUDENTE e TECNICO) */
    data object AppelliPrenotabiliEFuturi : Esse3ExamCallSearchFilter { override val value = "APPELLI_PRENOTABILI_E_FUTURI" }
    data class Unknown(override val value: String) : Esse3ExamCallSearchFilter

    object Serializer : KSerializer<Esse3ExamCallSearchFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3ExamCallSearchFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3ExamCallSearchFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "APPELLI_PRENOTABILI" -> AppelliPrenotabili
                "APPELLI_MOODLE_QUIZ" -> AppelliMoodleQuiz
                "APPELLI_PRENOTABILI_E_FUTURI" -> AppelliPrenotabiliEFuturi
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3ExamCallSearchFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3ExternalEntityTypeFilter.Serializer::class)
sealed interface Esse3ExternalEntityTypeFilter {
    val value: String

    data object Azienda : Esse3ExternalEntityTypeFilter { override val value = "AZIENDA" }
    data object Agenzia : Esse3ExternalEntityTypeFilter { override val value = "AGENZIA" }
    data object All : Esse3ExternalEntityTypeFilter { override val value = "ALL" }
    data class Unknown(override val value: String) : Esse3ExternalEntityTypeFilter

    object Serializer : KSerializer<Esse3ExternalEntityTypeFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3ExternalEntityTypeFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3ExternalEntityTypeFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "AZIENDA" -> Azienda
                "AGENZIA" -> Agenzia
                "ALL" -> All
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3ExternalEntityTypeFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3FieldName.Serializer::class)
sealed interface Esse3FieldName {
    val value: String

    data object Contents : Esse3FieldName { override val value = "CONTENUTI" }
    data object LearningObjectives : Esse3FieldName { override val value = "OBIETTIVI_FORMATIVI" }
    data object Prerequisites : Esse3FieldName { override val value = "PREREQUISITI" }
    data object TeachingMethods : Esse3FieldName { override val value = "METODI_DIDATTICI" }
    data object AssessmentMethods : Esse3FieldName { override val value = "MODALITA_VERIFICA_APPRENDIMENTO" }
    data object OtherInformation : Esse3FieldName { override val value = "ALTRE_INFO" }
    data object ReferenceTexts : Esse3FieldName { override val value = "TESTI_RIFERIMENTO" }
    data object SyllabusOption1 : Esse3FieldName { override val value = "SYLLABUS_OPT_1" }
    data object SyllabusOption2 : Esse3FieldName { override val value = "SYLLABUS_OPT_2" }
    data object SyllabusOption3 : Esse3FieldName { override val value = "SYLLABUS_OPT_3" }
    data object SustainableDevelopmentGoalsDescription : Esse3FieldName { override val value = "OBIETTIVI_SVIL_SOSTENIBILE_DES" }
    data class Unknown(override val value: String) : Esse3FieldName

    object Serializer : KSerializer<Esse3FieldName> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3FieldName", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3FieldName {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "CONTENUTI" -> Contents
                "OBIETTIVI_FORMATIVI" -> LearningObjectives
                "PREREQUISITI" -> Prerequisites
                "METODI_DIDATTICI" -> TeachingMethods
                "MODALITA_VERIFICA_APPRENDIMENTO" -> AssessmentMethods
                "ALTRE_INFO" -> OtherInformation
                "TESTI_RIFERIMENTO" -> ReferenceTexts
                "SYLLABUS_OPT_1" -> SyllabusOption1
                "SYLLABUS_OPT_2" -> SyllabusOption2
                "SYLLABUS_OPT_3" -> SyllabusOption3
                "OBIETTIVI_SVIL_SOSTENIBILE_DES" -> SustainableDevelopmentGoalsDescription
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3FieldName) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3GraduationGroupType.Serializer::class)
sealed interface Esse3GraduationGroupType {
    val value: String

    data object Examination : Esse3GraduationGroupType { override val value = "ESA" }
    data object Attendance : Esse3GraduationGroupType { override val value = "FREQ" }
    data class Unknown(override val value: String) : Esse3GraduationGroupType

    object Serializer : KSerializer<Esse3GraduationGroupType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3GraduationGroupType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3GraduationGroupType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "ESA" -> Examination
                "FREQ" -> Attendance
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3GraduationGroupType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3GraduationTypeCode.Serializer::class)
sealed interface Esse3GraduationTypeCode {
    val value: String

    data object Written : Esse3GraduationTypeCode { override val value = "S" }
    data object Oral : Esse3GraduationTypeCode { override val value = "O" }
    data object WrittenOralConsecutive : Esse3GraduationTypeCode { override val value = "SOC" }
    data object WrittenOralSimultaneous : Esse3GraduationTypeCode { override val value = "SOS" }
    data class Unknown(override val value: String) : Esse3GraduationTypeCode

    object Serializer : KSerializer<Esse3GraduationTypeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3GraduationTypeCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3GraduationTypeCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "S" -> Written
                "O" -> Oral
                "SOC" -> WrittenOralConsecutive
                "SOS" -> WrittenOralSimultaneous
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3GraduationTypeCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3InternshipApplicationStateCode.Serializer::class)
sealed interface Esse3InternshipApplicationStateCode {
    val value: String

    data object PreEnrolled : Esse3InternshipApplicationStateCode { override val value = "PRE" }
    data object Closed : Esse3InternshipApplicationStateCode { override val value = "CHI" }
    data object Cancelled : Esse3InternshipApplicationStateCode { override val value = "ANN" }
    data object Confirmed : Esse3InternshipApplicationStateCode { override val value = "CON" }
    data object Rejected : Esse3InternshipApplicationStateCode { override val value = "RIF" }
    data object Started : Esse3InternshipApplicationStateCode { override val value = "AVV" }
    data object NotAssigned : Esse3InternshipApplicationStateCode { override val value = "NAS" }
    data class Unknown(override val value: String) : Esse3InternshipApplicationStateCode

    object Serializer : KSerializer<Esse3InternshipApplicationStateCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3InternshipApplicationStateCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3InternshipApplicationStateCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "PRE" -> PreEnrolled
                "CHI" -> Closed
                "ANN" -> Cancelled
                "CON" -> Confirmed
                "RIF" -> Rejected
                "AVV" -> Started
                "NAS" -> NotAssigned
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3InternshipApplicationStateCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3InternshipCompanyStateFilter.Serializer::class)
sealed interface Esse3InternshipCompanyStateFilter {
    val value: String

    data object A : Esse3InternshipCompanyStateFilter { override val value = "A" }
    data object B : Esse3InternshipCompanyStateFilter { override val value = "B" }
    data object Bl : Esse3InternshipCompanyStateFilter { override val value = "BL" }
    data object C : Esse3InternshipCompanyStateFilter { override val value = "C" }
    data object P : Esse3InternshipCompanyStateFilter { override val value = "P" }
    data object R : Esse3InternshipCompanyStateFilter { override val value = "R" }
    data class Unknown(override val value: String) : Esse3InternshipCompanyStateFilter

    object Serializer : KSerializer<Esse3InternshipCompanyStateFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3InternshipCompanyStateFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3InternshipCompanyStateFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "A" -> A
                "B" -> B
                "BL" -> Bl
                "C" -> C
                "P" -> P
                "R" -> R
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3InternshipCompanyStateFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3InternshipEligibilityLanguageCode.Serializer::class)
sealed interface Esse3InternshipEligibilityLanguageCode {
    val value: String

    data object Ita : Esse3InternshipEligibilityLanguageCode { override val value = "ita" }
    data object Eng : Esse3InternshipEligibilityLanguageCode { override val value = "eng" }
    data class Unknown(override val value: String) : Esse3InternshipEligibilityLanguageCode

    object Serializer : KSerializer<Esse3InternshipEligibilityLanguageCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3InternshipEligibilityLanguageCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3InternshipEligibilityLanguageCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "ita" -> Ita
                "eng" -> Eng
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3InternshipEligibilityLanguageCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3InternshipEligibilityServiceType.Serializer::class)
sealed interface Esse3InternshipEligibilityServiceType {
    val value: String

    data object TirocinioCurricolare : Esse3InternshipEligibilityServiceType { override val value = "tirocinio_curricolare" }
    data object TirocinioPostlaurea : Esse3InternshipEligibilityServiceType { override val value = "tirocinio_postlaurea" }
    data class Unknown(override val value: String) : Esse3InternshipEligibilityServiceType

    object Serializer : KSerializer<Esse3InternshipEligibilityServiceType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3InternshipEligibilityServiceType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3InternshipEligibilityServiceType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "tirocinio_curricolare" -> TirocinioCurricolare
                "tirocinio_postlaurea" -> TirocinioPostlaurea
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3InternshipEligibilityServiceType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3InternshipEvaluationQuestionnaireTypeFilter.Serializer::class)
sealed interface Esse3InternshipEvaluationQuestionnaireTypeFilter {
    val value: String

    data object Vf : Esse3InternshipEvaluationQuestionnaireTypeFilter { override val value = "VF" }
    data object Rf : Esse3InternshipEvaluationQuestionnaireTypeFilter { override val value = "RF" }
    data object Mt : Esse3InternshipEvaluationQuestionnaireTypeFilter { override val value = "MT" }
    data class Unknown(override val value: String) : Esse3InternshipEvaluationQuestionnaireTypeFilter

    object Serializer : KSerializer<Esse3InternshipEvaluationQuestionnaireTypeFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3InternshipEvaluationQuestionnaireTypeFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3InternshipEvaluationQuestionnaireTypeFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "VF" -> Vf
                "RF" -> Rf
                "MT" -> Mt
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3InternshipEvaluationQuestionnaireTypeFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3Kind.Serializer::class)
sealed interface Esse3Kind {
    val value: String

    data object Basic : Esse3Kind { override val value = "BASIC" }
    data object Bearer : Esse3Kind { override val value = "BEARER" }
    data class Unknown(override val value: String) : Esse3Kind

    object Serializer : KSerializer<Esse3Kind> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3Kind", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3Kind {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "BASIC" -> Basic
                "BEARER" -> Bearer
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3Kind) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3LecturerExamCallFilter.Serializer::class)
sealed interface Esse3LecturerExamCallFilter {
    val value: String

    /** recupera tutti gli appelli che risultano integrati con moodle per la gestione dei QUIZ */
    data object AppelliMoodleQuiz : Esse3LecturerExamCallFilter { override val value = "APPELLI_MOODLE_QUIZ" }
    data class Unknown(override val value: String) : Esse3LecturerExamCallFilter

    object Serializer : KSerializer<Esse3LecturerExamCallFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3LecturerExamCallFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3LecturerExamCallFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "APPELLI_MOODLE_QUIZ" -> AppelliMoodleQuiz
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3LecturerExamCallFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3Level.Serializer::class)
sealed interface Esse3Level {
    val value: String

    data object Info : Esse3Level { override val value = "INFO" }
    data object Warning : Esse3Level { override val value = "WARN" }
    data object Error : Esse3Level { override val value = "ERROR" }
    data class Unknown(override val value: String) : Esse3Level

    object Serializer : KSerializer<Esse3Level> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3Level", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3Level {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "INFO" -> Info
                "WARN" -> Warning
                "ERROR" -> Error
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3Level) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3LinkedBookingOperation.Serializer::class)
sealed interface Esse3LinkedBookingOperation {
    val value: String

    data object Insert : Esse3LinkedBookingOperation { override val value = "INSERT" }
    data object Delete : Esse3LinkedBookingOperation { override val value = "DELETE" }
    data object Update : Esse3LinkedBookingOperation { override val value = "UPDATE" }
    data object DeleteAll : Esse3LinkedBookingOperation { override val value = "DELETE_ALL" }
    data class Unknown(override val value: String) : Esse3LinkedBookingOperation

    object Serializer : KSerializer<Esse3LinkedBookingOperation> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3LinkedBookingOperation", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3LinkedBookingOperation {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "INSERT" -> Insert
                "DELETE" -> Delete
                "UPDATE" -> Update
                "DELETE_ALL" -> DeleteAll
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3LinkedBookingOperation) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3MeasurementUnit1.Serializer::class)
sealed interface Esse3MeasurementUnit1 {
    val value: String

    data object EctsCredits : Esse3MeasurementUnit1 { override val value = "CFU" }
    data object DidacticUnit : Esse3MeasurementUnit1 { override val value = "UD" }
    data class Unknown(override val value: String) : Esse3MeasurementUnit1

    object Serializer : KSerializer<Esse3MeasurementUnit1> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3MeasurementUnit1", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3MeasurementUnit1 {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "CFU" -> EctsCredits
                "UD" -> DidacticUnit
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3MeasurementUnit1) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3MeasurementUnitCode.Serializer::class)
sealed interface Esse3MeasurementUnitCode {
    val value: String

    data object DidacticActivity : Esse3MeasurementUnitCode { override val value = "AD" }
    data object EctsCredits : Esse3MeasurementUnitCode { override val value = "CFU" }
    data class Unknown(override val value: String) : Esse3MeasurementUnitCode

    object Serializer : KSerializer<Esse3MeasurementUnitCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3MeasurementUnitCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3MeasurementUnitCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "AD" -> DidacticActivity
                "CFU" -> EctsCredits
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3MeasurementUnitCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3MinutesState.Serializer::class)
sealed interface Esse3MinutesState {
    val value: String

    data object ToBeProcessed : Esse3MinutesState { override val value = "C" }
    data object InProgress : Esse3MinutesState { override val value = "A" }
    data object Completed : Esse3MinutesState { override val value = "F" }
    data class Unknown(override val value: String) : Esse3MinutesState

    object Serializer : KSerializer<Esse3MinutesState> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3MinutesState", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3MinutesState {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "C" -> ToBeProcessed
                "A" -> InProgress
                "F" -> Completed
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3MinutesState) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3OperationType.Serializer::class)
sealed interface Esse3OperationType {
    val value: String

    data object Insert : Esse3OperationType { override val value = "INSERT" }
    data object Update : Esse3OperationType { override val value = "UPDATE" }
    data object Delete : Esse3OperationType { override val value = "DELETE" }
    data class Unknown(override val value: String) : Esse3OperationType

    object Serializer : KSerializer<Esse3OperationType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3OperationType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3OperationType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "INSERT" -> Insert
                "UPDATE" -> Update
                "DELETE" -> Delete
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3OperationType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3OutcomesInsertionState.Serializer::class)
sealed interface Esse3OutcomesInsertionState {
    val value: String

    data object Closed : Esse3OutcomesInsertionState { override val value = "C" }
    data object Open : Esse3OutcomesInsertionState { override val value = "A" }
    data object Finalized : Esse3OutcomesInsertionState { override val value = "F" }
    data class Unknown(override val value: String) : Esse3OutcomesInsertionState

    object Serializer : KSerializer<Esse3OutcomesInsertionState> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3OutcomesInsertionState", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3OutcomesInsertionState {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "C" -> Closed
                "A" -> Open
                "F" -> Finalized
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3OutcomesInsertionState) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3OutcomesPublicationState.Serializer::class)
sealed interface Esse3OutcomesPublicationState {
    val value: String

    data object NotPublished : Esse3OutcomesPublicationState { override val value = "C" }
    data object Published : Esse3OutcomesPublicationState { override val value = "A" }
    data object Definitive : Esse3OutcomesPublicationState { override val value = "F" }
    data class Unknown(override val value: String) : Esse3OutcomesPublicationState

    object Serializer : KSerializer<Esse3OutcomesPublicationState> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3OutcomesPublicationState", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3OutcomesPublicationState {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "C" -> NotPublished
                "A" -> Published
                "F" -> Definitive
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3OutcomesPublicationState) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3PlanType.Serializer::class)
sealed interface Esse3PlanType {
    val value: String

    data object Standard : Esse3PlanType { override val value = "S" }
    data object Individual : Esse3PlanType { override val value = "I" }
    data class Unknown(override val value: String) : Esse3PlanType

    object Serializer : KSerializer<Esse3PlanType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3PlanType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3PlanType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "S" -> Standard
                "I" -> Individual
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3PlanType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3Profile.Serializer::class)
sealed interface Esse3Profile {
    val value: String

    data object Guest : Esse3Profile { override val value = "GUEST" }
    data object Student : Esse3Profile { override val value = "STUDENTE" }
    data object Professor : Esse3Profile { override val value = "DOCENTE" }
    data object TechnicalUser : Esse3Profile { override val value = "USER_TECNICO" }
    data object ProvisionalEnrollment : Esse3Profile { override val value = "IMMATRICOLATI_IN_IPOTESI" }
    data object RegisteredUser : Esse3Profile { override val value = "REGISTRATO" }
    data class Unknown(override val value: String) : Esse3Profile

    object Serializer : KSerializer<Esse3Profile> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3Profile", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3Profile {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "GUEST" -> Guest
                "STUDENTE" -> Student
                "DOCENTE" -> Professor
                "USER_TECNICO" -> TechnicalUser
                "IMMATRICOLATI_IN_IPOTESI" -> ProvisionalEnrollment
                "REGISTRATO" -> RegisteredUser
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3Profile) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3QuestionStateCode.Serializer::class)
sealed interface Esse3QuestionStateCode {
    val value: String

    data object Active : Esse3QuestionStateCode { override val value = "A" }
    data object Draft : Esse3QuestionStateCode { override val value = "B" }
    data class Unknown(override val value: String) : Esse3QuestionStateCode

    object Serializer : KSerializer<Esse3QuestionStateCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3QuestionStateCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3QuestionStateCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "A" -> Active
                "B" -> Draft
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3QuestionStateCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3RecordBookRecognitionType.Serializer::class)
sealed interface Esse3RecordBookRecognitionType {
    val value: String

    data object Ric : Esse3RecordBookRecognitionType { override val value = "ric" }
    data object Attocar : Esse3RecordBookRecognitionType { override val value = "attoCar" }
    data class Unknown(override val value: String) : Esse3RecordBookRecognitionType

    object Serializer : KSerializer<Esse3RecordBookRecognitionType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3RecordBookRecognitionType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3RecordBookRecognitionType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "ric" -> Ric
                "attoCar" -> Attocar
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3RecordBookRecognitionType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3RecordBookStatsDataOriginFilter.Serializer::class)
sealed interface Esse3RecordBookStatsDataOriginFilter {
    val value: String

    data object LibrettoAdSuperate : Esse3RecordBookStatsDataOriginFilter { override val value = "LIBRETTO_AD_SUPERATE" }
    data object LibrettoAdSuperateFrequentate : Esse3RecordBookStatsDataOriginFilter { override val value = "LIBRETTO_AD_SUPERATE_FREQUENTATE" }
    data object LibrettoTutteAd : Esse3RecordBookStatsDataOriginFilter { override val value = "LIBRETTO_TUTTE_AD" }
    data object Piano : Esse3RecordBookStatsDataOriginFilter { override val value = "PIANO" }
    data class Unknown(override val value: String) : Esse3RecordBookStatsDataOriginFilter

    object Serializer : KSerializer<Esse3RecordBookStatsDataOriginFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3RecordBookStatsDataOriginFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3RecordBookStatsDataOriginFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "LIBRETTO_AD_SUPERATE" -> LibrettoAdSuperate
                "LIBRETTO_AD_SUPERATE_FREQUENTATE" -> LibrettoAdSuperateFrequentate
                "LIBRETTO_TUTTE_AD" -> LibrettoTutteAd
                "PIANO" -> Piano
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3RecordBookStatsDataOriginFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3RecordBookStatsRuleFilter.Serializer::class)
sealed interface Esse3RecordBookStatsRuleFilter {
    val value: String

    data object Offf : Esse3RecordBookStatsRuleFilter { override val value = "OFFF" }
    data object Rad : Esse3RecordBookStatsRuleFilter { override val value = "RAD" }
    data class Unknown(override val value: String) : Esse3RecordBookStatsRuleFilter

    object Serializer : KSerializer<Esse3RecordBookStatsRuleFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3RecordBookStatsRuleFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3RecordBookStatsRuleFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "OFFF" -> Offf
                "RAD" -> Rad
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3RecordBookStatsRuleFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3RegulationStatusCode.Serializer::class)
sealed interface Esse3RegulationStatusCode {
    val value: String

    data object Planned : Esse3RegulationStatusCode { override val value = "P" }
    data object Frequented : Esse3RegulationStatusCode { override val value = "C" }
    data object Validated : Esse3RegulationStatusCode { override val value = "V" }
    data object Passed : Esse3RegulationStatusCode { override val value = "L" }
    data object Cancelled : Esse3RegulationStatusCode { override val value = "X" }
    data class Unknown(override val value: String) : Esse3RegulationStatusCode

    object Serializer : KSerializer<Esse3RegulationStatusCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3RegulationStatusCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3RegulationStatusCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "P" -> Planned
                "C" -> Frequented
                "V" -> Validated
                "L" -> Passed
                "X" -> Cancelled
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3RegulationStatusCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3RuleConstraintType.Serializer::class)
sealed interface Esse3RuleConstraintType {
    val value: String

    data object NumberOfActivities : Esse3RuleConstraintType { override val value = "NUM_AD" }
    data object AllActivities : Esse3RuleConstraintType { override val value = "TUTTE_AD" }
    data object WeightCredits : Esse3RuleConstraintType { override val value = "PESO" }
    data class Unknown(override val value: String) : Esse3RuleConstraintType

    object Serializer : KSerializer<Esse3RuleConstraintType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3RuleConstraintType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3RuleConstraintType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "NUM_AD" -> NumberOfActivities
                "TUTTE_AD" -> AllActivities
                "PESO" -> WeightCredits
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3RuleConstraintType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3SessionEvaluationType.Serializer::class)
sealed interface Esse3SessionEvaluationType {
    val value: String

    data object FinalExam : Esse3SessionEvaluationType { override val value = "PF" }
    data object PartialExam : Esse3SessionEvaluationType { override val value = "PP" }
    data class Unknown(override val value: String) : Esse3SessionEvaluationType

    object Serializer : KSerializer<Esse3SessionEvaluationType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3SessionEvaluationType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3SessionEvaluationType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "PF" -> FinalExam
                "PP" -> PartialExam
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3SessionEvaluationType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3State.Serializer::class)
sealed interface Esse3State {
    val value: String

    data object Planned : Esse3State { override val value = "P" }
    data object Frequented : Esse3State { override val value = "F" }
    data object Passed : Esse3State { override val value = "S" }
    data class Unknown(override val value: String) : Esse3State

    object Serializer : KSerializer<Esse3State> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3State", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3State {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "P" -> Planned
                "F" -> Frequented
                "S" -> Passed
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3State) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3State2.Serializer::class)
sealed interface Esse3State2 {
    val value: String

    data object Active : Esse3State2 { override val value = "A" }
    data object Draft : Esse3State2 { override val value = "B" }
    data object Deleted : Esse3State2 { override val value = "X" }
    data class Unknown(override val value: String) : Esse3State2

    object Serializer : KSerializer<Esse3State2> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3State2", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3State2 {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "A" -> Active
                "B" -> Draft
                "X" -> Deleted
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3State2) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3State3.Serializer::class)
sealed interface Esse3State3 {
    val value: String

    data object Draft : Esse3State3 { override val value = "B" }
    data object Proposed : Esse3State3 { override val value = "P" }
    data object Validated : Esse3State3 { override val value = "V" }
    data object Approved : Esse3State3 { override val value = "A" }
    data object Rejected : Esse3State3 { override val value = "R" }
    data object Cancelled : Esse3State3 { override val value = "X" }
    data class Unknown(override val value: String) : Esse3State3

    object Serializer : KSerializer<Esse3State3> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3State3", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3State3 {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "B" -> Draft
                "P" -> Proposed
                "V" -> Validated
                "A" -> Approved
                "R" -> Rejected
                "X" -> Cancelled
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3State3) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3StateCode.Serializer::class)
sealed interface Esse3StateCode {
    val value: String

    data object Active : Esse3StateCode { override val value = "A" }
    data object Draft : Esse3StateCode { override val value = "B" }
    data object Closed : Esse3StateCode { override val value = "C" }
    data object Deprecated : Esse3StateCode { override val value = "D" }
    data object Expired : Esse3StateCode { override val value = "E" }
    data class Unknown(override val value: String) : Esse3StateCode

    object Serializer : KSerializer<Esse3StateCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3StateCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3StateCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "A" -> Active
                "B" -> Draft
                "C" -> Closed
                "D" -> Deprecated
                "E" -> Expired
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3StateCode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3StudentGender.Serializer::class)
sealed interface Esse3StudentGender {
    val value: String

    data object Male : Esse3StudentGender { override val value = "M" }
    data object Female : Esse3StudentGender { override val value = "F" }
    data class Unknown(override val value: String) : Esse3StudentGender

    object Serializer : KSerializer<Esse3StudentGender> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3StudentGender", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3StudentGender {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "M" -> Male
                "F" -> Female
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3StudentGender) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3StudyPlanStateFilter.Serializer::class)
sealed interface Esse3StudyPlanStateFilter {
    val value: String

    data object B : Esse3StudyPlanStateFilter { override val value = "B" }
    data object P : Esse3StudyPlanStateFilter { override val value = "P" }
    data object V : Esse3StudyPlanStateFilter { override val value = "V" }
    data object A : Esse3StudyPlanStateFilter { override val value = "A" }
    data object R : Esse3StudyPlanStateFilter { override val value = "R" }
    data object X : Esse3StudyPlanStateFilter { override val value = "X" }
    data class Unknown(override val value: String) : Esse3StudyPlanStateFilter

    object Serializer : KSerializer<Esse3StudyPlanStateFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3StudyPlanStateFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3StudyPlanStateFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "B" -> B
                "P" -> P
                "V" -> V
                "A" -> A
                "R" -> R
                "X" -> X
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3StudyPlanStateFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3SystemLog.Serializer::class)
sealed interface Esse3SystemLog {
    val value: String

    data object Up1 : Esse3SystemLog { override val value = "UP_1" }
    data object Up2 : Esse3SystemLog { override val value = "UP_2" }
    data object None : Esse3SystemLog { override val value = "NONE" }
    data class Unknown(override val value: String) : Esse3SystemLog

    object Serializer : KSerializer<Esse3SystemLog> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3SystemLog", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3SystemLog {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "UP_1" -> Up1
                "UP_2" -> Up2
                "NONE" -> None
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3SystemLog) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3TafMode.Serializer::class)
sealed interface Esse3TafMode {
    val value: String

    data object CoreActivities : Esse3TafMode { override val value = "A" }
    data object CharacterizingActivities : Esse3TafMode { override val value = "B" }
    data object RelatedIntegrativeActivities : Esse3TafMode { override val value = "C" }
    data object ElectiveActivities : Esse3TafMode { override val value = "D" }
    data object FinalExamination : Esse3TafMode { override val value = "E" }
    data object OtherSkills : Esse3TafMode { override val value = "F" }
    data object PlacementTraining : Esse3TafMode { override val value = "G" }
    data object RequisiteActivities : Esse3TafMode { override val value = "S" }
    data class Unknown(override val value: String) : Esse3TafMode

    object Serializer : KSerializer<Esse3TafMode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3TafMode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3TafMode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "A" -> CoreActivities
                "B" -> CharacterizingActivities
                "C" -> RelatedIntegrativeActivities
                "D" -> ElectiveActivities
                "E" -> FinalExamination
                "F" -> OtherSkills
                "G" -> PlacementTraining
                "S" -> RequisiteActivities
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3TafMode) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3TeacherDiaryStateFilter.Serializer::class)
sealed interface Esse3TeacherDiaryStateFilter {
    val value: String

    data object B : Esse3TeacherDiaryStateFilter { override val value = "B" }
    data object S : Esse3TeacherDiaryStateFilter { override val value = "S" }
    data object A : Esse3TeacherDiaryStateFilter { override val value = "A" }
    data class Unknown(override val value: String) : Esse3TeacherDiaryStateFilter

    object Serializer : KSerializer<Esse3TeacherDiaryStateFilter> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3TeacherDiaryStateFilter", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3TeacherDiaryStateFilter {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "B" -> B
                "S" -> S
                "A" -> A
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3TeacherDiaryStateFilter) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3TeachingUnitType.Serializer::class)
sealed interface Esse3TeachingUnitType {
    val value: String

    data object Block : Esse3TeachingUnitType { override val value = "BLK" }
    data object EctsCredits : Esse3TeachingUnitType { override val value = "CFU" }
    data object Year : Esse3TeachingUnitType { override val value = "ANN" }
    data class Unknown(override val value: String) : Esse3TeachingUnitType

    object Serializer : KSerializer<Esse3TeachingUnitType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3TeachingUnitType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3TeachingUnitType {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "BLK" -> Block
                "CFU" -> EctsCredits
                "ANN" -> Year
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3TeachingUnitType) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable(with = Esse3TypologyCode.Serializer::class)
sealed interface Esse3TypologyCode {
    val value: String

    data object AdmissionTitle : Esse3TypologyCode { override val value = "AMM" }
    data object EnrollmentTitle : Esse3TypologyCode { override val value = "IMM" }
    data object AbbreviationTitle : Esse3TypologyCode { override val value = "ABBR" }
    data object EquivalentTitle : Esse3TypologyCode { override val value = "EQUI" }
    data class Unknown(override val value: String) : Esse3TypologyCode

    object Serializer : KSerializer<Esse3TypologyCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3TypologyCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Esse3TypologyCode {
            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
                is JsonPrimitive -> element.content
                is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
                else -> ""
            }
            return when (raw) {
                "AMM" -> AdmissionTitle
                "IMM" -> EnrollmentTitle
                "ABBR" -> AbbreviationTitle
                "EQUI" -> EquivalentTitle
                else -> Unknown(raw)
            }
        }

        override fun serialize(encoder: Encoder, value: Esse3TypologyCode) {
            encoder.encodeString(value.value)
        }
    }
}
