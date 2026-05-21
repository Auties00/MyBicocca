package it.attendance100.mybicocca.data.remote.elearning.dto

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Request body for `session.startWidgetSession` on the Kaltura REST API.
 *
 * A widget session is an anonymous, read-only Kaltura Session (KS) that can be minted
 * without any shared secret. It can read access-control-free entries and is enough to
 * stream Bicocca's kalvidres videos.
 *
 * @property anonymousWidgetId Kaltura's "default widget" identifier for the institution,
 *   conventionally `"_" + partnerId` (e.g. `"_2351962"`).
 */
@Serializable
internal data class ElearningKalturaWidgetSessionRequest(
    @SerialName("widgetId")
    val anonymousWidgetId: String
)

/**
 * Response of `session.startWidgetSession`.
 *
 * Sealed because Kaltura returns either the requested widget session or a
 * [KalturaAPIException](https://developer.kaltura.com/api-docs/General_Concepts/Service_API_Errors)
 * envelope under the same HTTP 200; the [Success] / [Error] branches model that split.
 * Deserialization dispatches on `objectType` (the only field reliably present on the
 * error envelope) via [KalturaWidgetSessionResponseSerializer].
 */
@Serializable(with = KalturaWidgetSessionResponseSerializer::class)
sealed interface ElearningKalturaWidgetSessionResponse {
    /**
     * Anonymous Kaltura Session (KS) returned by `session.startWidgetSession`.
     *
     * The KS is the bearer credential for downstream Kaltura API calls and for the
     * `playManifest` streaming URL. It is short-lived and tied to the institution's
     * partner — do not persist it across sessions.
     *
     * @property kalturaSessionToken Opaque KS string (starts with `djJ8…`); pass as `ks`
     *   to subsequent `baseEntry.*` calls and into `playManifest` URLs.
     * @property partnerId Kaltura partner (institution) the session belongs to.
     * @property userId Always `0` for an anonymous widget session.
     */
    @Serializable
    data class Success(
        @SerialName("ks")
        val kalturaSessionToken: String,
        val partnerId: Int,
        val userId: Int,
    ) : ElearningKalturaWidgetSessionResponse

    /** Kaltura-API exception envelope returned in place of a widget session. */
    @Serializable
    data class Error(
        val code: String,
        val message: String,
        val args: Map<String, String> = emptyMap(),
    ) : ElearningKalturaWidgetSessionResponse
}

internal object KalturaWidgetSessionResponseSerializer :
    JsonContentPolymorphicSerializer<ElearningKalturaWidgetSessionResponse>(
        ElearningKalturaWidgetSessionResponse::class
    ) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ElearningKalturaWidgetSessionResponse> =
        if (element.isKalturaApiException()) ElearningKalturaWidgetSessionResponse.Error.serializer()
        else ElearningKalturaWidgetSessionResponse.Success.serializer()
}

/**
 * Request body for `baseEntry.getPlaybackContext` on the Kaltura REST API.
 *
 * @property kalturaEntryId The Kaltura entry id whose playback context is requested
 *   (e.g. `"1_tb2txam7"`).
 * @property kalturaSessionToken A valid KS (anonymous widget KS is sufficient for
 *   non-protected entries).
 * @property contextDataParams Filter for which flavor (quality) tags to include in
 *   the response. **No default** because kotlinx-serialization's `Json` configuration
 *   has `encodeDefaults = false` and Kaltura rejects the request with
 *   `MISSING_MANDATORY_PARAMETER` when the field is omitted — callers must construct
 *   one explicitly.
 */
@Serializable
internal data class ElearningKalturaPlaybackContextRequest(
    @SerialName("entryId")
    val kalturaEntryId: String,
    @SerialName("ks")
    val kalturaSessionToken: String,
    val contextDataParams: ContextDataParams,
) {
    /**
     * Flavor-tag filter for `getPlaybackContext`.
     *
     * @property objectType Kaltura type marker; always `"KalturaContextDataParams"`.
     * @property flavorTags Comma-separated tag list, or `"all"` to include every flavor.
     */
    @Serializable
    data class ContextDataParams(
        val objectType: String,
        val flavorTags: String,
    )
}

/**
 * Response of `baseEntry.getPlaybackContext`.
 *
 * Sealed because Kaltura returns either the requested playback context or a
 * [KalturaAPIException](https://developer.kaltura.com/api-docs/General_Concepts/Service_API_Errors)
 * envelope under the same HTTP 200; the [Success] / [Error] branches model that split.
 * Deserialization dispatches on `objectType` via [KalturaPlaybackContextResponseSerializer].
 */
