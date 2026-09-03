package it.attendance100.mybicocca.core.notification

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Invariants of the channel registry that are cheap to hold and expensive to notice once broken,
 * since a channel's settings belong to the user the moment it is first created and cannot be
 * corrected from code afterwards.
 */
class NotificationChannelsTest {

    @Test
    fun `channel ids are unique`() {
        val ids = NotificationChannelId.entries.map { it.id }
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun `every channel id carries a version suffix`() {
        // Retrofitting the suffix is impossible: an unversioned id already on a user's device
        // keeps its settings forever, so the convention only works if it is never broken.
        NotificationChannelId.entries.forEach { channel ->
            assertThat(channel.id).matches(".*_v\\d+$")
        }
    }

    @Test
    fun `no live channel id is also listed as retired`() {
        // Registration creates channels and then deletes the retired ones, so an id in both lists
        // would delete the channel it had just created.
        val live = NotificationChannelId.entries.map { it.id }.toSet()
        assertThat(RETIRED_CHANNEL_IDS.intersect(live)).isEmpty()
    }

    @Test
    fun `progress is quieter than the actionable channel`() {
        // Progress updates many times per download; alerting on each is the failure this ordering
        // exists to prevent.
        assertThat(NotificationChannelId.UPDATE_PROGRESS.importance)
            .isLessThan(NotificationChannelId.UPDATE_ACTIONABLE.importance)
    }
}
