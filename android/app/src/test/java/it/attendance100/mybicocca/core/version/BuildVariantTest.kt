package it.attendance100.mybicocca.core.version

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * A nightly is named after the release it is heading towards, so `0.0.6-nightly` and the stable
 * `0.0.6` share everything but the suffix. These pin down that the suffix is what tells them apart.
 */
class BuildVariantTest {

    @Test
    fun `a nightly is not the stable release it is named after`() {
        assertThat(isSameBuild(null, "0.0.6", RUNNING_SHA, "0.0.6-nightly")).isFalse()
    }

    @Test
    fun `a stable build is the release with its own version`() {
        assertThat(isSameBuild(null, "0.0.5", RUNNING_SHA, "0.0.5")).isTrue()
        assertThat(isSameBuild(null, "0.0.6", RUNNING_SHA, "0.0.5")).isFalse()
    }

    /** Every nightly on one base version shares a version name, so only the commit can decide. */
    @Test
    fun `a nightly release is matched on its commit, whatever its name`() {
        assertThat(isSameBuild(RUNNING_SHA, "Oct 9, 2026", RUNNING_SHA, "0.0.6-nightly")).isTrue()
        assertThat(isSameBuild("0ther5h", "0.0.6-nightly", RUNNING_SHA, "0.0.6-nightly")).isFalse()
    }

    @Test
    fun `the release a nightly previews is newer than that nightly`() {
        assertThat(SemVer.isNewer("0.0.6", "0.0.6-nightly")).isTrue()
    }

    /** The release a nightly was built on top of is what "restore to stable" goes back to. */
    @Test
    fun `the release a nightly was built on is not newer than it`() {
        assertThat(SemVer.isNewer("0.0.5", "0.0.6-nightly")).isFalse()
    }

    /**
     * A nightly's name only ever guesses a patch bump. When the release turns out to be a minor
     * or a major one, the guess is wrong but the ordering must still hold.
     */
    @Test
    fun `a minor or major release is newer than a nightly that guessed a patch`() {
        assertThat(SemVer.isNewer("0.1.0", "0.0.6-nightly")).isTrue()
        assertThat(SemVer.isNewer("1.0.0", "0.9.4-nightly")).isTrue()
        assertThat(isSameBuild(null, "0.1.0", RUNNING_SHA, "0.0.6-nightly")).isFalse()
    }

    @Test
    fun `an older release is never newer than a nightly from a later minor or major`() {
        assertThat(SemVer.isNewer("0.0.9", "0.1.1-nightly")).isFalse()
        assertThat(SemVer.isNewer("0.9.9", "1.0.1-nightly")).isFalse()
    }

    private companion object {
        const val RUNNING_SHA = "abc1234"
    }
}
