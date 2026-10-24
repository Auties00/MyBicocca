package it.attendance100.mybicocca.data.remote.elearning.api

import it.attendance100.mybicocca.data.remote.elearning.exception.KalturaException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

/** Asks Kaltura's public CDN for playlists. Needs no login. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("live")
class KalturaApiTest {
    private val api = KalturaApi()

    @AfterAll
    fun tearDown() = api.close()

    @Test
    fun `an entry Kaltura doesn't serve is a 404`() {
        val error = assertThrows<KalturaException> { runBlocking { api.playback.getHlsPlaylist(2351962, "0_missing0") } }
        assertEquals(404, error.statusCode)
    }
}
