package it.attendance100.mybicocca.core.notification

/**
 * A notification's slot in the tray, as a type rather than a loose `Int`.
 *
 * Posting reuses a slot: the same id updates the notification already there. Loose integer
 * constants make that a hazard, since two unrelated features settling on `1` silently overwrite
 * each other and the symptom (a notification that vanishes when something unrelated happens) looks
 * nothing like the cause.
 */
sealed class NotificationId(val value: Int) {

    /** One live instance, app-wide. */
    data object UpdateAvailable : NotificationId(1)
    data object UpdateProgress : NotificationId(2)
    data object UpdateReady : NotificationId(3)

    /**
     * One instance per thing, so a re-post updates the notification about *that* course or exam
     * rather than replacing an unrelated one. [kind] separates namespaces, so a course and an exam
     * sharing an id string still land in different slots.
     */
    data class Entity(val kind: String, val key: String) : NotificationId(idFor(kind, key))

    companion object {
        /**
         * Entity ids start above every singleton slot, so adding a singleton can never land on a
         * slot an entity is already using.
         */
        const val ENTITY_ID_FLOOR = 1_000

        /**
         * 31 bits of entropy spread over a range only 1000 short of the full positive `Int`, so
         * the modulo barely narrows it: a few hundred live per-entity notifications collide with
         * probability on the order of 1e-5. A collision is silent — one notification replaces
         * another — so this is worth re-checking if entity notifications ever number in the
         * thousands rather than the dozens.
         */
        internal fun idFor(kind: String, key: String): Int {
            val hash = "$kind/$key".hashCode() and Int.MAX_VALUE
            return ENTITY_ID_FLOOR + hash % (Int.MAX_VALUE - ENTITY_ID_FLOOR)
        }
    }
}
