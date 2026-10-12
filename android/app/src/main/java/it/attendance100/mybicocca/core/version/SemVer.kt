package it.attendance100.mybicocca.core.version

/**
 * A lenient semantic-version value used to decide whether a remote release is newer than the
 * installed build. Parsing is forgiving on purpose: release tags in the wild carry a `v` prefix
 * (`v0.1`), omit the patch (`0.1`), or trail build metadata (`1.2.0+ci.7`), and the installed
 * [android.content.pm.PackageInfo.versionName] is whatever the build file declared. Anything
 * unparseable yields null so the caller can fall back to "treat as not-newer" rather than
 * crash on a malformed tag.
 *
 * Precedence follows SemVer 2.0.0: the numeric core (major, minor, patch) compared in order, then
 * a pre-release ranks below the same core without one, and two pre-releases on the same core are
 * ranked by their dot-separated identifiers — numeric ones numerically, the rest by ASCII, a numeric
 * one below a non-numeric one, and a shorter list below a longer one it prefixes. That last part is
 * what orders nightlies: `0.0.6-nightly.9` < `0.0.6-nightly.12` < `0.0.6`. Build metadata after `+`
 * is ignored, per spec.
 */
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: List<String> = emptyList(),
) : Comparable<SemVer> {

    val isPreRelease: Boolean get() = preRelease.isNotEmpty()

    override fun compareTo(other: SemVer): Int {
        major.compareTo(other.major).let { if (it != 0) return it }
        minor.compareTo(other.minor).let { if (it != 0) return it }
        patch.compareTo(other.patch).let { if (it != 0) return it }

        // Same numeric core: a pre-release ranks below the final release.
        if (isPreRelease != other.isPreRelease) return if (isPreRelease) -1 else 1

        for ((mine, theirs) in preRelease.zip(other.preRelease)) {
            comparePreReleaseIdentifier(mine, theirs).let { if (it != 0) return it }
        }
        return preRelease.size.compareTo(other.preRelease.size)
    }

    companion object {
        /**
         * Parses a version string into a [SemVer], or null when no numeric core can be read.
         * Strips an optional leading `v`/`V`, drops `+build` metadata, splits a `-prerelease`
         * suffix into its dot-separated identifiers, and reads up to three dot-separated integers
         * (missing parts default to 0).
         */
        fun parse(raw: String?): SemVer? {
            if (raw.isNullOrBlank()) return null

            var text = raw.trim()
            if (text.startsWith("v") || text.startsWith("V")) text = text.substring(1)
            text = text.substringBefore('+')
            val core = text.substringBefore('-')
            val preRelease = if ('-' in text) text.substringAfter('-').split('.') else emptyList()
            val parts = core.split('.')
            val major = parts.getOrNull(0)?.toIntOrNull() ?: return null
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
            return SemVer(major, minor, patch, preRelease)
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

        private fun comparePreReleaseIdentifier(a: String, b: String): Int {
            val aNum = a.toLongOrNull()
            val bNum = b.toLongOrNull()
            return when {
                aNum != null && bNum != null -> aNum.compareTo(bNum)
                aNum != null -> -1
                bNum != null -> 1
                else -> a.compareTo(b)
            }
        }
    }
}
