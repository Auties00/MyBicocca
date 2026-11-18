package it.attendance100.mybicocca.data.mapper.elearning

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressEntity
import it.attendance100.mybicocca.data.elearning.ElearningKalturaVideoStreamResponse
import it.attendance100.mybicocca.data.elearning.ElearningKalturaVideoVariant
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import org.junit.Test
import java.time.Instant

/**
 * Covers the Kaltura stream response -> domain mapping (stream URLs, variants, and the
 * derived sessionless thumbnail URL), the master-playlist variant mapping including the
 * fallback key for unnamed flavors, and the local video-progress entity round-trip.
 */
class VideoStreamMapperTest {

    private fun variant(flavorId: String? = "1_abc") = ElearningKalturaVideoVariant(
        flavorId = flavorId,
        pixelWidth = 1280,
        pixelHeight = 720,
        bitrateKbps = 2500,
        frameRateFps = 25f,
    )

    @Test
    fun `stream response toDomain copies stream fields and derives the thumbnail`() {
        val response = ElearningKalturaVideoStreamResponse.Success(
            kalturaEntryId = "1_tb2txam7",
            partnerId = 2351962,
            hlsStreamUrl = "https://stream/master.m3u8",
            dashStreamUrl = "https://stream/manifest.mpd",
            availableVideoVariants = listOf(variant()),
        )
        val stream = response.toDomain(cmId = 999)
        assertThat(stream.cmId).isEqualTo(999)
        assertThat(stream.kalturaEntryId).isEqualTo("1_tb2txam7")
        assertThat(stream.partnerId).isEqualTo(2351962)
        assertThat(stream.hlsUrl).isEqualTo("https://stream/master.m3u8")
        assertThat(stream.dashUrl).isEqualTo("https://stream/manifest.mpd")
        assertThat(stream.variants).hasSize(1)
        assertThat(stream.thumbnailUrl)
            .isEqualTo("https://cdnapisec.kaltura.com/p/2351962/thumbnail/entry_id/1_tb2txam7")
    }

    @Test
    fun `variant toDomain copies the rendition metadata`() {
        val domain = variant().toDomain()
        assertThat(domain.flavorId).isEqualTo("1_abc")
        assertThat(domain.widthPx).isEqualTo(1280)
        assertThat(domain.heightPx).isEqualTo(720)
        assertThat(domain.bitrateKbps).isEqualTo(2500)
        assertThat(domain.frameRateFps).isEqualTo(25f)
    }

    @Test
    fun `variant toDomain derives a key when the flavor is unnamed`() {
        assertThat(variant(flavorId = null).toDomain().flavorId).isEqualTo("1280x720@2500")
    }

    @Test
    fun `video progress entity toDomain maps every field`() {
        val entity = VideoProgressEntity(
            accountId = "acc-1",
            cmId = 12,
            courseId = 5,
            positionMs = 30_000L,
            durationMs = 60_000L,
            completed = false,
            lastUpdatedAtMs = 1_700_000_000_000L,
        )
        val domain = entity.toDomain()
        assertThat(domain.cmId).isEqualTo(12)
        assertThat(domain.courseId).isEqualTo(5)
        assertThat(domain.positionMs).isEqualTo(30_000L)
        assertThat(domain.durationMs).isEqualTo(60_000L)
        assertThat(domain.completed).isFalse()
        assertThat(domain.lastUpdatedAt).isEqualTo(Instant.ofEpochMilli(1_700_000_000_000L))
    }

    @Test
    fun `video progress round-trips through entity and back`() {
        val original = VideoProgress(
            cmId = 12,
            courseId = 5,
            positionMs = 30_000L,
            durationMs = 60_000L,
            completed = true,
            lastUpdatedAt = Instant.ofEpochMilli(1_700_000_000_000L),
        )
        val restored = original.toEntity(AccountId("acc-1")).toDomain()
        assertThat(restored).isEqualTo(original)
    }

    @Test
    fun `video progress toEntity carries the account id and update millis`() {
        val original = VideoProgress(
            cmId = 12,
            courseId = 5,
            positionMs = 1L,
            durationMs = 2L,
            completed = false,
            lastUpdatedAt = Instant.ofEpochMilli(42L),
        )
        val entity = original.toEntity(AccountId("acc-9"))
        assertThat(entity.accountId).isEqualTo("acc-9")
        assertThat(entity.lastUpdatedAtMs).isEqualTo(42L)
    }
}
