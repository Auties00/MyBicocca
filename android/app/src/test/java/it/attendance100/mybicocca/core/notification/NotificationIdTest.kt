package it.attendance100.mybicocca.core.notification

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * The property that matters: two things that aren't the same notification never share a slot,
 * because a collision shows up as a notification silently replaced by an unrelated one.
 */
class NotificationIdTest {

    @Test
    fun `singleton slots are distinct`() {
        val ids = listOf(
            NotificationId.UpdateAvailable,
            NotificationId.UpdateProgress,
            NotificationId.UpdateReady,
        ).map { it.value }

        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun `singleton slots stay below the entity floor`() {
        listOf(
            NotificationId.UpdateAvailable,
            NotificationId.UpdateProgress,
            NotificationId.UpdateReady,
        ).forEach { assertThat(it.value).isLessThan(NotificationId.ENTITY_ID_FLOOR) }
    }

    @Test
    fun `the same entity resolves to the same slot every time`() {
        // A re-post has to update the notification about that course, not add a second one.
        assertThat(NotificationId.Entity("course", "E1801Q").value)
            .isEqualTo(NotificationId.Entity("course", "E1801Q").value)
    }

    @Test
    fun `different entities of the same kind get different slots`() {
        assertThat(NotificationId.Entity("course", "E1801Q").value)
            .isNotEqualTo(NotificationId.Entity("course", "E2001Q").value)
    }

    @Test
    fun `the same key in different kinds does not collide`() {
        assertThat(NotificationId.Entity("course", "42").value)
            .isNotEqualTo(NotificationId.Entity("exam", "42").value)
    }

    @Test
    fun `entity slots never reach into the singleton range`() {
        val keys = (0..500).map { NotificationId.Entity("exam", "key-$it") }
        keys.forEach { assertThat(it.value).isAtLeast(NotificationId.ENTITY_ID_FLOOR) }
    }
}
