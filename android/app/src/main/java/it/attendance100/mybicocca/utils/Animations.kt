package it.attendance100.mybicocca.utils

import kotlin.math.pow
import kotlin.math.ln

/**
 * A piecewise function that performs an ease-in-out transition to a logarithmic curve.
 *
 * @param x The input value.
 * @param y The target value where the ease-in-out phase ends and the logarithmic phase begins.
 * @param transitionPoint The input 'x' value at which the function switches behaviors.
 * @param logStrength Controls the "strength" of the logarithmic growth.
 * - 1.0f = default logarithmic curve
 * - ..1.0f = more dampened (grows slower)
 * - 1.0f.. = less dampened (grows faster)
 * @return The calculated value.
 */
fun hybridEaseLog(
  x: Float,
  y: Float,
  transitionPoint: Float,
  logStrength: Float = 1.0f
): Float {
  // Ensure positive transitionPoint
  if (transitionPoint <= 0f) return if (x > 0f) y else 0.0f

  if (x < 0.0f) {
    // Undefined behavior (x: ..0)
    return 0.0f

  } else if (x <= transitionPoint) {
    // Ease-In-Out part (x: 0..transitionPoint)

    // Normalize x
    val t = x / transitionPoint

    // Smoothstep ease-in-out formula: 6t^5 - 15t^4 + 10t^3
    val easedT = t.pow(3) * (t * (t * 6.0f - 15.0f) + 10.0f)

    // Dernormalize to y
    return easedT * y

  } else {
    // Logarithmic part (x: transitionPoint..)

    // Log function that starts at the point (transitionPoint, y)
    // The function y + C * log(x - transitionPoint + 1) achieves this
    // We add 1 inside the log so that at x == transitionPoint, log(1) == 0, which makes the function output exactly 'y'
    return y + logStrength * ln(x - transitionPoint + 1.0f)
  }
}