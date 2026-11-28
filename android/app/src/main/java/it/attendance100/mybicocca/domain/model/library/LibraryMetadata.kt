package it.attendance100.mybicocca.domain.model.library

import java.text.Normalizer
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** University campus hosting a Bicocca library. */
enum class LibraryCampus { Milano, Monza }

/** Floor of a catalogued Bicocca library. */
enum class LibraryFloor { Ground, Second }

/**
 * Static metadata for a known Bicocca library, keyed by Affluences site id.
 *
 * @property id Affluences site UUID.
 * @property shortName Compact library name for tight surfaces ("Centrale", "Medicina").
 * @property campus University campus.
 * @property floor Floor enum for localized UI formatting.
 * @property floorIt Italian floor label ("II piano", "piano terra").
 * @property floorEn English floor label ("2nd floor", "ground floor").
 * @property mapBuildingCode Campus map building code ("U06", "U08").
 * @property photoUrl Curated photo URL applied in the live data layer.
 * @property latitude Site latitude for proximity matching.
 * @property longitude Site longitude for proximity matching.
 */
data class LibraryMetadata(
    val id: String,
    val shortName: String,
    val campus: LibraryCampus,
    val floor: LibraryFloor,
    val floorIt: String,
    val floorEn: String,
    val mapBuildingCode: String,
    val photoUrl: String,
    val latitude: Double,
    val longitude: Double,
) {
    fun floorLabel(isItalian: Boolean): String = if (isItalian) floorIt else floorEn
}

/**
 * Static catalogue of the Bicocca libraries plus pure formatting and matching helpers shared
 * across preferred-campus resolution, live photo overrides, building lines, contact formatting,
 * and reservation identification.
 */
object LibraryCatalog {

    const val CENTRALE_ID = "4912615d-501b-4c4e-a2a7-ccaec81c5429"
    const val MEDICINA_ID = "455e0d1d-2236-4e49-8e76-f1b931b08974"

    const val MAX_LOCATION_DISTANCE_KM = 5.0

    val Centrale = LibraryMetadata(
        id = CENTRALE_ID,
        shortName = "Centrale",
        campus = LibraryCampus.Milano,
        floor = LibraryFloor.Second,
        floorIt = "II piano",
        floorEn = "2nd floor",
        mapBuildingCode = "U06",
        photoUrl = "https://www.biblio.unimib.it/sites/bibl/files/styles/colorbox_1600x900/public/_P_00428.jpg?itok=6KNLGoe_",
        latitude = 45.5182,
        longitude = 9.21392,
    )

    val Medicina = LibraryMetadata(
        id = MEDICINA_ID,
        shortName = "Medicina",
        campus = LibraryCampus.Monza,
        floor = LibraryFloor.Ground,
        floorIt = "piano terra",
        floorEn = "ground floor",
        mapBuildingCode = "U08",
        photoUrl = "https://static.affluences.media/sites-pictures/sites/ktdmpsvtG.jpeg",
        latitude = 45.6039,
        longitude = 9.26105,
    )

    val entries: List<LibraryMetadata> = listOf(Centrale, Medicina)

    private val byIdMap: Map<String, LibraryMetadata> = entries.associateBy { it.id }

    private val UnimibSuffixRegex = Regex("\\s*\\(Unimib\\)", RegexOption.IGNORE_CASE)
    private val TrailingCountryRegex = Regex(",\\s*(Italie|Italia)\\s*$", RegexOption.IGNORE_CASE)
    private val DiacriticsRegex = Regex("\\p{Mn}+")
    private val NonWordRegex = Regex("[^\\p{L}\\p{Nd}]+")
    private val U6TokenRegex = Regex("(^|\\s)u0?6(\\s|$)")
    private val U8TokenRegex = Regex("(^|\\s)u0?8(\\s|$)")

    private const val EARTH_RADIUS_KM = 6371.0088

    private val MonzaProgrammes: List<String> = listOf(
        "medicina e chirurgia",
        "medicine and surgery",
        "odontoiatria e protesi dentaria",
        "fisioterapia",
        "igiene dentale",
        "infermieristica",
        "ostetricia",
        "tecniche di laboratorio biomedico",
        "tecniche di radiologia medica",
        "terapia della neuro e psicomotricita",
        "biotecnologie mediche",
        "scienze infermieristiche e ostetriche",
    )

    fun byId(id: String): LibraryMetadata? = byIdMap[id]

