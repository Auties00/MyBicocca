package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component

import java.util.Locale

// Whole grades show without a decimal ("8"), fractional ones keep one place ("8.5").
fun formatGradeValue(grade: Double): String = when {
    grade % 1.0 == 0.0 -> grade.toInt().toString()
    else -> String.format(Locale.ITALIAN, "%.1f", grade)
}
