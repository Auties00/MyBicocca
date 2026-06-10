package it.attendance100.mybicocca.domain.model.security

/**
 * Outcome of the app-lock password fallback, produced by comparing the entered password against
 * the active account's stored sign-in credential and consumed by the lock screen.
 *
 * [Success] dismisses the lock, [WrongPassword] keeps it up with an error, and [Error] means
 * there is nothing to verify against (no active account or no stored credential), leaving
 * biometrics as the only way in.
 */
enum class UnlockResult { Success, WrongPassword, Error }