    /** Strips any "(Unimib)" tag from an Affluences secondary name. */
    fun cleanSecondaryName(secondaryName: String?): String? =
        secondaryName
            ?.replace(UnimibSuffixRegex, "")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    /**
     * Builds the building line shown across cards and headers: secondary name without "(Unimib)",
     * followed by " · " and the floor label when present (e.g. "Ed. U6 Agorà · II piano").
     */
    fun buildingLine(secondaryName: String?, floorLabel: String?): String? =
        listOfNotNull(
            cleanSecondaryName(secondaryName),
            floorLabel?.trim()?.takeIf { it.isNotEmpty() },
        ).joinToString(" · ").takeIf { it.isNotEmpty() }

    fun buildingLine(library: Library, isItalian: Boolean): String? =
        buildingLine(library.secondaryName, byId(library.id)?.floorLabel(isItalian))

    /** Removes a trailing ", Italie" or ", Italia" added by the Affluences backend. */
    fun cleanAddress(address: String?): String? =
        address
            ?.trim()
            ?.replace(TrailingCountryRegex, "")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    /** Formats Italian university numbers like "+39 02 6448 6251"; preserves unrecognized formats. */
    fun formatPhone(raw: String?): String? {
        val trimmed = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val compact = trimmed.filter { it.isDigit() || it == '+' }
        val local = when {
            compact.startsWith("+39") -> compact.removePrefix("+39")
            compact.startsWith("0039") -> compact.removePrefix("0039")
            compact.startsWith("0") -> compact
            else -> return trimmed
        }
        if (!local.all { it.isDigit() }) return trimmed
        return when {
            local.startsWith("02") && local.length == 10 ->
                "+39 02 ${local.substring(2, 6)} ${local.substring(6)}"
            local.startsWith("02") && local.length == 9 ->
                "+39 02 ${local.substring(2, 5)} ${local.substring(5)}"
            local.startsWith("039") && local.length == 10 ->
                "+39 039 ${local.substring(3, 6)} ${local.substring(6)}"
            local.startsWith("039") && local.length == 9 ->
                "+39 039 ${local.substring(3, 6)} ${local.substring(6)}"
            else -> trimmed
        }
    }

    /** Orders libraries with [preferredCampus] first, falling back to Centrale first. */
    fun sortForCampus(libraries: List<Library>, preferredCampus: LibraryCampus?): List<Library> {
        val targetCampus = preferredCampus ?: LibraryCampus.Milano
        return libraries.sortedWith(
            compareBy<Library>(
                { byId(it.id)?.campus != targetCampus },
                { it.id != CENTRALE_ID },
            ),
        )
    }

    /** Returns the nearest catalogued library if within [maxDistanceKm] of the coordinates. */
    fun nearestWithinKm(
        latitude: Double,
        longitude: Double,
        maxDistanceKm: Double = MAX_LOCATION_DISTANCE_KM,
    ): LibraryMetadata? = entries
        .map { it to distanceKm(latitude, longitude, it.latitude, it.longitude) }
        .filter { (_, distance) -> distance <= maxDistanceKm }
        .minByOrNull { (_, distance) -> distance }
        ?.first

    /** Great-circle haversine distance in kilometres. */
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val radLat1 = Math.toRadians(lat1)
        val radLat2 = Math.toRadians(lat2)
        val h = sin(dLat / 2).pow(2) + cos(radLat1) * cos(radLat2) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }

    /**
     * Resolves a career programme description to [LibraryCampus.Monza] if it contains any Monza
     * programme as whole words after lower-casing, accent stripping and punctuation folding,
     * otherwise [LibraryCampus.Milano]. Returns null for blank input.
     */
    fun campusForProgramme(programmeName: String?): LibraryCampus? {
        val normalized = normalizeWords(programmeName) ?: return null
        val padded = " $normalized "
        return if (MonzaProgrammes.any { phrase -> " $phrase " in padded }) {
            LibraryCampus.Monza
        } else {
            LibraryCampus.Milano
        }
    }

    /**
     * Identifies the catalogued library for a server reservation, which carries names instead of
     * a site UUID. Returns null when neither name matches a catalogued site.
     */
    fun matchReservation(libraryName: String, librarySecondaryName: String?): LibraryMetadata? {
        val normalized = normalizeWords(listOfNotNull(libraryName, librarySecondaryName).joinToString(" "))
            ?: return null
        val padded = " $normalized "
        return when {
            " medicina " in padded || " asclepio " in padded || U8TokenRegex.containsMatchIn(normalized) ->
                Medicina
            " centrale " in padded || " agora " in padded || U6TokenRegex.containsMatchIn(normalized) ->
                Centrale
            else -> null
        }
    }

    private fun normalizeWords(input: String?): String? {
        val raw = input?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val decomposed = Normalizer.normalize(raw.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        val stripped = DiacriticsRegex.replace(decomposed, "")
        return NonWordRegex.replace(stripped, " ").trim().takeIf { it.isNotEmpty() }
    }
}
