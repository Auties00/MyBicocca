package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptQuestion
import it.attendance100.mybicocca.domain.model.elearning.quiz.AttemptReview
import it.attendance100.mybicocca.ui.component.text.HtmlBody
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.ext.parseQuestion

/**
 * The finished-attempt review as one page inside the quiz sheet: a grade hero, optional
 * overall feedback, then every question read-only with its marks. Height-bounded so it scrolls
 * within the sheet and closed by the same full-pill action bar as the attempt wizard; the
 * close button (and the header back) return to the overview.
 */
@Composable
internal fun AttemptReviewContent(
    review: AttemptReview,
    onClose: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    BackHandler(onBack = onClose)
    val questions = remember(review) { review.pages.flatMap { it.questions } }

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 540.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp),
        ) {
            item("review_hero") {
                ReviewGradeHero(review = review, questions = questions)
            }
            val feedback = review.feedback?.takeIf { it.isNotBlank() }
            if (feedback != null) {
                item("review_feedback") {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = scheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        HtmlBody(html = feedback, modifier = Modifier.padding(14.dp))
                    }
                }
            }
            questions.forEach { question ->
                item("review_question_${question.slot}") {
                    val parsed = remember(question.html) { question.parseQuestion() }
                    QuestionCard(
                        question = question,
                        parsed = parsed,
                        answerFields = emptyMap(),
                        flagged = false,
                        readOnly = true,
                        onAnswer = {},
                        onToggleFlag = null,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = scheme.onPrimary,
            ),
        ) {
            Text(stringResource(R.string.elearning_quiz_close_review), fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Review opener: an oversized grade over the max grade, with a per-verdict count summary. */
@Composable
private fun ReviewGradeHero(review: AttemptReview, questions: List<AttemptQuestion>) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(
            text = stringResource(R.string.elearning_quiz_review_eyebrow),
            color = scheme.tertiary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.6.sp,
        )
        val grade = review.gradeFormatted?.toDoubleOrNull()
        if (grade != null || review.gradeFormatted != null) {
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = grade?.let(::formatGradeValue) ?: review.gradeFormatted,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Black,
                    fontSize = 44.sp,
                    lineHeight = 44.sp,
                    letterSpacing = (-2).sp,
                )
                review.maxGrade?.takeIf { it > 0 }?.let { max ->
                    Text(
                        text = "/ ${formatGradeValue(max)}",
                        color = scheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }
        val right = questions.count { it.state == "gradedright" }
        val partial = questions.count { it.state == "gradedpartial" }
        val wrong = questions.count { it.state == "gradedwrong" }
        val skipped = questions.count { it.state == "gaveup" }
        val summary = listOfNotNull(
            right.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.elearning_quiz_correct_count, it, it) },
            partial.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.elearning_quiz_partial_count, it, it) },
            wrong.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.elearning_quiz_wrong_count, it, it) },
            skipped.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.elearning_quiz_no_answer_count, it, it) },
        )
        if (summary.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = summary.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}
