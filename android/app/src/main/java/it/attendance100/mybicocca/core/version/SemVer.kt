package it.attendance100.mybicocca.core.version

/**
 * A lenient semantic-version value used to decide whether a remote release is newer than the
 * installed build. Parsing is forgiving on purpose: release tags in the wild carry a `v` prefix
 * (`v0.1`), omit the patch (`0.1`), or trail build metadata (`1.2.0+ci.7`), and the installed
 * [android.content.pm.PackageInfo.versionName] is whatever the build file declared. Anything
 * unparseable yields null so the caller can fall back to "treat as not-newer" rather than
 * crash on a malformed tag.
 *
 * Precedence follows SemVer 2.0.0 for the numeric core (major, minor, patch compared in order)
 * with one deliberate simplification: a pre-release suffix (`-beta.1`) lowers a version below
 * the same core without one, but two different pre-releases on the same core compare equal —
 * the app never needs to rank `1.0.0-beta.1` against `1.0.0-beta.2`, only to know the suffixed
 * build is older than the final `1.0.0`. Build metadata after `+` is ignored, per spec.
 */
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val isPreRelease: Boolean,
) : Comparable<SemVer> {

    override fun compareTo(other: SemVer): Int {
        major.compareTo(other.major).let { if (it != 0) return it }
        minor.compareTo(other.minor).let { if (it != 0) return it }
        patch.compareTo(other.patch).let { if (it != 0) return it }

        // Same numeric core: a pre-release ranks below the final release.
        return when {
            isPreRelease == other.isPreRelease -> 0
            isPreRelease -> -1
            else -> 1
        }
    }

    companion object {
        /**
         * Parses a version string into a [SemVer], or null when no numeric core can be read.
         * Strips an optional leading `v`/`V`, drops `+build` metadata, splits a `-prerelease`
         * suffix, and reads up to three dot-separated integers (missing parts default to 0).
         */
        fun parse(raw: String?): SemVer? {
            if (raw.isNullOrBlank()) return null

            var text = raw.trim()
            if (text.startsWith("v") || text.startsWith("V")) text = text.substring(1)
            text = text.substringBefore('+')
            val core = text.substringBefore('-')
            val isPreRelease = '-' in text
            val parts = core.split('.')
            val major = parts.getOrNull(0)?.toIntOrNull() ?: return null
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
            return SemVer(major, minor, patch, isPreRelease)
        }

        /**
         * True when [latest] represents a strictly newer build than [installed]. Returns false
         * whenever either side fails to parse, so a malformed tag never nags the user with a
         * phantom update.
         */
        fun isNewer(latest: String?, installed: String?): Boolean {
            val latestVer = parse(latest) ?: return false
            val installedVer = parse(installed) ?: return false
            return latestVer > installedVer
        }
    }
}