@Serializable(with = KalturaPlaybackContextResponseSerializer::class)
sealed interface ElearningKalturaPlaybackContextResponse {

    /**
     * Contains the list of [playableSources] (one per delivery format / protocol
     * combination, e.g. HLS-over-HTTPS, DASH-over-HTTPS, progressive MP4-over-HTTPS, …)
     * and the list of [availableVideoVariants] that those sources expose for adaptive
     * playback.
     *
     * @property playableSources Delivery URLs the player can hand to ExoPlayer / Media3.
     *   Already include the KS embedded in the path.
     * @property availableVideoVariants Per-quality assets (Kaltura "flavors"); useful
     *   for building a quality picker UI, but for plain playback ExoPlayer handles
     *   selection from the HLS / DASH manifest automatically.
     */
    @Serializable
    data class Success(
        @SerialName("sources")
        val playableSources: List<ElearningKalturaPlayableSource> = emptyList(),
        @SerialName("flavorAssets")
        val availableVideoVariants: List<ElearningKalturaVideoVariant> = emptyList(),
    ) : ElearningKalturaPlaybackContextResponse

    /** Kaltura-API exception envelope returned in place of a playback context. */
    @Serializable
    data class Error(
        val code: String,
        val message: String,
        val args: Map<String, String> = emptyMap(),
    ) : ElearningKalturaPlaybackContextResponse
}

internal object KalturaPlaybackContextResponseSerializer :
    JsonContentPolymorphicSerializer<ElearningKalturaPlaybackContextResponse>(
        ElearningKalturaPlaybackContextResponse::class
    ) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ElearningKalturaPlaybackContextResponse> =
        if (element.isKalturaApiException()) ElearningKalturaPlaybackContextResponse.Error.serializer()
        else ElearningKalturaPlaybackContextResponse.Success.serializer()
}

private fun JsonElement.isKalturaApiException(): Boolean =
    (this as? JsonObject)?.get("objectType")?.jsonPrimitive?.contentOrNull == "KalturaAPIException"

/**
 * One ready-to-play delivery URL returned by `getPlaybackContext`.
 *
 * Kaltura returns many variants — different delivery profiles and codec containers
 * (`applehttp`, `mpegdash`, `url`, `hdnetworkmanifest`, `rtsp`, `rtmp`, …) across
 * `http` / `https` protocols. On modern Android, prefer `applehttp` (HLS) over HTTPS
 * for Media3 / ExoPlayer.
 *
 * @property deliveryFormat Kaltura format identifier (`"applehttp"` for HLS,
 *   `"mpegdash"` for DASH, `"url"` for progressive MP4, etc.).
 * @property supportedProtocols Comma-separated transport protocols, e.g. `"http,https"`
 *   or `"https"`.
 * @property availableFlavorIds Comma-separated list of flavor (quality) ids encoded
 *   into [streamUrl].
 * @property streamUrl Absolute URL ready to be fed to a media player; already contains
 *   partner, entry, flavors, KS, and (for HLS / DASH) the manifest path.
 * @property deliveryProfileId Kaltura's internal delivery-profile id; mostly diagnostic.
 */
@Serializable
data class ElearningKalturaPlayableSource(
    @SerialName("format")
    val deliveryFormat: String,
    @SerialName("protocols")
    val supportedProtocols: String,
    @SerialName("flavorIds")
    val availableFlavorIds: String,
    @SerialName("url")
    val streamUrl: String,
    @SerialName("deliveryProfileId")
    val deliveryProfileId: Int? = null
)

/**
 * A single quality variant of a Kaltura entry — what the Kaltura API calls a
 * "flavor asset". Each flavor is one rendition (resolution + bitrate) of the video.
 *
 * Useful for surfacing quality information in the UI; not required for plain
 * playback, since the HLS / DASH manifest pointed at by
 * [ElearningKalturaPlayableSource.streamUrl] already wraps all flavors and lets
 * ExoPlayer / Media3 pick the right one.
 *
 * @property flavorId Kaltura asset id (e.g. `"1_6yjy6ykd"`).
 * @property kalturaEntryId Parent entry id this flavor belongs to.
 * @property pixelWidth Frame width in pixels (`null` for audio-only flavors).
 * @property pixelHeight Frame height in pixels (`null` for audio-only flavors).
 * @property bitrateKbps Average bitrate in kilobits per second.
 * @property frameRateFps Frames per second.
 * @property sizeBytes File size in bytes; null if unknown or omitted by Kaltura.
 * @property fileExtension Container file extension (`"mp4"`, etc.).
 */
