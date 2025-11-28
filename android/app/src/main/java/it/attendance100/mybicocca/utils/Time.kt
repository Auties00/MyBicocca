package it.attendance100.mybicocca.utils

import android.os.*
import kotlinx.coroutines.*

/**
 * Wait for a specified amount of time before executing an action.
 *
 * Cancellable.
 *
 * Usage:
 * ```
 * wait(scope, 1000L) {
 *   // Do something
 * }
 * ```
 *
 * @param scope The CoroutineScope in which to launch the coroutine
 * @param delayMillis The delay in milliseconds before executing the action
 * @param action The action to be executed after the delay
 */
fun wait(scope: CoroutineScope, delayMillis: Long, action: () -> Unit) {
  scope.launch {
    delay(delayMillis)
    action()
  }
}

/**
 * Wait for a specified amount of time before executing an action on the main thread.
 *
 * Not cancellable.
 *
 * Usage:
 * ```
 * wait(1000L) {
 *   // Do something
 * }
 * ```
 *
 * @param delayMillis The delay in milliseconds before executing the action
 * @param action The action to be executed after the delay
 */
fun wait(delayMillis: Long, action: () -> Unit) {
  Handler(Looper.getMainLooper()).postDelayed(
    action,
    delayMillis
  )
}