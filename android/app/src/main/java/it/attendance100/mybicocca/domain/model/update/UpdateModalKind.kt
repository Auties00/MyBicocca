package it.attendance100.mybicocca.domain.model.update

/**
 * Which of the update sheet's three jobs it is doing.
 *
 * The two channel changes are not variations in wording. They are reversible actions the user has
 * already half-taken — the switch is flipped before the sheet opens — so backing out has to undo
 * it, and the copy has to describe a move sideways or backwards rather than an upgrade.
 */
enum class UpdateModalKind {
    /** A newer version, offered normally. */
    Standard,

    /** Stable to nightly: the switch is on, and leaving turns it back off. */
    SwitchToNightly,

    /** Nightly back to stable: the switch is off, and leaving turns it back on. */
    RestoreStable,
}

/**
 * An update sheet that was open when the process died, so it can be put back.
 *
 * The release travels with it rather than being looked up again on restore: restore-to-stable's
 * release is fetched straight from GitHub and lives in no channel slot, and the download it
 * started is cleared from [it.attendance100.mybicocca.data.local.settings.UpdateStateStore]'s
 * pending slot the moment it finishes — which is exactly when the user is most likely to still
 * have the sheet open.
 */
data class PendingUpdateModal(val release: AppRelease, val kind: UpdateModalKind)