@Serializable
data class ElearningKalturaVideoVariant(
    @SerialName("id")
    val flavorId: String,
    @SerialName("entryId")
    val kalturaEntryId: String,
    @SerialName("width")
    val pixelWidth: Int? = null,
    @SerialName("height")
    val pixelHeight: Int? = null,
    @SerialName("bitrate")
    val bitrateKbps: Int? = null,
    @SerialName("frameRate")
    val frameRateFps: Float? = null,
    // Kaltura serializes byte counts as a quoted string to dodge JS number-precision
    // limits; expose it as a Long via a derived property.
    @SerialName("sizeInBytes")
    internal val sizeBytesRaw: String? = null,
    @SerialName("fileExt")
    val fileExtension: String? = null
) {
    /**
     * Parsed size in bytes, or `null` if the raw value is missing or malformed.
     */
    val sizeBytes: Long? get() = sizeBytesRaw?.toLongOrNull()
}

/**
 * High-level result of resolving a kalvidres course module to a streamable video.
 *
 * Sealed because `mod/kalvidres/view.php` requires a live `MoodleSession` browser cookie
 * that the WS token alone does not provide. When the cached cookie is missing or has
 * expired server-side, the page Moodle returns is a login redirect rather than the
 * Kaltura iframe — this is surfaced as [RequiresReauth] so the caller can re-run the
 * SAML login flow (which both refreshes the cookie and updates the cached wsToken) and
 * retry the resolution.
 */
sealed interface ElearningKalturaVideoStreamResponse {

    /**
     * Successfully resolved video stream, ready to hand to a media player.
     *
     * Pass [hlsStreamUrl] directly to a Media3 `HlsMediaSource`. Use [dashStreamUrl] as
     * a fallback if HLS playback fails. Use [availableVideoVariants] only if you want
     * to expose a quality picker in the UI; otherwise ignore it.
     *
     * @property kalturaEntryId Kaltura entry id for the video (`1_…` for Bicocca).
     * @property partnerId Kaltura partner (institution) the entry belongs to.
     * @property kalturaSessionToken The KS used to mint the stream URLs. Forward it to
     *   analytics calls (e.g. `analytics.trackEvent`) so playback events reach Kaltura
     *   under the same session.
     * @property hlsStreamUrl Apple-HTTP-Live-Streaming (`.m3u8`) URL — preferred for
     *   Media3 / ExoPlayer on Android.
     * @property dashStreamUrl MPEG-DASH (`.mpd`) URL, or `null` if the entry does not
     *   expose a DASH source.
     * @property availableVideoVariants Per-quality assets (Kaltura flavor list),
     *   deduplicated and sorted ascending by bitrate.
     */
    data class Success(
        val kalturaEntryId: String,
        val partnerId: Int,
        val kalturaSessionToken: String,
        val hlsStreamUrl: String,
        val dashStreamUrl: String?,
        val availableVideoVariants: List<ElearningKalturaVideoVariant>,
    ) : ElearningKalturaVideoStreamResponse

    /**
     * The current browser session was missing or has been invalidated by Moodle and
     * `mod/kalvidres/view.php` returned a login redirect instead of the Kaltura iframe.
     *
     * The caller should re-run [it.attendance100.mybicocca.data.remote.elearning.api.ElearningAuthApi.login],
     * persist the fresh `moodleSessionCookie`, install it via
     * [it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi.resume], and retry
     * the resolution. If the retry still returns [RequiresReauth] something else is
     * wrong (most commonly the user is not enrolled in the source course).
     */
    object RequiresReauth : ElearningKalturaVideoStreamResponse
}

/**
 * Result of resolving a Kaltura entry id for a kalvidres course module. Mirrors
 * [ElearningKalturaVideoStreamResponse] so callers that cache the cmId→entryId mapping
 * locally get the same reauth signal.
 */
sealed interface ElearningKalturaEntryIdResponse {
    /** The Kaltura entry id (e.g. `"1_tb2txam7"`). */
    data class Success(val kalturaEntryId: String) : ElearningKalturaEntryIdResponse

    /**
     * The browser session was invalidated and the iframe could not be located.
     * Same remediation as [ElearningKalturaVideoStreamResponse.RequiresReauth].
     */
    object RequiresReauth : ElearningKalturaEntryIdResponse
}
