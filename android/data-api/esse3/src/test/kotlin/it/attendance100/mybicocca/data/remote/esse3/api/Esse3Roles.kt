package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel

/*
 * The roles of the test user, from the group and profiles Esse3 reports at login, which the
 * live tests compare with the roles an operation requires to skip those the user can't call.
 * The specs name the roles but not how logins report them, so the mapping lives here.
 */

private val ROLES_BY_PROFILE = mapOf(
    "AUTHENTICATED" to Esse3PermissionLevel.AUTHENTICATED_USER,
    "DOCENTE" to Esse3PermissionLevel.TEACHER,
    "IMMATRICOLATI_IN_IPOTESI" to Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT,
    "REGISTRATO" to Esse3PermissionLevel.REGISTERED_USER,
    "SEGRETERIA" to Esse3PermissionLevel.ADMIN_OFFICE,
    "SOGG_EST" to Esse3PermissionLevel.EXTERNAL_SUBJECT,
    "STUDENTE" to Esse3PermissionLevel.STUDENT,
    "USER_TECNICO" to Esse3PermissionLevel.TECHNICAL_USER,
)

/** The id of the students' user group. */
const val STUDENT_GROUP_ID = 6

private val ROLES_BY_GROUP = mapOf(
    7 to Esse3PermissionLevel.TEACHER,
    4 to Esse3PermissionLevel.PROVISIONAL_ENROLLED_STUDENT,
    9 to Esse3PermissionLevel.REGISTERED_USER,
    STUDENT_GROUP_ID to Esse3PermissionLevel.STUDENT,
)

/** The role of a login profile, by its name (`STUDENTE`). */
fun esse3RoleOfProfile(name: String): Esse3PermissionLevel =
    ROLES_BY_PROFILE.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value ?: Esse3PermissionLevel.UNKNOWN

/** The role of a user group, by its id. */
fun esse3RoleOfGroup(id: Int): Esse3PermissionLevel = ROLES_BY_GROUP[id] ?: Esse3PermissionLevel.UNKNOWN
