package it.attendance100.mybicocca.codegen.esse3.spec

object TypeMapping {

    fun resolvePropertyType(prop: ParsedProperty): ResolvedType {
        if (prop.isArray) {
            val inner = when {
                prop.itemRef != null -> ResolvedType.Reference(prop.itemRef)
                else -> resolveScalar(prop.itemType, prop.itemFormat)
            }
            return ResolvedType.ListOf(inner)
        }
        if (prop.ref != null) {
            return ResolvedType.Reference(prop.ref)
        }
        return resolveScalar(prop.type, prop.format)
    }

    fun resolveScalar(type: String?, format: String?): ResolvedType {
        return ResolvedType.Simple(
            when (type) {
                "string" -> when (format) {
                    "date" -> "LocalDate?"
                    "date-time" -> "LocalDateTime?"
                    else -> "String"
                }
                "integer" -> when (format) {
                    "int64" -> "Long"
                    else -> "Int"
                }
                "number" -> when (format) {
                    "float" -> "Float"
                    else -> "Double"
                }
                "boolean" -> "Boolean"
                "object" -> "kotlinx.serialization.json.JsonObject"
                "file" -> "ByteArray"
                else -> "String"
            }
        )
    }

    fun needsSerializer(type: String?, format: String?): String? {
        if (type == "string") {
            return when (format) {
                "date" -> "Esse3NullableLocalDateSerializer"
                "date-time" -> "Esse3NullableLocalDateTimeSerializer"
                else -> null
            }
        }
        return null
    }

    fun mapPermissionKind(kind: String): String {
        return when (kind) {
            "ALL" -> "ANY"
            "AUTHENTICATED" -> "AUTHENTICATED_USER"
            "STUDENTE" -> "STUDENT"
            "DOCENTE" -> "TEACHER"
            "USER_TECNICO" -> "TECHNICAL_USER"
            "SEGRETERIA" -> "ADMIN_OFFICE"
            "SOGG_ESTERNO" -> "EXTERNAL_SUBJECT"
            "IMMATRICOLATI_IN_IPOTESI" -> "PROVISIONAL_ENROLLED_STUDENT"
            "REGISTRATO" -> "REGISTERED_USER"
            else -> "UNKNOWN"
        }
    }

    fun isDateType(kotlinType: String): Boolean {
        return kotlinType == "LocalDate?" || kotlinType == "LocalDateTime?"
    }
}
