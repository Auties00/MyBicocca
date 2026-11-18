package it.attendance100.mybicocca.data.elearning

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import it.attendance100.mybicocca.data.remote.elearning.api.KalturaPlaybackApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleWebMediaApi
import it.attendance100.mybicocca.data.remote.elearning.exception.KalturaException
import kotlinx.coroutines.test.runTest
import org.junit.Test

/** Kaltura playback: the playlist's renditions, and entries Kaltura no longer serves. */
class ElearningKalturaTest {
    private val playback = mockk<KalturaPlaybackApi>()
    private val kaltura = ElearningKaltura(mockk<MoodleWebMediaApi>(), playback)

    @Test
    fun `reads the renditions of the HLS playlist by bitrate`() = runTest {
        coEvery { playback.getHlsPlaylist(ElearningKaltura.BICOCCA_PARTNER_ID, "1_abc") } returns """
            #EXTM3U
            #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1500000,RESOLUTION=1280x720,FRAME-RATE=25.000,CODECS="avc1.64001f,mp4a.40.2"
            https://cdn/p/1/flavorId/1_hd/a.m3u8
            #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=500000,RESOLUTION=640x360
            https://cdn/p/1/flavorId/1_sd/a.m3u8
        """.trimIndent()
        val stream = kaltura.getVideoStream("1_abc") as ElearningKalturaVideoStreamResponse.Success
        assertThat(stream.availableVideoVariants).containsExactly(
            ElearningKalturaVideoVariant("1_sd", 640, 360, 500, null),
            ElearningKalturaVideoVariant("1_hd", 1280, 720, 1500, 25f),
        ).inOrder()
        assertThat(stream.hlsStreamUrl).isEqualTo(
            "https://cdnapisec.kaltura.com/p/2351962/sp/235196200/playManifest/entryId/1_abc/protocol/https/format/applehttp/a.m3u8"
        )
    }

    @Test
    fun `an entry Kaltura answers 404 for is unavailable`() = runTest {
        coEvery { playback.getHlsPlaylist(any(), any()) } throws KalturaException(404, null, "Not Found")
        assertThat(kaltura.getVideoStream("1_gone")).isEqualTo(ElearningKalturaVideoStreamResponse.Unavailable)
    }
}
