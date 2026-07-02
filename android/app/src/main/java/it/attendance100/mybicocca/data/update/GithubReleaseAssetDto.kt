package it.attendance100.mybicocca.data.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubReleaseAssetDto(
    @SerialName("name") val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    @SerialName("size") val size: Long,
    @SerialName("content_type") val contentType: String,
    @SerialName("digest") val digest: String? = null, // GitHub-computed content hash, formatted "sha256:<hex>"
)
