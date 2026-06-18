package it.attendance100.mybicocca.domain.model.update

/**
 * Where this install came from, which decides how an available update is delivered.
 *
 * Resolved at runtime from the OS install-source rather than baked in at build time, so a single
 * artifact behaves correctly whether it was sideloaded from the GitHub release page or (in the
 * future) installed through Google Play. For now every channel that isn't Play is treated as
 * [GITHUB]; the update flow opens the release page. When the Play listing ships, the same enum
 * routes Play installs to the store page (and later to the in-app update flows) without touching
 * any call site.
 */
enum class DistributionSource {
    /** Sideloaded — the APK came from the GitHub releases page (or any non-Play installer). */
    GITHUB,

    /** Installed from the Google Play Store (`com.android.vending`). */
    PLAY_STORE,
}
