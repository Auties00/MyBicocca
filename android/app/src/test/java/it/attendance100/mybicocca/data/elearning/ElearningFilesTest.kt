package it.attendance100.mybicocca.data.elearning

import com.google.common.truth.Truth.assertThat
import io.ktor.utils.io.ByteReadChannel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleFilesApi
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleUploadedFile
import it.attendance100.mybicocca.data.remote.runtime.FileUpload
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

/** File downloads and uploads over the generated web service file endpoints. */
class ElearningFilesTest {
    private val token = "t".repeat(32)
    private val api = mockk<MoodleFilesApi>()
    private val files = ElearningFiles(api)
    private val url = "https://elearning.unimib.it/webservice/pluginfile.php/123/mod_resource/content/1/Slide%201.pdf?forcedownload=1"

    @Test
    fun `adds the token to a file's URL once`() {
        assertThat(files.authenticatedFileUrl(token, url)).isEqualTo("$url&token=$token")
        assertThat(files.authenticatedFileUrl(token, "$url&token=x")).isEqualTo("$url&token=x")
        assertThrows(IllegalArgumentException::class.java) { files.authenticatedFileUrl(token, "https://elearning.unimib.it/pluginfile.php/1/a.pdf") }
    }

    @Test
    fun `downloads the path after pluginfile php, without the query`() = runTest {
        coEvery { api.downloadFile("123/mod_resource/content/1/Slide%201.pdf", token) } returns ByteReadChannel("%PDF".toByteArray())
        assertThat(files.downloadFileBytes(token, url)).isEqualTo("%PDF".toByteArray())
    }

    @Test
    fun `uploads every file to the draft area the first creates`() = runTest {
        coEvery { api.uploadFile(token, 0, any(), any()) } returns listOf(MoodleUploadedFile(itemId = 77))
        coEvery { api.uploadFile(token, 77, any(), any()) } returns listOf(MoodleUploadedFile(itemId = 77))
        val stored = files.uploadToDraftArea(token, listOf(FileUpload("a.txt", "text/plain", byteArrayOf(1)), FileUpload("b.txt", null, byteArrayOf(2))))
        assertThat(stored.map { it.itemId }).containsExactly(77, 77)
        coVerify(exactly = 1) { api.uploadFile(token, 0, any(), any()) }
        coVerify(exactly = 1) { api.uploadFile(token, 77, any(), any()) }
    }
}
