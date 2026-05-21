package it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.quizDetail.state.QuizDetailOneShotEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun QuizDetailScreen(
    quizId: Int,
    courseId: Int,
    viewModel: QuizDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.oneShotEvents.collectLatest { event ->
            when (event) {
                is QuizDetailOneShotEvent.AttemptStarted -> Unit
                is QuizDetailOneShotEvent.AttemptSubmitted -> Unit
                is QuizDetailOneShotEvent.RefreshFailed -> Unit
            }
        }
    }

    @Suppress("UNUSED_PARAMETER") quizId
    @Suppress("UNUSED_PARAMETER") courseId
}
