package it.attendance100.mybicocca.data.remote.esse3.dto

enum class Esse3PermissionLevel(val profileName: String?, val groupId: Int?) {
    ANY(null, null),
    AUTHENTICATED_USER("AUTHENTICATED", null),
    TEACHER("DOCENTE", 7),
    PROVISIONAL_ENROLLED_STUDENT("IMMATRICOLATI_IN_IPOTESI", 4),
    REGISTERED_USER("REGISTRATO", 9),
    ADMIN_OFFICE("SEGRETERIA", null),
    ADMIN_OFFICE_ADMIN("SEGRETERIA_ADMIN", null),
    EXTERNAL_SUBJECT("SOGG_EST", null),
    STUDENT("STUDENTE", 6),
    TECHNICAL_USER("USER_TECNICO", null),
    UNKNOWN(null, null);

    companion object {
        fun fromProfileName(name: String): Esse3PermissionLevel {
            return entries.firstOrNull { it.profileName.equals(name, ignoreCase = true) } ?: UNKNOWN
        }

        fun fromGroupId(id: Int): Esse3PermissionLevel {
            return entries.firstOrNull { it.groupId == id } ?: UNKNOWN
        }
    }
}